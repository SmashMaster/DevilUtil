package com.samrj.devil.gl;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

/**
 * OpenGL render buffer wrapper.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class RBO extends DGLObj
{
    public final int id;
    private boolean deleted;
    
    RBO()
    {
        DGL.checkState();
        if (!DGL.getCapabilities().OpenGL30) throw new UnsupportedOperationException(
                "Render buffers unsupported in OpenGL < 3.0");
        id = GL30.glGenRenderbuffers();
    }
    
    /**
     * @return Whether this render buffer is bound.
     */
    public final boolean isBound()
    {
        return !deleted && GL11.glGetInteger(GL30.GL_RENDERBUFFER_BINDING) == id;
    }
    
    final int tempBind()
    {
        int oldID = GL11.glGetInteger(GL30.GL_RENDERBUFFER_BINDING);
        if (oldID != id) GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, id);
        return oldID;
    }
    
    final void tempUnbind(int oldID)
    {
        if (oldID == id) return;
        GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, oldID);
    }
    
    /**
     * Binds this render buffer.
     */
    public final void bind()
    {
        if (deleted) throw new IllegalStateException("Cannot bind deleted render buffer.");
        GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, id);
    }
    
    /**
     * Allocates space on the GPU for an image of the given size and format.
     * 
     * @param width The width of the image.
     * @param height The height of the image.
     * @param format The format of the image.
     */
    public final void storage(int width, int height, int format)
    {
        if (width <= 0 || height <= 0) throw new IllegalArgumentException("Illegal image dimensions.");
        
        int oldID = tempBind();
        GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, format, width, height);
        tempUnbind(oldID);
    }

    @Override
    void delete()
    {
        GL30.glDeleteRenderbuffers(id);
        deleted = true;
    }
}
