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

package com.samrj.devil.gl;

import com.samrj.devil.graphics.TexUtil;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL13C.nglCompressedTexImage2D;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * Abstract class for 2D textures.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @param <T> This texture's own type.
 */
abstract class Texture2DAbstract<T extends Texture2DAbstract<T>> extends Texture<T>
{
    private int width, height;
    
    Texture2DAbstract(int target, int binding)
    {
        super(target, binding);
        width = -1;
        height = -1;
    }
    
    /**
     * @return The width of this texture, or -1 if it has no associated image.
     */
    public int getWidth()
    {
        return width;
    }
    
    /**
     * @return The height of this texture, or -1 if it has no associated image.
     */
    public int getHeight()
    {
        return height;
    }
    
    /**
     * Allocates space on the GPU for an image of the given size and format, but
     * does not upload any information. Useful for attaching to frame buffers.
     * 
     * @param width The width of the image.
     * @param height The height of the image.
     * @param format The format of the image.
     * @return This texture.
     */
    public T image(int width, int height, int format)
    {
        if (width < 0 || height < 0) throw new IllegalArgumentException("Illegal image dimensions.");
        
        int baseFormat = TexUtil.getBaseFormat(format);
        if (baseFormat == -1) throw new IllegalArgumentException("Illegal image format.");
        
        this.width = width;
        this.height = height;
        int primType = TexUtil.getGLPrimitiveType(format);
        
        int oldID = tempBind();
        nglTexImage2D(target, 0, format, width, height, 0, baseFormat, primType, NULL);
        internalFormat = format;
        tempUnbind(oldID);
        
        setVRAMUsage(TexUtil.getBits(format)*width*height);
        
        return getThis();
    }
    
    /**
     * Allocates space on the GPU for the image, and then uploads it, linking it
     * to this texture. After calling, the image may be safely deleted from
     * memory. Any previous image data associated with this texture is released.
     * 
     * @param image The image to upload to the GPU.
     * @param format The texture format to store the image as.
     * @return This texture.
     */
    public T image(Image image, int format)
    {
        if (image.deleted()) throw new IllegalStateException("Image is deleted.");
        
        int dataFormat = TexUtil.getBaseFormat(format);
        if (image.bands != TexUtil.getBands(dataFormat))
            throw new IllegalArgumentException("Incompatible format bands.");
        
        width = image.width;
        height = image.height;
        int primType = TexUtil.getGLPrimitiveType(format);
        
        int oldID = tempBind();
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
        glTexImage2D(target, 0, format, width, height, 0, dataFormat, primType, image.buffer);
        internalFormat = format;
        tempUnbind(oldID);
        
        setVRAMUsage(TexUtil.getBits(format)*width*height);
        
        return getThis();
    }
    
    /**
     * Allocates space on the GPU for the image, and then uploads it, linking it
     * to this texture. After calling, the image may be safely deleted from
     * memory. Any previous image data associated with this texture is released.
     * 
     * @param image The image to upload to the GPU.
     * @return This texture.
     */
    public T image(Image image)
    {
        int format = TexUtil.getFormat(image);
        if (format == -1) throw new IllegalArgumentException("Illegal image format.");
        return image(image, format);
    }
    
    /**
     * Uploads the given compressed image to this texture. After calling, the
     * image may be safely deleted from memory. Any previous image data
     * associated with this texture is released.
     * 
     * @param image The compressed image to upload.
     * @return This texture.
     */
    public T image(ImageCompressed image)
    {
        if (image.deleted()) throw new IllegalStateException("Image is deleted.");
        
        width = image.width;
        height = image.height;
        
        int oldID = tempBind();
        nglCompressedTexImage2D(target, 0, image.format, width, height, 0, image.size(), image.address());
        internalFormat = image.format;
        tempUnbind(oldID);
        
        setVRAMUsage(image.size());
        
        return getThis();
    }
    
    /**
     * Overwrites the stored image for this texture with the given image. This
     * texture must already have allocated storage.
     * 
     * @param image The image to upload to the GPU.
     * @param format The texture format to store the image as.
     * @return This texture.
     */
    public T subimage(Image image, int format)
    {
        if (format != internalFormat)
            throw new IllegalArgumentException("Illegal format " + TexUtil.formatToString(format) + ", expected " + TexUtil.formatToString(internalFormat));

        if (image.deleted()) throw new IllegalStateException("Image is deleted.");
        
        int dataFormat = TexUtil.getBaseFormat(format);
        if (image.bands != TexUtil.getBands(dataFormat))
            throw new IllegalArgumentException("Incompatible format bands.");
        
        if (image.width != width || image.height != height)
            throw new IllegalArgumentException("Incompatible image dimensions.");
        
        int primType = TexUtil.getGLPrimitiveType(format);
        int oldID = tempBind();
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
        glTexSubImage2D(target, 0, 0, 0, width, height, dataFormat, primType, image.buffer);
        tempUnbind(oldID);
        return getThis();
    }

    /**
     * Overwrites the stored image for this texture with the given image. This
     * texture must already have allocated storage.
     * 
     * @param image The image to upload to the GPU.
     * @return This texture.
     */
    public T subimage(Image image)
    {
        int format = TexUtil.getFormat(image);
        if (format != internalFormat)
            throw new IllegalArgumentException("Illegal format " + TexUtil.formatToString(format) + ", expected " + TexUtil.formatToString(internalFormat));
        return subimage(image, internalFormat);
    }
    
    /**
     * Downloads the OpenGL data for this texture into the given image.
     */
    public T download(Image image, int format)
    {
        if (format != internalFormat)
            throw new IllegalArgumentException("Illegal format " + TexUtil.formatToString(format) + ", expected " + TexUtil.formatToString(internalFormat));

        int dataFormat = TexUtil.getBaseFormat(format);
        int primType = TexUtil.getGLPrimitiveType(format);
        int oldID = tempBind();
        image.buffer.clear();
        glPixelStorei(GL_PACK_ALIGNMENT, 1);
        glGetTexImage(target, 0, dataFormat, primType, image.buffer);
        tempUnbind(oldID);
        return getThis();
    }

    /**
     * Downloads the OpenGL data for this texture into the given image.
     */
    public T download(Image image)
    {
        return download(image, internalFormat);
    }

    @Override
    public String toString()
    {
        return getWidth() + "x" + getHeight() + " " + getClass().getSimpleName();
    }
}
