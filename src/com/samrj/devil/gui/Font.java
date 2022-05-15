package com.samrj.devil.gui;

import com.samrj.devil.gl.DGL;
import com.samrj.devil.gl.Texture2D;
import com.samrj.devil.math.Vec2;
import com.samrj.devil.util.IOUtil;
import org.lwjgl.stb.STBTTAlignedQuad;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTTPackContext;
import org.lwjgl.stb.STBTTPackedchar;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL12C.GL_UNSIGNED_INT_8_8_8_8_REV;
import static org.lwjgl.stb.STBTruetype.*;
import static org.lwjgl.system.MemoryUtil.*;

/**
 * Loads TTF fonts using STB.
 * 
 * Adopted and heavily modified from LWJGL demo code, Copyright 2019 LWJGL,
 * under this license: https://www.lwjgl.org/license
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2020 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Font
{
    static final BakedChar NULL_CHAR = new BakedChar();
    
    private final FontProperties props;
    private final STBTTFontinfo fontInfo;
    private final ByteBuffer ttf;
    
    private final float scale, descent;
    
    final Texture2D texture;
    final BakedChar[] chars;
    
    private boolean isDestroyed;
    
    /**
     * loads a TTF font using the given InputStream and properties. The stream
     * is read completely and then closed. An OpenGL context must exist on the
     * calling thread.
     */
    public Font(InputStream in, FontProperties properties) throws IOException
    {
        props = new FontProperties(properties);
        fontInfo = STBTTFontinfo.malloc();
        
        //Read whole font to buffer.
        byte[] bytes = IOUtil.readAllBytes(in);
        in.close();
        ttf = memAlloc(bytes.length);
        ttf.put(bytes);
        ttf.flip();
        
        stbtt_InitFont(fontInfo, ttf);
        
        scale = stbtt_ScaleForPixelHeight(fontInfo, props.height);
        
        //Pack font into bitmap.
        ByteBuffer bitmap = memAlloc(props.bitmapWidth*props.bitmapHeight);
        STBTTPackedchar.Buffer cdata = STBTTPackedchar.malloc(props.count);
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            IntBuffer d = stack.mallocInt(1);
            stbtt_GetFontVMetrics(fontInfo, null, d, null);
            descent = d.get(0)*scale;
            
            STBTTPackContext pc = STBTTPackContext.mallocStack(stack);
            stbtt_PackBegin(pc, bitmap, props.bitmapWidth, props.bitmapHeight, 0, 1, NULL);
            stbtt_PackSetOversampling(pc, props.supersampling, props.supersampling);
            stbtt_PackFontRange(pc, ttf, 0, props.height, props.first, cdata);
            stbtt_PackEnd(pc);
        }
        
        //Convert bitmap from R8 to RGBA8.
        ByteBuffer rgba8 = memAlloc(props.bitmapWidth*props.bitmapHeight*4);
        for (int i = 0; i < bitmap.capacity(); i++)
            rgba8.putInt((bitmap.get(i) << 24) | 0x00FFFFFF);
        rgba8.flip();
        memFree(bitmap);

        //Upload to GPU and clean up after ourselves.
        texture = DGL.genTex2D();
        texture.bind();
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, props.bitmapWidth, props.bitmapHeight, 0, GL_RGBA, GL_UNSIGNED_INT_8_8_8_8_REV, rgba8);
        texture.unbind();
        memFree(rgba8);
        
        //Bake character quads ahead of time so STB doesn't crash the JVM when something inevitably goes wrong.
        chars = new BakedChar[props.first + props.count];
        for (int codepoint=0; codepoint<chars.length; codepoint++)
        {
            BakedChar c = new BakedChar();
            chars[codepoint] = c;
            
            try (MemoryStack stack = MemoryStack.stackPush())
            {
                IntBuffer advance = stack.mallocInt(1);
                stbtt_GetCodepointHMetrics(fontInfo, codepoint, advance, null);
                c.advance = advance.get(0)*scale;
                
                if (codepoint < props.first) continue;
                
                FloatBuffer x = stack.floats(0.0f);
                FloatBuffer y = stack.floats(0.0f);
                STBTTAlignedQuad q = STBTTAlignedQuad.callocStack(stack);
                stbtt_GetPackedQuad(cdata, props.bitmapWidth, props.bitmapHeight, codepoint - props.first, x, y, q, false);
                c.width = q.x1() - q.x0();
                c.height = q.y1() - q.y0();
                c.offsetX = q.x0();
                c.offsetY = q.y0() + descent;
                c.uvS0 = q.s0();
                c.uvT0 = q.t0();
                c.uvS1 = q.s1();
                c.uvT1 = q.t1();
            }
        }
        
        cdata.free();
    }
    
    /**
     * Loads a TTF font using the given InputStream, with the default font
     * properties. The stream is read completely and then closed.
     */
    public Font(InputStream in) throws IOException
    {
        this(in, new FontProperties());
    }
    
    /**
     * Returns the width of a given string of text in pixels, if it were
     * rendered using this font.
     */
    public float getWidth(String text)
    {
        if (isDestroyed) throw new IllegalStateException("Font destroyed.");
        
        int length = text.length();
        int width = 0;
        
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            IntBuffer advance = stack.mallocInt(1);
            
            for (int offset = 0; offset < length;)
            {
               int codepoint = text.codePointAt(offset);
               stbtt_GetCodepointHMetrics(fontInfo, codepoint, advance, null);
               width += advance.get(0);
               offset += Character.charCount(codepoint);
            }
        }
        
        return width*scale;
    }
    
    /**
     * Returns the index closest to the given horizontal offset in a string.
     * Useful for finding the caret when selecting text.
     */
    public int getCaret(String text, float offsetX)
    {
        if (isDestroyed) throw new IllegalStateException("Font destroyed.");
        if (offsetX <= 0.0f) return 0;
        
        int length = text.length();
        float width = 0;
        
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            IntBuffer advance = stack.mallocInt(1);
            
            for (int offset = 0; offset < length;)
            {
               int codepoint = text.codePointAt(offset);
               stbtt_GetCodepointHMetrics(fontInfo, codepoint, advance, null);
               
               float fAdvance = advance.get(0)*scale;
               if (width + fAdvance*0.5f >= offsetX) return offset;
               
               width += fAdvance;
               offset += Character.charCount(codepoint);
            }
        }
        
        return length;
    }
    
    /**
     * Returns the total height of this font, which is equal to its ascent plus
     * its descent.
     */
    public float getHeight()
    {
        return props.height;
    }
    
    /**
     * Returns the width and height of the given string.
     */
    public Vec2 getSize(String text)
    {
        return new Vec2(getWidth(text), getHeight());
    }
    
    /**
     * Returns the descent of this font, which is the distance between its
     * baseline and the lowest descenders of this font.
     */
    public float getDescent()
    {
        return descent;
    }
    
    /**
     * Returns true if this font has been destroyed.
     */
    public boolean isDestroyed()
    {
        return isDestroyed;
    }
    
    /**
     * Frees up any native resources associated with this font.
     */
    public void destroy()
    {
        if (!isDestroyed)
        {
            DGL.delete(texture);
            fontInfo.free();
            memFree(ttf);
            isDestroyed = true;
        }
    }
    
    /**
     * Defines the unicode range and bitmap rendering parameters for this font.
     */
    public static class FontProperties
    {
        public float height;
        public int first;
        public int count;
        public int bitmapWidth, bitmapHeight;
        public int supersampling;
        
        public FontProperties(FontProperties props)
        {
            height = props.height;
            first = props.first;
            count = props.count;
            bitmapWidth = props.bitmapWidth;
            bitmapHeight = props.bitmapHeight;
            supersampling = props.supersampling;
        }
        
        public FontProperties()
        {
            height = 18.0f;
            first = 32;
            count = 95;
            bitmapWidth = 1024;
            bitmapHeight = 1024;
            supersampling = 4;
        }
    }
    
    static class BakedChar
    {
        float width, height;
        float offsetX, offsetY;
        float advance;
        float uvS0, uvT0;
        float uvS1, uvT1;
    }
}
