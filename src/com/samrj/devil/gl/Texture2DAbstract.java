package com.samrj.devil.gl;

import com.samrj.devil.graphics.TexUtil;
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
        
        width = image.width;
        height = image.height;
        int baseFormat = TexUtil.getBaseFormat(format);
        int primType = TexUtil.getPrimEnum(image.type);
        
        int oldID = tempBind();
        GL11.nglTexImage2D(target, 0, format, width, height, 0,
                baseFormat, primType, image.address());
        tempUnbind(oldID);
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
        
        int primType = TexUtil.getPrimEnum(TexUtil.getPrimType(format));
        
        int oldID = tempBind();
        GL11.nglTexImage2D(target, 0, format, width, height, 0,
                baseFormat, primType, MemoryUtil.NULL);
        tempUnbind(oldID);
    }
}
