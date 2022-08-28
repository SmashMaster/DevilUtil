/*
 * Copyright (c) 2019 Sam Johnson
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

package com.samrj.devil.graphics;

import com.samrj.devil.gl.Image;

import java.util.HashMap;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL14C.*;
import static org.lwjgl.opengl.GL30C.*;
import static org.lwjgl.opengl.GL33C.*;
import static org.lwjgl.opengl.GL41C.GL_RGB565;
import static org.lwjgl.opengl.GL42C.*;
import static org.lwjgl.opengl.GL43C.*;

/**
 * Texture utility methods.
 * 
 * @author Samuel Johnson (SmashMaster)
 */
public class TexUtil
{
    public enum Format
    {
        R8(GL_R8, GL_RED, GL_UNSIGNED_BYTE, 8),
        R8_SNORM(GL_R8_SNORM, GL_RED, GL_BYTE, 8),
        R16(GL_R16, GL_RED, GL_UNSIGNED_SHORT, 16),
        R16_SNORM(GL_R16_SNORM, GL_RED, GL_SHORT, 16),
        RG8(GL_RG8, GL_RG, GL_UNSIGNED_BYTE, 16),
        RG8_SNORM(GL_RG8_SNORM, GL_RG, GL_SHORT, 16),
        RG16(GL_RG16, GL_RG, GL_UNSIGNED_SHORT, 32),
        RG16_SNORM(GL_RG16_SNORM, GL_RG, GL_SHORT, 32),
        R3_G3_B2(GL_R3_G3_B2, GL_RGB, -1, 8),
        RGB4(GL_RGB4, GL_RGB, -1, 12),
        RGB5(GL_RGB5, GL_RGB, -1, 15),
        RGB565(GL_RGB565, GL_RGB, -1, 16),
        RGB8(GL_RGB8, GL_RGB, GL_UNSIGNED_BYTE, 8),
        RGB8_SNORM(GL_RGB8_SNORM, GL_RGB, GL_BYTE, 24),
        RGB10(GL_RGB10, GL_RGB, -1, 30),
        RGB12(GL_RGB12, GL_RGB, -1, 36),
        RGB16(GL_RGB16, GL_RGB, GL_UNSIGNED_SHORT, 48),
        RGB16_SNORM(GL_RGB16_SNORM, GL_RGB, GL_SHORT, 48),
        RGBA2(GL_RGBA2, GL_RGBA, -1, 8),
        RGBA4(GL_RGBA4, GL_RGBA, -1, 16),
        RGB5_A1(GL_RGB5_A1, GL_RGBA, -1, 16),
        RGBA8(GL_RGBA8, GL_RGBA, GL_UNSIGNED_BYTE, 32),
        RGBA8_SNORM(GL_RGBA8_SNORM, GL_RGBA, GL_BYTE, 32),
        RGB10_A2(GL_RGB10_A2, GL_RGBA, -1, 32),
        RGB10_A2UI(GL_RGB10_A2UI, GL_RGBA, -1, 32),
        RGBA12(GL_RGBA12, GL_RGBA, -1, 48),
        RGBA16(GL_RGBA16, GL_RGBA, GL_UNSIGNED_SHORT, 64),
        RGBA16_SNORM(GL_RGBA16_SNORM, GL_RGBA, GL_SHORT, 64),
        SRGB8(GL_SRGB8, GL_RGB, GL_UNSIGNED_BYTE, 24),
        SRGB8_ALPHA8(GL_SRGB8_ALPHA8, GL_RGBA, GL_BYTE, 32),
        R16F(GL_R16F, GL_RED, GL_HALF_FLOAT, 16),
        RG16F(GL_RG16F, GL_RG, GL_HALF_FLOAT, 32),
        RGB16F(GL_RGB16F, GL_RGB, GL_HALF_FLOAT, 48),
        RGBA16F(GL_RGBA16F, GL_RGBA, GL_HALF_FLOAT, 64),
        R32F(GL_R32F, GL_RED, GL_FLOAT, 32),
        RG32F(GL_RG32F, GL_RG, GL_FLOAT, 64),
        RGB32F(GL_RGB32F, GL_RGB, GL_FLOAT, 96),
        RGBA32F(GL_RGBA32F, GL_RGBA, GL_FLOAT, 128),
        R11F_G11F_B10F(GL_R11F_G11F_B10F, GL_RGB, -1, 32),
        RGB9_E5(GL_RGB9_E5, GL_RGB, -1, 32),
        R8I(GL_R8I, GL_RED_INTEGER, GL_BYTE, 8),
        R8UI(GL_R8UI, GL_RED_INTEGER, GL_UNSIGNED_BYTE, 8),
        R16I(GL_R16I, GL_RED_INTEGER, GL_SHORT, 16),
        R16UI(GL_R16UI, GL_RED_INTEGER, GL_UNSIGNED_SHORT, 16),
        R32I(GL_R32I, GL_RED_INTEGER, GL_INT, 32),
        R32UI(GL_R32UI, GL_RED_INTEGER, GL_UNSIGNED_INT, 32),
        RG8I(GL_RG8I, GL_RG_INTEGER, GL_BYTE, 16),
        RG8UI(GL_RG8UI, GL_RG_INTEGER, GL_UNSIGNED_BYTE, 16),
        RG16I(GL_RG16I, GL_RG_INTEGER, GL_SHORT, 32),
        RG16UI(GL_RG16UI, GL_RG_INTEGER, GL_UNSIGNED_SHORT, 32),
        RG32I(GL_RG32I, GL_RG_INTEGER, GL_INT, 64),
        RG32UI(GL_RG32UI, GL_RG_INTEGER, GL_UNSIGNED_INT, 64),
        RGB8I(GL_RGB8I, GL_RGB_INTEGER, GL_BYTE, 24),
        RGB8UI(GL_RGB8UI, GL_RGB_INTEGER, GL_UNSIGNED_BYTE, 24),
        RGB16I(GL_RGB16I, GL_RGB_INTEGER, GL_SHORT, 48),
        RGB16UI(GL_RGB16UI, GL_RGB_INTEGER, GL_UNSIGNED_SHORT, 48),
        RGB32I(GL_RGB32I, GL_RGB_INTEGER, GL_INT, 96),
        RGB32UI(GL_RGB32UI, GL_RGB_INTEGER, GL_UNSIGNED_INT, 96),
        RGBA8I(GL_RGBA8I, GL_RGBA_INTEGER, GL_BYTE, 32),
        RGBA8UI(GL_RGBA8UI, GL_RGBA_INTEGER, GL_UNSIGNED_BYTE, 32),
        RGBA16I(GL_RGBA16I, GL_RGBA_INTEGER, GL_SHORT, 64),
        RGBA16UI(GL_RGBA16UI, GL_RGBA_INTEGER, GL_UNSIGNED_SHORT, 64),
        RGBA32I(GL_RGBA32I, GL_RGBA_INTEGER, GL_INT, 128),
        RGBA32UI(GL_RGBA32UI, GL_RGBA_INTEGER, GL_UNSIGNED_INT, 128),
        DEPTH_COMPONENT16(GL_DEPTH_COMPONENT16, GL_DEPTH_COMPONENT, GL_UNSIGNED_SHORT, 16),
        DEPTH_COMPONENT24(GL_DEPTH_COMPONENT24, GL_DEPTH_COMPONENT, -1, 24),
        DEPTH_COMPONENT32(GL_DEPTH_COMPONENT32, GL_DEPTH_COMPONENT, GL_UNSIGNED_INT, 32),
        DEPTH_COMPONENT32F(GL_DEPTH_COMPONENT32F, GL_DEPTH_COMPONENT, GL_FLOAT, 32),
        DEPTH24_STENCIL8(GL_DEPTH24_STENCIL8, GL_DEPTH_STENCIL, GL_UNSIGNED_INT_24_8, 32),
        DEPTH32F_STENCIL8(GL_DEPTH32F_STENCIL8, GL_DEPTH_STENCIL, GL_FLOAT_32_UNSIGNED_INT_24_8_REV, 40),
        STENCIL_INDEX1(GL_STENCIL_INDEX1, GL_STENCIL_INDEX, -1, 1),
        STENCIL_INDEX4(GL_STENCIL_INDEX4, GL_STENCIL_INDEX, -1, 4),
        STENCIL_INDEX8(GL_STENCIL_INDEX8, GL_STENCIL_INDEX, -1, 8),
        STENCIL_INDEX16(GL_STENCIL_INDEX16, GL_STENCIL_INDEX, -1, 16),
        COMPRESSED_RED(GL_COMPRESSED_RED, GL_RED, -1, 0),
        COMPRESSED_RG(GL_COMPRESSED_RG, GL_RG, -1, 0),
        COMPRESSED_RGB(GL_COMPRESSED_RGB, GL_RGB, -1, 0),
        COMPRESSED_RGBA(GL_COMPRESSED_RGBA, GL_RGBA, -1, 0),
        COMPRESSED_SRGB(GL_COMPRESSED_SRGB, GL_RGB, -1, 0),
        COMPRESSED_SRGB_ALPHA(GL_COMPRESSED_SRGB_ALPHA, GL_RGBA, -1, 0),
        COMPRESSED_RED_RGTC1(GL_COMPRESSED_RED_RGTC1, GL_RED, -1, 0),
        COMPRESSED_SIGNED_RED_RGTC1(GL_COMPRESSED_SIGNED_RED_RGTC1, GL_RED, -1, 0),
        COMPRESSED_RG_RGTC2(GL_COMPRESSED_RG_RGTC2, GL_RG, -1, 0),
        COMPRESSED_SIGNED_RG_RGTC2(GL_COMPRESSED_SIGNED_RG_RGTC2, GL_RG, -1, 0),
        COMPRESSED_RGBA_BPTC_UNORM(GL_COMPRESSED_RGBA_BPTC_UNORM, GL_RGBA, -1, 0),
        COMPRESSED_SRGB_ALPHA_BPTC_UNORM(GL_COMPRESSED_SRGB_ALPHA_BPTC_UNORM, GL_RGBA, -1, 0),
        COMPRESSED_RGB_BPTC_SIGNED_FLOAT(GL_COMPRESSED_RGB_BPTC_SIGNED_FLOAT, GL_RGB, -1, 0),
        COMPRESSED_RGB_BPTC_UNSIGNED_FLOAT(GL_COMPRESSED_RGB_BPTC_UNSIGNED_FLOAT, GL_RGB, -1, 0),
        COMPRESSED_RGB8_ETC2(GL_COMPRESSED_RGB8_ETC2, GL_RGB, -1, 0),
        COMPRESSED_SRGB8_ETC2(GL_COMPRESSED_SRGB8_ETC2, GL_RGB, -1, 0),
        COMPRESSED_RGB8_PUNCHTHROUGH_ALPHA1_ETC2(GL_COMPRESSED_RGB8_PUNCHTHROUGH_ALPHA1_ETC2, GL_RGB, -1, 0),
        COMPRESSED_SRGB8_PUNCHTHROUGH_ALPHA1_ETC2(GL_COMPRESSED_SRGB8_PUNCHTHROUGH_ALPHA1_ETC2, GL_RGB, -1, 0),
        COMPRESSED_RGBA8_ETC2_EAC(GL_COMPRESSED_RGBA8_ETC2_EAC, GL_RGBA, -1, 0),
        COMPRESSED_SRGB8_ALPHA8_ETC2_EAC(GL_COMPRESSED_SRGB8_ALPHA8_ETC2_EAC, GL_RGBA, -1, 0),
        COMPRESSED_R11_EAC(GL_COMPRESSED_R11_EAC, GL_RED, -1, 0),
        COMPRESSED_SIGNED_R11_EAC(GL_COMPRESSED_SIGNED_R11_EAC, GL_RED, -1, 0),
        COMPRESSED_RG11_EAC(GL_COMPRESSED_RG11_EAC, GL_RG, -1, 0),
        COMPRESSED_SIGNED_RG11_EAC(GL_COMPRESSED_SIGNED_RG11_EAC, GL_RG, -1, 0);

        public final int format, baseFormat, type, bits;

        Format(int format, int baseFormat, int type, int bits)
        {
            this.format = format;
            this.baseFormat = baseFormat;
            this.type = type != -1 ? type : GL_UNSIGNED_BYTE; //Default to unsigned bytes for texture storage
            this.bits = bits;
        }
    }

    private static final HashMap<Integer, Format> FORMATS = new HashMap<>();

    static
    {
        for (Format format : Format.values()) FORMATS.put(format.format, format);
    }

    private static Format getFormat(int format)
    {
        Format f = FORMATS.get(format);
        if (f == null) throw new IllegalArgumentException("Unrecognized format " + format);
        return f;
    }

    /**
     * @param baseFormat a base internal format, as in the internalFormat
     *                   argument of the {@code glTexImage2D()} method.
     * @return the number of components in a particular texture format.
     */
    public static int getBands(int baseFormat)
    {
        return switch (baseFormat)
        {
            case GL_ALPHA -> 1; //Deprecated
            case GL_DEPTH_COMPONENT -> 1;
            case GL_DEPTH_STENCIL -> 2;
            case GL_RED, GL_RED_INTEGER -> 1;
            case GL_RG, GL_RG_INTEGER -> 2;
            case GL_RGB, GL_RGB_INTEGER -> 3;
            case GL_RGBA, GL_RGBA_INTEGER -> 4;
            case GL_STENCIL_INDEX -> 1;
            default -> throw new IllegalArgumentException("Unrecognized base format " + baseFormat);
        };
    }
    
    /**
     * Returns the OpenGL format best corresponding with the given image, or -1
     * if none could be found.
     * 
     * @param image The image to get the format of.
     * @return The OpenGL format of the given image, or -1 if none exists.
     */
    public static int getFormat(Image image)
    {
        switch (image.type)
        {
            case BYTE: switch (image.bands)
                {
                    case 1: return GL_R8;
                    case 2: return GL_RG8;
                    case 3: return GL_RGB8;
                    case 4: return GL_RGBA8;
                }
                break;
            case CHAR: switch (image.bands)
                {
                    case 1: return GL_R16;
                    case 2: return GL_RG16;
                    case 3: return GL_RGB16;
                    case 4: return GL_RGBA16;
                }
                break;
            case INT: switch (image.bands)
                {
                    case 1: return GL_R32I;
                    case 2: return GL_RG32I;
                    case 3: return GL_RGB32I;
                    case 4: return GL_RGBA32I;
                }
                break;
            case FLOAT: switch (image.bands)
                {
                    case 1: return GL_R32F;
                    case 2: return GL_RG32F;
                    case 3: return GL_RGB32F;
                    case 4: return GL_RGBA32F;
                }
                break;
        }
        
        return -1;
    }
    
    /**
     * @param format an OpenGL texture format.
     * @return the base OpenGL internal texture format corresponding with the
     *         given format.
     */
    public static int getBaseFormat(int format)
    {
        return getFormat(format).baseFormat;
    }
    
    /**
     * Returns the OpenGL field name for the given format.
     * 
     * @param format An OpenGL texture format.
     * @return The OpenGL field name for the given format.
     */
    public static String formatToString(int format)
    {
        return "GL_" + getFormat(format);
    }

    public static String baseFormatToString(int baseFormat)
    {
        return switch (baseFormat)
        {
            case GL_ALPHA -> "GL_ALPHA"; //Deprecated
            case GL_DEPTH_COMPONENT -> "GL_DEPTH_COMPONENT";
            case GL_DEPTH_STENCIL -> "GL_DEPTH_STENCIL";
            case GL_RED -> "GL_RED";
            case GL_RED_INTEGER -> "GL_RED_INTEGER";
            case GL_RG -> "GL_RG";
            case GL_RG_INTEGER -> "GL_RG_INTEGER";
            case GL_RGB -> "GL_RGB";
            case GL_RGB_INTEGER -> "GL_RGB_INTEGER";
            case GL_RGBA -> "GL_RGBA";
            case GL_RGBA_INTEGER -> "GL_RGBA_INTEGER";
            case GL_STENCIL_INDEX -> "GL_STENCIL_INDEX";
            default -> "Unrecognized base format 0x" + Integer.toHexString(baseFormat);
        };
    }

    public static String typeToString(int type)
    {
        return switch (type)
        {
            case GL_BYTE -> "GL_BYTE";
            case GL_UNSIGNED_BYTE -> "GL_UNSIGNED_BYTE";
            case GL_SHORT -> "GL_SHORT";
            case GL_UNSIGNED_SHORT -> "GL_UNSIGNED_SHORT";
            case GL_INT -> "GL_INT";
            case GL_UNSIGNED_INT -> "GL_UNSIGNED_INT";
            case GL_HALF_FLOAT -> "GL_HALF_FLOAT";
            case GL_FLOAT -> "GL_FLOAT";
            case GL_UNSIGNED_INT_24_8 -> "GL_UNSIGNED_INT_24_8";
            case GL_FLOAT_32_UNSIGNED_INT_24_8_REV -> "GL_FLOAT_32_UNSIGNED_INT_24_8_REV";
            default -> "Unrecognized type 0x" + Integer.toHexString(type);
        };
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
            case 1: return GL_RED;
            case 2: return GL_RG;
            case 3: return GL_RGB;
            case 4: return GL_RGBA;
            default: return -1;
        }
    }

    /**
     * Returns the OpenGL enumerator for the given primitive type.
     *
     * @param format an OpenGL texture format.
     * @return the primitive data type associated with the given OpenGL format.
     */
    public static int getPrimitiveType(int format)
    {
        return getFormat(format).type;
    }

    /**
     * @param format an OpenGL texture format.
     * @return Approximately how many bits are stored per texel for the given format.
     */
    public static long getBits(int format)
    {
        return getFormat(format).bits;
    }

    /**
     * @param filter an OpenGL texture minify filter.
     * @return whether or not the given filter is a mipmap filter.
     */
    public static boolean isMipmapFilter(int filter)
    {
        switch (filter)
        {
            case GL_NEAREST_MIPMAP_NEAREST:
            case GL_LINEAR_MIPMAP_NEAREST:
            case GL_NEAREST_MIPMAP_LINEAR:
            case GL_LINEAR_MIPMAP_LINEAR: return true;
                
            default: return false;
        }
    }
    
    /**
     * Returns the OpenGL depth texture format for the given number of bits, or
     * -1 if none could be found.
     * 
     * @param bits The desired number of depth bits.
     * @return An OpenGL depth format.
     */
    public static int getDepthFormat(int bits)
    {
        switch (bits)
        {
            case 16: return GL_DEPTH_COMPONENT16;
            case 24: return GL_DEPTH_COMPONENT24;
            case 32: return GL_DEPTH_COMPONENT32;
            default: return -1;
        }
    }
    
    private TexUtil() {}
}
