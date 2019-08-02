package com.samrj.devil.gui;

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
    public final NkUserFont nkFont;
    
    private final FontProperties props;
    private final STBTTFontinfo fontInfo;
    private final STBTTPackedchar.Buffer cdata;
    private final ByteBuffer ttf;
    
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
        
        float scale = stbtt_ScaleForPixelHeight(fontInfo, props.height);
        float descent;
        
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
        int glTexID = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, glTexID);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, props.bitmapWidth, props.bitmapHeight, 0, GL_RGBA, GL_UNSIGNED_INT_8_8_8_8_REV, texture);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glBindTexture(GL_TEXTURE_2D, 0);
        memFree(texture);
        
        //Bake character quads ahead of time so STB doesn't crash the JVM when something inevitably goes wrong.
        BakedChar nullChar = new BakedChar();
        BakedChar[] chars = new BakedChar[props.first + props.count];
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
                c.offsetY = q.y0() + (props.height + descent);
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
            ufg.offset().set(c.offsetX, c.offsetY);
            ufg.xadvance(c.advance);
            ufg.uv(0).set(c.uvS0, c.uvT0);
            ufg.uv(1).set(c.uvS1, c.uvT1);
        })
        .texture(it -> it.id(glTexID));
    }
    
    /**
     * loads a TTF font using the given InputStream, with the default font
     * properties. The stream is read completely and then closed.
     */
    public Font(InputStream in) throws IOException
    {
        this(in, new FontProperties());
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
        private float advance;
        private float uvS0, uvT0;
        private float uvS1, uvT1;
    }
}
