package com.samrj.devil.gl;

import com.samrj.devil.graphics.TexUtil;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL30C.*;

/**
 * OpenGL render buffer wrapper.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2019 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class RBO extends DGLObj
{
    public final int id;
    private boolean deleted;
    private long vramUsage;
    
    RBO()
    {
        DGL.checkState();
        if (!DGL.getCapabilities().OpenGL30) throw new UnsupportedOperationException(
                "Render buffers unsupported in OpenGL < 3.0");
        id = glGenRenderbuffers();
    }
    
    /**
     * @return Whether this render buffer is bound.
     */
    public final boolean isBound()
    {
        return !deleted && glGetInteger(GL_RENDERBUFFER_BINDING) == id;
    }
    
    final int tempBind()
    {
        int oldID = glGetInteger(GL_RENDERBUFFER_BINDING);
        if (oldID != id) glBindRenderbuffer(GL_RENDERBUFFER, id);
        return oldID;
    }
    
    final void tempUnbind(int oldID)
    {
        if (oldID == id) return;
        glBindRenderbuffer(GL_RENDERBUFFER, oldID);
    }
    
    /**
     * Binds this render buffer.
     */
    public final void bind()
    {
        if (deleted) throw new IllegalStateException("Cannot bind deleted render buffer.");
        glBindRenderbuffer(GL_RENDERBUFFER, id);
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
        glRenderbufferStorage(GL_RENDERBUFFER, format, width, height);
        tempUnbind(oldID);
        
        long newVRAM = TexUtil.getBits(format)*width*height;
        Profiler.addUsedVRAM(newVRAM - vramUsage);
        vramUsage = newVRAM;
    }

    @Override
    void delete()
    {
        Profiler.removeUsedVRAM(vramUsage);
        vramUsage = 0;
        glDeleteRenderbuffers(id);
        deleted = true;
    }
}
