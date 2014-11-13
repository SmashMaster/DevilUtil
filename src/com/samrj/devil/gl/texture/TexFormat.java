package com.samrj.devil.gl.texture;

import com.samrj.devil.gl.util.Primitive;
import static com.samrj.devil.gl.util.Primitive.*;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL30;

public enum TexFormat
{
    R8   (GL30.GL_R8,    GL11.GL_RED,  1, BYTE),
    RG8  (GL30.GL_RG8,   GL30.GL_RG,   2, BYTE),
    RGB8 (GL11.GL_RGB8,  GL11.GL_RGB,  3, BYTE),
    RGBA8(GL11.GL_RGBA8, GL11.GL_RGBA, 4, BYTE),
    R16   (GL30.GL_R16UI,    GL30.GL_RED_INTEGER,  1, SHORT),
    RG16  (GL30.GL_RG16UI,   GL30.GL_RG_INTEGER,   2, SHORT),
    RGB16 (GL30.GL_RGB16UI,  GL30.GL_RGB_INTEGER,  3, SHORT),
    RGBA16(GL30.GL_RGBA16UI, GL30.GL_RGBA_INTEGER, 4, SHORT),
    R32   (GL30.GL_R32UI,    GL30.GL_RED_INTEGER,  1, INT),
    RG32  (GL30.GL_RG32UI,   GL30.GL_RG_INTEGER,   2, INT),
    RGB32 (GL30.GL_RGB32UI,  GL30.GL_RGB_INTEGER,  3, INT),
    RGBA32(GL30.GL_RGBA32UI, GL30.GL_RGBA_INTEGER, 4, INT),
    R32F   (GL30.GL_R32F,    GL11.GL_RED,  1, FLOAT),
    RG32F  (GL30.GL_RG32F,   GL30.GL_RG,   2, FLOAT),
    RGB32F (GL30.GL_RGB32F,  GL11.GL_RGB,  3, FLOAT),
    RGBA32F(GL30.GL_RGBA32F, GL11.GL_RGBA, 4, FLOAT),
    
    DEPTH16 (GL14.GL_DEPTH_COMPONENT16, GL11.GL_DEPTH_COMPONENT, 1, SHORT),
    DEPTH24 (GL14.GL_DEPTH_COMPONENT24, GL11.GL_DEPTH_COMPONENT, 1, INT),
    DEPTH32F(GL30.GL_DEPTH_COMPONENT32F, GL11.GL_DEPTH_COMPONENT, 1, FLOAT);
    
    public static TexFormat getFormat(int bands, Primitive type)
    {
        switch (type)
        {
            case BYTE: switch (bands)
            {
                case 1: return R8;
                case 2: return RG8;
                case 3: return RGB8;
                case 4: return RGBA8;
            }
            case SHORT: switch (bands)
            {
                case 1: return R16;
                case 2: return RG16;
                case 3: return RGB16;
                case 4: return RGBA16;
            }
            case INT: switch (bands)
            {
                case 1: return R32;
                case 2: return RG32;
                case 3: return RGB32;
                case 4: return RGBA32;
            }
            case FLOAT: switch (bands)
            {
                case 1: return R32F;
                case 2: return RG32F;
                case 3: return RGB32F;
                case 4: return RGBA32F;
            }
        }
        
        throw new IllegalArgumentException(
                "Illegal type/band count combination: " + type + "/" + bands + "!");
    }

    final int glFormat, glBaseFormat;
    public final int bands;
    public final Primitive type;

    private TexFormat(int glFormat, int glBaseFormat, int bands, Primitive type)
    {
        this.glFormat = glFormat; this.glBaseFormat = glBaseFormat;
        this.bands = bands;
        this.type = type;
    }
}
