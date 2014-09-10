package com.samrj.devil.graphics;

import com.samrj.devil.math.Util;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL30;

/**
 * Texture utility methods.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class TexUtil
{
    /**
     * @param baseFormat a base internal format, as in the internalFormat
     *                   argument of the {@code glTexImage2D()} method.
     * @return the number of components in a particular texture format. Supports
     *         only the core profile formats (OpenGL 3.2+).
     */
    public static int getBands(int baseFormat)
    {
        switch (baseFormat)
        {
            case GL11.GL_DEPTH_COMPONENT:
            case GL11.GL_RED: return 1;
                
            case GL30.GL_DEPTH_STENCIL:
            case GL30.GL_RG: return 2;
                
            case GL11.GL_RGB: return 3;
            case GL11.GL_RGBA: return 4;
            
            default: return -1;
        }
    }
    
    /**
     * @param format an OpenGL texture format.
     * @return the base OpenGL internal texture format corresponding with the
     *         given format.
     */
    public static int getBaseFormat(int format)
    {
        switch (format)
        {
            //Forward-compatible formats.
            case GL11.GL_DEPTH_COMPONENT:
            case GL14.GL_DEPTH_COMPONENT16:
            case GL14.GL_DEPTH_COMPONENT24:
            case GL14.GL_DEPTH_COMPONENT32:
            case GL30.GL_DEPTH_COMPONENT32F: return GL11.GL_DEPTH_COMPONENT;
                
            case GL11.GL_RED:
            case GL30.GL_R8:
            case GL30.GL_R16:
            case GL30.GL_R16F:
            case GL30.GL_R16I:
            case GL30.GL_R32F:
            case GL30.GL_R32I: return GL11.GL_RED;
                
            case GL30.GL_DEPTH_STENCIL:
            case GL30.GL_DEPTH24_STENCIL8:
            case GL30.GL_DEPTH32F_STENCIL8: return GL30.GL_DEPTH_STENCIL;
                
            case GL30.GL_RG:
            case GL30.GL_RG8:
            case GL30.GL_RG16:
            case GL30.GL_RG16F:
            case GL30.GL_RG16I:
            case GL30.GL_RG32F:
            case GL30.GL_RG32I: return GL30.GL_RG;
                
            case GL11.GL_RGB:
            case GL11.GL_RGB8:
            case GL11.GL_RGB16:
            case GL30.GL_RGB16F:
            case GL30.GL_RGB16I:
            case GL30.GL_RGB32F:
            case GL30.GL_RGB32I: return GL11.GL_RGB;
                
            case GL11.GL_RGBA:
            case GL11.GL_RGBA8:
            case GL11.GL_RGBA16:
            case GL30.GL_RGBA16F:
            case GL30.GL_RGBA16I:
            case GL30.GL_RGBA32F:
            case GL30.GL_RGBA32I: return GL11.GL_RGBA;
                
            default: return -1;
        }
    }
    /**
     * @param bands the number of color channels for a format.
     * @return the 'best guess' format compatible with the given number of
     *         bands.
     */
    public static int getDefaultFormat(int bands)
    {
        switch (bands)
        {
            case 1: return GL11.GL_RED;
            case 2: return GL30.GL_RG;
            case 3: return GL11.GL_RGB;
            case 4: return GL11.GL_RGBA;
                
            default: return -1;
        }
    }
    
    /**
     * @param format an OpenGL texture format.
     * @return the primitive data type associated with the given OpenGL format.
     */
    public static Util.PrimType getPrimType(int format)
    {
        switch (format)
        {
            case GL30.GL_R8:
            case GL30.GL_RG8:
            case GL11.GL_RGB8:
            case GL11.GL_RGBA8: return Util.PrimType.BYTE;
            
            case GL14.GL_DEPTH_COMPONENT16:
            case GL30.GL_R16:
            case GL30.GL_RG16:
            case GL11.GL_RGB16:
            case GL11.GL_RGBA16: return Util.PrimType.CHAR;
            
            case GL30.GL_R16I:
            case GL30.GL_RG16I:
            case GL30.GL_RGB16I:
            case GL30.GL_RGBA16I: return Util.PrimType.SHORT;
            
            case GL30.GL_DEPTH_COMPONENT32F:
            case GL30.GL_R32F:
            case GL30.GL_RG32F:
            case GL30.GL_RGB32F:
            case GL30.GL_RGBA32F: return Util.PrimType.FLOAT;
            
            case GL30.GL_R32I:
            case GL30.GL_RG32I:
            case GL30.GL_RGB32I:
            case GL30.GL_RGBA32I: return Util.PrimType.INT;
                
            default: return null;
        }
    }
    
    /**
     * @param filter an OpenGL texture minify filter.
     * @return whether or not the given filter is a mipmap filter.
     */
    public static boolean isMipmapFilter(int filter)
    {
        switch (filter)
        {
            case GL11.GL_NEAREST_MIPMAP_NEAREST:
            case GL11.GL_LINEAR_MIPMAP_NEAREST:
            case GL11.GL_NEAREST_MIPMAP_LINEAR:
            case GL11.GL_LINEAR_MIPMAP_LINEAR: return true;
                
            default: return false;
        }
    }
    
    private TexUtil() {}
}
