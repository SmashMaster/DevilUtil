package com.samrj.devil.gui;

import com.samrj.devil.game.GLFWUtil;
import com.samrj.devil.gl.DGL;
import com.samrj.devil.gl.ShaderProgram;
import com.samrj.devil.gl.VertexStream;
import com.samrj.devil.math.Mat4;
import com.samrj.devil.math.Vec2;
import com.samrj.devil.math.Vec2i;
import com.samrj.devil.math.Vec4;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import org.lwjgl.nuklear.NkUserFont;
import org.lwjgl.nuklear.NkUserFontGlyph;
import org.lwjgl.stb.STBTTAlignedQuad;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTTPackContext;
import org.lwjgl.stb.STBTTPackedchar;
import org.lwjgl.system.MemoryStack;

import static org.lwjgl.nuklear.Nuklear.*;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL12C.*;
import static org.lwjgl.opengl.GL13C.*;
import static org.lwjgl.opengl.GL14C.*;
import static org.lwjgl.stb.STBTruetype.*;
import static org.lwjgl.system.MemoryUtil.*;

/**
 * Loads TTF fonts using STB, and then converts them for Nuklear.
 * 
 * Adopted from LWJGL demo code, Copyright 2019 LWJGL, under this license:
 * https://www.lwjgl.org/license
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2019 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Font
{
    private static final int MAX_STREAM_VERTICES = 1024;
    private static final Vec4 WHITE = new Vec4(1.0f);
    
    private static ShaderProgram shader;
    private static long window;
    private static VertexStream vStream;
    private static Vec2 vPos;
    private static Vec2 vUV;
    private static Vec4 vColor;
    
    static void guiInit(ShaderProgram shader, long window)
    {
        Font.shader = shader;
        Font.window = window;
        vStream = DGL.genVertexStream(MAX_STREAM_VERTICES, 0);
        vPos = vStream.vec2("Position");
        vUV = vStream.vec2("TexCoord");
        vColor = vStream.vec4("Color");
        vStream.begin();
    }
    
    static void guiDestroy()
    {
        DGL.delete(vStream);
    }
    
    public final NkUserFont nkFont;
    
    private final FontProperties props;
    private final STBTTFontinfo fontInfo;
    private final STBTTPackedchar.Buffer cdata;
    private final ByteBuffer ttf;
    
    private final float scale, descent;
    
    private final int glTexID;
    
    private final BakedChar nullChar;
    private final BakedChar[] chars;
    
    /**
     * loads a TTF font using the given InputStream and properties. The stream
     * is read completely and then closed. An OpenGL context must exist on the
     * calling thread.
     */
    public Font(InputStream in, FontProperties properties) throws IOException
    {
        nkFont = NkUserFont.malloc();
        props = new FontProperties(properties);
        fontInfo = STBTTFontinfo.malloc();
        cdata = STBTTPackedchar.malloc(props.count);
        
        //Read whole font to buffer.
        byte[] bytes = in.readAllBytes();
        in.close();
        ttf = memAlloc(bytes.length);
        ttf.put(bytes);
        ttf.flip();
        
        stbtt_InitFont(fontInfo, ttf);
        
        scale = stbtt_ScaleForPixelHeight(fontInfo, props.height);
        
        //Pack font into bitmap.
        ByteBuffer bitmap = memAlloc(props.bitmapWidth*props.bitmapHeight);
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
        ByteBuffer texture = memAlloc(props.bitmapWidth*props.bitmapHeight*4);
        for (int i = 0; i < bitmap.capacity(); i++)
            texture.putInt((bitmap.get(i) << 24) | 0x00FFFFFF);
        texture.flip();
        memFree(bitmap);

        //Upload to GPU and clean up after ourselves.
        glTexID = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, glTexID);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, props.bitmapWidth, props.bitmapHeight, 0, GL_RGBA, GL_UNSIGNED_INT_8_8_8_8_REV, texture);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glBindTexture(GL_TEXTURE_2D, 0);
        memFree(texture);
        
        //Bake character quads ahead of time so STB doesn't crash the JVM when something inevitably goes wrong.
        nullChar = new BakedChar();
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
                c.nkOffsetY = q.y0() + (props.height + descent);
                c.uvS0 = q.s0();
                c.uvT0 = q.t0();
                c.uvS1 = q.s1();
                c.uvT1 = q.t1();
            }
        }
        
        //Give font pack data to Nuklear.
        nkFont.width((handle, h, text, len) ->
        {
            float text_width = 0;
            try (MemoryStack stack = MemoryStack.stackPush())
            {
                IntBuffer unicode = stack.mallocInt(1);

                int glyph_len = nnk_utf_decode(text, memAddress(unicode), len);
                if (glyph_len == 0) return 0;
                
                int text_len = glyph_len;
                
                IntBuffer advance = stack.mallocInt(1);
                while (text_len <= len && glyph_len != 0)
                {
                    if (unicode.get(0) == NK_UTF_INVALID) break;
                    
                    stbtt_GetCodepointHMetrics(fontInfo, unicode.get(0), advance, null);
                    text_width += advance.get(0)*scale;
                    
                    glyph_len = nnk_utf_decode(text + text_len, memAddress(unicode), len - text_len);
                    text_len += glyph_len;
                }
            }
            
            return text_width;
        })
        .height(props.height)
        .query((handle, font_height, glyph, codepoint, next_codepoint) ->
        {
            BakedChar c = codepoint < chars.length ? chars[codepoint] : nullChar;
            NkUserFontGlyph ufg = NkUserFontGlyph.create(glyph);
            ufg.width(c.width);
            ufg.height(c.height);
            ufg.offset().set(c.offsetX, c.nkOffsetY);
            ufg.xadvance(c.advance);
            ufg.uv(0).set(c.uvS0, c.uvT0);
            ufg.uv(1).set(c.uvS1, c.uvT1);
        })
        .texture(it -> it.id(glTexID));
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
     * Returns the total height of this font, which is equal to its ascent plus
     * its descent.
     */
    public float getHeight()
    {
        return props.height;
    }
    
    /**
     * Returns the descent of this font, which is the distance between its
     * baseline and the lowest descenders of this font.
     */
    public float getDescent()
    {
        return descent;
    }
    
    private Vec2 alignPos(String text, Vec2 pos, Vec2 align)
    {
        pos = new Vec2(pos.x, pos.y);
        align = new Vec2(align.x - 1.0f, align.y - 1.0f).mult(0.5f);
        align.x *= getWidth(text);
        align.y *= props.height;
        pos.add(align);
        return pos;
    }
    
    /**
     * Renders the given text at the given position, with the given alignment
     * and color.
     */
    public void render(String text, Vec2 pos, Vec2 alignment, Vec4 color)
    {
        pos = alignPos(text, pos, alignment);
        
        Vec2i size = GLFWUtil.getWindowSize(window);
        Vec2i res = GLFWUtil.getFramebufferSize(window);
        
        //Set up OpenGL state
        DGL.useProgram(shader);
        glEnable(GL_BLEND);
        glBlendEquation(GL_FUNC_ADD);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glDisable(GL_CULL_FACE);
        glDisable(GL_DEPTH_TEST);
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, glTexID);
        
        DGL.useProgram(shader);
        shader.uniform1i("Texture", 0);
        shader.uniformMat4("ProjMtx", Mat4.orthographic(0.0f, size.x, 0.0f, size.y, -1.0f, 1.0f));
        glViewport(0, 0, res.x, res.y);
        
        int length = text.length();
        float xpos = pos.x;
        
        vColor.set(color);
        
        for (int offset = 0; offset < length;)
        {
           int codepoint = text.codePointAt(offset);
           BakedChar c = codepoint < chars.length ? chars[codepoint] : nullChar;
           
           float x0 = xpos + c.offsetX;
           float x1 = x0 + c.width;
           float y1 = pos.y - c.offsetY;
           float y0 = y1 - c.height;
           
           vPos.set(x0, y0); vUV.set(c.uvS0, c.uvT1); vStream.vertex();
           vPos.set(x0, y1); vUV.set(c.uvS0, c.uvT0); vStream.vertex();
           vPos.set(x1, y1); vUV.set(c.uvS1, c.uvT0); vStream.vertex();
           
           vPos.set(x0, y0); vUV.set(c.uvS0, c.uvT1); vStream.vertex();
           vPos.set(x1, y1); vUV.set(c.uvS1, c.uvT0); vStream.vertex();
           vPos.set(x1, y0); vUV.set(c.uvS1, c.uvT1); vStream.vertex();
           
           xpos += c.advance;
           offset += Character.charCount(codepoint);
        }
        
        vStream.upload();
        DGL.draw(vStream, GL_TRIANGLES);
        
        //Reset OpenGL state
        DGL.useProgram(null);
        glDisable(GL_BLEND);
    }
    
    /**
     * Renders the given text at the given position, with the given alignment.
     */
    public void render(String text, Vec2 pos, Vec2 alignment)
    {
        render(text, pos, alignment, WHITE);
    }
    
    /**
     * Renders the given text at the given position, with the given alignment
     * and color.
     */
    public void render(String text, Vec2 pos, Alignment alignment, Vec4 color)
    {
        render(text, pos, alignment.dir(), color);
    }
    
    /**
     * Renders the given text at the given position, with the given alignment.
     */
    public void render(String text, Vec2 pos, Alignment alignment)
    {
        render(text, pos, alignment.dir(), WHITE);
    }
    
    /**
     * Frees up and native resources associated with this font.
     */
    public void destroy()
    {
        glDeleteTextures(nkFont.texture().id());
        nkFont.query().free();
        nkFont.width().free();
        nkFont.free();
        fontInfo.free();
        cdata.free();
        memFree(ttf);
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
    
    private class BakedChar
    {
        private float width, height;
        private float offsetX, offsetY;
        private float nkOffsetY;
        private float advance;
        private float uvS0, uvT0;
        private float uvS1, uvT1;
    }
}
