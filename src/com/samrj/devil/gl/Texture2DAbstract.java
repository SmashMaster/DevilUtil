package com.samrj.devil.gl;

import com.samrj.devil.graphics.TexUtil;
import com.samrj.devil.math.Util.PrimType;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;

/**
 * Abstract class for 2D textures.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
abstract class Texture2DAbstract extends Texture
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
     */
    public void image(int width, int height, int format)
    {
        if (width <= 0 || height <= 0) throw new IllegalArgumentException("Illegal image dimensions.");
        
        int baseFormat = TexUtil.getBaseFormat(format);
        if (baseFormat == -1) throw new IllegalArgumentException("Illegal image format.");
        
        this.width = width;
        this.height = height;
        PrimType primType = TexUtil.getPrimType(format);
        int glPrimType = primType != null ? TexUtil.getGLPrim(primType) : GL11.GL_UNSIGNED_BYTE;
        
        int oldID = tempBind();
        GL11.nglTexImage2D(target, 0, format, width, height, 0,
                baseFormat, glPrimType, MemoryUtil.NULL);
        tempUnbind(oldID);
    }
    
    /**
     * Allocates space on the GPU for the image, and then uploads it, linking it
     * to this texture. After calling, the image may be safely deleted from
     * memory. Any previous image data associated with this texture is released.
     * 
     * @param image The image to upload to the GPU.
     * @param format The texture format to store the image as.
     */
    public void image(Image image, int format)
    {
        if (image.deleted()) throw new IllegalStateException("Image is deleted.");
        
        int dataFormat = TexUtil.getBaseFormat(format);
        if (image.bands != TexUtil.getBands(dataFormat))
            throw new IllegalArgumentException("Incompatible format bands.");
        
        width = image.width;
        height = image.height;
        int primType = TexUtil.getGLPrim(image.type);
        
        int oldID = tempBind();
        GL11.nglTexImage2D(target, 0, format, width, height, 0,
                dataFormat, primType, image.address());
        tempUnbind(oldID);
    }
    
    /**
     * Allocates space on the GPU for the image, and then uploads it, linking it
     * to this texture. After calling, the image may be safely deleted from
     * memory. Any previous image data associated with this texture is released.
     * 
     * @param image The image to upload to the GPU.
     */
    public void image(Image image)
    {
        int format = TexUtil.getFormat(image);
        if (format == -1) throw new IllegalArgumentException("Illegal image format.");
        image(image, format);
    }
    
    /**
     * Overwrites the stored image for this texture with the given image. This
     * texture must already have allocated storage.
     * 
     * @param image The image to upload to the GPU.
     * @param format The texture format to store the image as.
     */
    public void subimage(Image image, int format)
    {
        if (image.deleted()) throw new IllegalStateException("Image is deleted.");
        
        int dataFormat = TexUtil.getBaseFormat(format);
        if (image.bands != TexUtil.getBands(dataFormat))
            throw new IllegalArgumentException("Incompatible format bands.");
        
        if (image.width != width || image.height != height)
            throw new IllegalArgumentException("Incompatible image dimensions.");
        
        int primType = TexUtil.getGLPrim(image.type);
        int oldID = tempBind();
        GL11.nglTexSubImage2D(target, 0, 0, 0, width, height,
                dataFormat, primType, image.address());
        tempUnbind(oldID);
    }
    
    /**
     * Overwrites the stored image for this texture with the given image. This
     * texture must already have allocated storage.
     * 
     * @param image The image to upload to the GPU.
     */
    public void subimage(Image image)
    {
        int format = TexUtil.getFormat(image);
        if (format == -1) throw new IllegalArgumentException("Illegal image format.");
        subimage(image, format);
    }
}
