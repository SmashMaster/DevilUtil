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
    
    public Font(InputStream in, FontProperties properties) throws IOException
    {
        nkFont = NkUserFont.malloc();
        
        props = new FontProperties(properties);
        
        //Read whole font to buffer.
        byte[] bytes = in.readAllBytes();
        in.close();
        ByteBuffer ttf = memAlloc(bytes.length);
        ttf.put(bytes);
        ttf.flip();
        
        int glTexID = glGenTextures();
        
        fontInfo = STBTTFontinfo.malloc();
        stbtt_InitFont(fontInfo, ttf);
        
        float scale = stbtt_ScaleForPixelHeight(fontInfo, props.height);
        float descent;
        
        cdata = STBTTPackedchar.malloc(props.count);
        ByteBuffer bitmap = memAlloc(props.bitmapWidth*props.bitmapHeight);
        
        //Pack font into bitmap.
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            IntBuffer d = stack.mallocInt(1);
            stbtt_GetFontVMetrics(fontInfo, null, d, null);
            descent = d.get(0)*scale;
            
            STBTTPackContext pc = STBTTPackContext.mallocStack(stack);
            stbtt_PackBegin(pc, bitmap, props.bitmapWidth, props.bitmapHeight, 0, 1, NULL);
            stbtt_PackSetOversampling(pc, 4, 4);
            stbtt_PackFontRange(pc, ttf, 0, props.height, 32, cdata);
            stbtt_PackEnd(pc);
        }
        
        //Convert bitmap from R8 to RGBA8.
        ByteBuffer texture = memAlloc(props.bitmapWidth*props.bitmapHeight*4);
        for (int i = 0; i < bitmap.capacity(); i++)
            texture.putInt((bitmap.get(i) << 24) | 0x00FFFFFF);
        texture.flip();

        //Upload to GPU and clean up after ourselves.
        glBindTexture(GL_TEXTURE_2D, glTexID);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, props.bitmapWidth, props.bitmapHeight, 0, GL_RGBA, GL_UNSIGNED_INT_8_8_8_8_REV, texture);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);

        memFree(texture);
        memFree(bitmap);
        memFree(ttf);
        
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
            if (codepoint - props.first >= props.count) return;
            
            try (MemoryStack stack = MemoryStack.stackPush())
            {
                FloatBuffer x = stack.floats(0.0f);
                FloatBuffer y = stack.floats(0.0f);

                STBTTAlignedQuad q = STBTTAlignedQuad.mallocStack(stack);
                IntBuffer advance = stack.mallocInt(1);
                
                stbtt_GetPackedQuad(cdata, props.bitmapWidth, props.bitmapHeight, codepoint - props.first, x, y, q, false);
                stbtt_GetCodepointHMetrics(fontInfo, codepoint, advance, null);

                NkUserFontGlyph ufg = NkUserFontGlyph.create(glyph);
                ufg.width(q.x1() - q.x0());
                ufg.height(q.y1() - q.y0());
                ufg.offset().set(q.x0(), q.y0() + (props.height + descent));
                ufg.xadvance(advance.get(0) * scale);
                ufg.uv(0).set(q.s0(), q.t0());
                ufg.uv(1).set(q.s1(), q.t1());
            }
        })
        .texture(it -> it.id(glTexID));
    }
    
    public Font(InputStream in) throws IOException
    {
        this(in, new FontProperties());
    }
    
    public void destroy()
    {
        glDeleteTextures(nkFont.texture().id());
        nkFont.query().free();
        nkFont.width().free();
        nkFont.free();
        fontInfo.free();
        cdata.free();
    }
    
    public static class FontProperties
    {
        public float height = 18.0f;
        public int first = 32;
        public int count = 95;
        public int bitmapWidth = 1024, bitmapHeight = 1024;
        public int supersampling = 4;
        
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
        }
    }
}
