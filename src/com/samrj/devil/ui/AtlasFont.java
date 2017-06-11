/*
 * Copyright (c) 2015 Sam Johnson
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.samrj.devil.ui;

import com.samrj.devil.gl.DGL;
import com.samrj.devil.gl.Image;
import com.samrj.devil.gl.Texture2D;
import com.samrj.devil.gl.VertexStream;
import com.samrj.devil.io.LittleEndianInputStream;
import com.samrj.devil.math.Util;
import com.samrj.devil.math.Vec2;
import com.samrj.devil.res.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

/**
 * Bitmap font class for loading packed fonts generated by BMFont. The font data
 * file is the binary format. See BMFont's documentation for more details.
 * 
 * http://www.angelcode.com/products/bmfont/
 * 
 * @author Samuel Johnson (SmashMaster)
 */
public class AtlasFont
{
    private static final String MSG_ERROR_FORMAT = "Illegal file format specified.";
    
    private static void ensureByte(InputStream in, int b, String message) throws IOException
    {
        if (in.read() != b) throw new IOException(message);
    }
    
    private static void skip(InputStream in, int bytes) throws IOException
    {
        if (in.skip(bytes) != bytes) throw new IOException("Failed to skip bytes.");
    }
    
    private final String name;
    private final int lineHeight, baseHeight;
    private final Texture2D texture;
    private final Char[] chars;
    private final int firstCharID;
    
    private VertexStream stream;
    private Vec2 pos, coord;
    
    public AtlasFont(String directory, String fontFile) throws IOException
    {
        if (!directory.endsWith("/")) directory += "/";
        
        InputStream inputStream = Resource.open(directory + fontFile);
        LittleEndianInputStream in = new LittleEndianInputStream(inputStream);
        
        //HEADER
        ensureByte(in, 66, MSG_ERROR_FORMAT);
        ensureByte(in, 77, MSG_ERROR_FORMAT);
        ensureByte(in, 70, MSG_ERROR_FORMAT);
        ensureByte(in, 3, MSG_ERROR_FORMAT);
        
        //INFO BLOCK
        ensureByte(in, 1, "Expected info block first.");
        skip(in, 18); //Skip rest of info block
        name = in.readNullTermStr();
        
        //COMMON BLOCK
        ensureByte(in, 2, "Expected common block second.");
        skip(in, 4); //Skip info block size
        lineHeight = in.readLittleUnsignedShort();
        baseHeight = lineHeight - in.readLittleUnsignedShort();
        skip(in, 4);
        int pages = in.readLittleUnsignedShort();
        if (pages != 1) throw new IOException("Only one page texture supported.");
        if ((in.read() & 128) != 0) throw new IOException("Channel-packed fonts not supported.");
        ensureByte(in, 0, "Alpha channel must contain glyph data.");
        skip(in, 3);
        
        //PAGES BLOCK
        ensureByte(in, 3, "Expected pages block third.");
        skip(in, 4);
        String texFile = in.readNullTermStr();
        
        Image image = DGL.loadImage(directory + texFile);
        if (!Util.isPower2(image.width) || !Util.isPower2(image.height))
            throw new IOException("Texture dimensions must be powers of two.");
        if (image.bands != 1) throw new IOException("Texture format must have one band.");
        
        texture = DGL.genTex2D();
        texture.image(image, GL11.GL_ALPHA8);
        texture.bind();
        texture.parami(GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        texture.parami(GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        DGL.delete(image);
        
        //CHARS BLOCK
        ensureByte(in, 4, "Expected chars block fourth.");
        int numChars = in.readLittleInt()/20;
        
        List<Char> charList = new ArrayList<>(numChars);
        int minChar = Integer.MAX_VALUE, maxChar = -1;
        for (int i=0; i<numChars; i++)
        {
            Char c = new Char(in, texture.getWidth(), texture.getHeight());
            charList.add(c);
            if (c.id < minChar) minChar = c.id;
            if (c.id > maxChar) maxChar = c.id;
        }
        
        chars = new Char[maxChar - minChar + 1];
        for (Char c : charList) chars[c.id - minChar] = c;
        firstCharID = minChar;
        
        in.close();
    }
    
    public String getName()
    {
        return name;
    }
    
    private Char getChar(char c)
    {
        if (c < firstCharID) return null;
        
        int i = c - firstCharID;
        return i < chars.length ? chars[i] : null;
    }
    
    public float getWidth(char c)
    {
        Char ch = getChar(c);
        return ch != null ? ch.xAdvance : 0.0f;
    }
    
    public float getWidth(String text)
    {
        float out = 0.0f;
        for (int i=0; i<text.length(); i++) out += getWidth(text.charAt(i));
        return out;
    }
    
    public float getBaseHeight()
    {
        return baseHeight;
    }
    
    public float getHeight()
    {
        return lineHeight;
    }
    
    private Vec2 alignPos(String text, Vec2 pos, Vec2 align)
    {
        pos = new Vec2(pos.x, pos.y - lineHeight + baseHeight);
        align = new Vec2(align.x - 1.0f, -align.y - 1.0f).mult(0.5f);
        align.x *= getWidth(text);
        align.y *= baseHeight-lineHeight;
        pos.add(align);
        return pos;
    }
    
    public void draw(String text, Vec2 pos, Vec2 align)
    {
        if (stream == null)
        {
            stream = DGL.genVertexStream(1024, -1);
            this.pos = stream.vec2("in_pos");
            coord = stream.vec2("in_tex_coord");
            stream.begin();
        }
        
        pos = alignPos(text, pos, align);
        
        int x = 0;
        for (int i=0; i<text.length(); i++)
        {
            Char c = getChar(text.charAt(i));
            if (c == null) continue;
            
            int lf = Math.round(pos.x) + x + c.xOffset;
            int rt = lf + c.width;
            int bt = Math.round(pos.y) + c.yOffset;
            int tp = bt + c.height;
            
            coord.set(c.tx0, c.ty0); this.pos.set(lf, bt); stream.vertex();
            coord.set(c.tx0, c.ty1); this.pos.set(lf, tp); stream.vertex();
            coord.set(c.tx1, c.ty1); this.pos.set(rt, tp); stream.vertex();
            coord.set(c.tx1, c.ty0); this.pos.set(rt, bt); stream.vertex();
            
            x += c.xAdvance;
        }
        stream.upload();
        
        texture.bind(GL13.GL_TEXTURE0);
        DGL.draw(stream, GL11.GL_QUADS);
    }
    
    public void draw(String text, Vec2 p, Alignment align)
    {
        draw(text, p, align.dir());
    }
    
    public void draw(String text, Vec2 p)
    {
        draw(text, p, Alignment.NE);
    }
    
    public void drawDeprecated(String text, Vec2 pos, Vec2 align)
    {
        pos = alignPos(text, pos, align);
        
        boolean shouldEnable = !GL11.glIsEnabled(GL11.GL_TEXTURE_2D);
        if (shouldEnable) GL11.glEnable(GL11.GL_TEXTURE_2D);
        
        texture.bind(GL13.GL_TEXTURE0);
        GL11.glBegin(GL11.GL_QUADS);
        
        int x = 0;
        for (int i=0; i<text.length(); i++)
        {
            Char c = getChar(text.charAt(i));
            if (c == null) continue;
            
            int lf = Math.round(pos.x) + x + c.xOffset;
            int rt = lf + c.width;
            int bt = Math.round(pos.y) + c.yOffset;
            int tp = bt + c.height;
            
            GL11.glTexCoord2f(c.tx0, c.ty0); GL11.glVertex2f(lf, bt);
            GL11.glTexCoord2f(c.tx0, c.ty1); GL11.glVertex2f(lf, tp);
            GL11.glTexCoord2f(c.tx1, c.ty1); GL11.glVertex2f(rt, tp);
            GL11.glTexCoord2f(c.tx1, c.ty0); GL11.glVertex2f(rt, bt);
            
            x += c.xAdvance;
        }
        
        GL11.glEnd();
        
        if (shouldEnable) GL11.glDisable(GL11.GL_TEXTURE_2D);
    }
    
    public void drawDeprecated(String text, Vec2 p, Alignment align)
    {
        drawDeprecated(text, p, align.dir());
    }
    
    public void drawDeprecated(String text, Vec2 p)
    {
        drawDeprecated(text, p, Alignment.NE);
    }
    
    public void delete()
    {
        if (stream != null)
        {
            DGL.delete(stream);
            stream = null;
        }
        
        DGL.delete(texture);
    }
    
    private class Char
    {
        private final int id;
        private final int width, height;
        private final float tx0, tx1, ty0, ty1;
        private final int xOffset;
        private final int yOffset;
        private final int xAdvance;
        
        private Char(LittleEndianInputStream in, float texWidth, float texHeight) throws IOException
        {
            id = in.readLittleInt();
            float x = in.readLittleUnsignedShort();
            float y = in.readLittleUnsignedShort();
            width = in.readLittleUnsignedShort();
            height = in.readLittleUnsignedShort();
            xOffset = in.readLittleUnsignedShort();
            yOffset = lineHeight - baseHeight - height - in.readLittleUnsignedShort();
            xAdvance = in.readLittleUnsignedShort();
            skip(in, 2);
            
            tx0 = x/texWidth;
            tx1 = (x + width)/texWidth;
            
            ty0 = 1.0f - (y + height)/texHeight;
            ty1 = 1.0f - y/texHeight;
        }
    }
}
