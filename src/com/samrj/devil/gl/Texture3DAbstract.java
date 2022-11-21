package com.samrj.devil.gl;

import com.samrj.devil.graphics.TexUtil;

import static org.lwjgl.opengl.GL11C.GL_UNPACK_ALIGNMENT;
import static org.lwjgl.opengl.GL11C.glPixelStorei;
import static org.lwjgl.opengl.GL12C.glTexSubImage3D;
import static org.lwjgl.opengl.GL12C.nglTexImage3D;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * Abstract class for 3D textures.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @param <T> This texture's own type.
 * @copyright 2019 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
abstract class Texture3DAbstract<T extends Texture3DAbstract<T>> extends Texture<T>
{
    private int width, height, depth;
    
    Texture3DAbstract(int target, int binding)
    {
        super(target, binding);
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
     * @return The depth of this texture, or -1 if it has no associated image.
     */
    public int getDepth()
    {
        return depth;
    }
    
    /**
     * Allocates space on the GPU for an image of the given size and format, but
     * does not upload any information.
     * 
     * @param width The width of the image.
     * @param height The height of the image.
     * @param depth The depth of the image.
     * @param format The format of the image.
     * @return This texture.
     */
    public T image(int width, int height, int depth, int format)
    {
        if (width < 0 || height < 0 || depth < 0)
            throw new IllegalArgumentException("Illegal image dimensions.");
        
        int baseFormat = TexUtil.getBaseFormat(format);
        if (baseFormat == -1) throw new IllegalArgumentException("Illegal image format.");
        
        int primType = TexUtil.getGLPrimitiveType(format);
        
        this.width = width;
        this.height = height;
        this.depth = depth;
        int oldID = tempBind();
        nglTexImage3D(target, 0, format, width, height, depth, 0, baseFormat, primType, NULL);
        internalFormat = format;
        tempUnbind(oldID);
        
        setVRAMUsage(TexUtil.getBits(format)*width*height*depth);
        
        return getThis();
    }
    
    /**
     * Overwrites the stored image layer with the given image. This texture must
     * already have allocated storage.
     * 
     * @param image The image to overwrite the 
     * @param depth The image layer to overwrite.
     * @param format The texture format to store the image as.
     * @return This texture.
     */
    public T subimage(Image image, int depth, int format)
    {
        if (image.deleted()) throw new IllegalStateException("Image is deleted.");
        
        int dataFormat = TexUtil.getBaseFormat(format);
        if (image.bands != TexUtil.getBands(dataFormat))
            throw new IllegalArgumentException("Incompatible format bands.");
        
        if (depth < 0 || depth >= this.depth)
            throw new IllegalArgumentException("Illegal depth specified.");
        
        if (image.width != width || image.height != height)
            throw new IllegalArgumentException("Incompatible image dimensions.");
        
        int primType = TexUtil.getGLPrimitiveType(format);
        int oldID = tempBind();
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
        glTexSubImage3D(target, 0, 0, 0, depth, width, height, 1, dataFormat, primType, image.buffer);
        internalFormat = format;
        tempUnbind(oldID);
        return getThis();
    }
    
    /**
     * Overwrites the stored image layer with the given image. This texture must
     * already have allocated storage.
     * 
     * @param image The image to overwrite the 
     * @param depth The image layer to overwrite.
     */
    public T subimage(Image image, int depth)
    {
        int format = TexUtil.getFormat(image);
        if (format != internalFormat)
            throw new IllegalArgumentException("Illegal format " + TexUtil.formatToString(format) + ", expected " + TexUtil.formatToString(internalFormat));
        return subimage(image, depth, format);
    }

    /**
     * Overwrites the stored image layer with the given image. This texture must
     * already have allocated storage.
     *
     * @param image The image to overwrite the
     * @param xoffset The xoffset to the image layer to overwrite.
     * @param yoffset The yoffset to the image layer to overwrite.
     * @param zoffset The image layer to overwrite.
     * @param format The texture format to store the image as.
     * @return This texture.
     */
    public T subimage(Image image, int xoffset, int yoffset, int zoffset, int format)
    {
        if (image.deleted()) throw new IllegalStateException("Image is deleted.");

        int dataFormat = TexUtil.getBaseFormat(format);
        if (image.bands != TexUtil.getBands(dataFormat))
            throw new IllegalArgumentException("Incompatible format bands.");

        if (xoffset < 0 || xoffset >= this.width)
            throw new IllegalArgumentException("Illegal xoffset specified.");

        if (yoffset < 0 || yoffset >= this.height)
            throw new IllegalArgumentException("Illegal yoffset specified.");

        if (zoffset < 0 || zoffset >= this.depth)
            throw new IllegalArgumentException("Illegal zoffset specified.");

        int primType = TexUtil.getGLPrimitiveType(format);
        int oldID = tempBind();
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
        glTexSubImage3D(target, 0, xoffset, yoffset, zoffset, image.width, image.height, 1, dataFormat, primType, image.buffer);
        internalFormat = format;
        tempUnbind(oldID);
        return getThis();
    }

    /**
     * Overwrites the stored image layer with the given image. This texture must
     * already have allocated storage.
     *
     * @param image The image to overwrite the
     * @param depth The image layer to overwrite.
     */
    public T subimage(Image image, int xoffset, int yoffset, int depth)
    {
        int format = TexUtil.getFormat(image);
        if (format != internalFormat)
            throw new IllegalArgumentException("Illegal format " + TexUtil.formatToString(format) + ", expected " + TexUtil.formatToString(internalFormat));
        return subimage(image, xoffset, yoffset, depth, format);
    }
}
