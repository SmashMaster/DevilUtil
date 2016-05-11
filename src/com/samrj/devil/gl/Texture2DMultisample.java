package com.samrj.devil.gl;

import org.lwjgl.opengl.GL32;

/**
 * OpenGL multisampled 2D texture class.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2016 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Texture2DMultisample extends Texture<Texture2DMultisample>
{
    private int width, height;
    
    Texture2DMultisample()
    {
        super(GL32.GL_TEXTURE_2D_MULTISAMPLE, GL32.GL_TEXTURE_BINDING_2D_MULTISAMPLE);
        width = -1;
        height = -1;
    }
    
    @Override
    Texture2DMultisample getThis()
    {
        return this;
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
     * @param samples
     * @param format The format of the image.
     * @param fixedSampleLocations
     * @return This texture.
     */
    public Texture2DMultisample image(int width, int height, int samples, int format, boolean fixedSampleLocations)
    {
        if (width <= 0 || height <= 0) throw new IllegalArgumentException("Illegal image dimensions.");
        
        this.width = width;
        this.height = height;
        
        int oldID = tempBind();
        GL32.glTexImage2DMultisample(target, samples, format, width, height, fixedSampleLocations);
        tempUnbind(oldID);
        return getThis();
    }
}
