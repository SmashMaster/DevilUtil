package com.samrj.devil.gl;

import com.samrj.devil.io.Memory;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

/**
 * OpenGL frame buffer wrapper. Requires OpenGL 3.0.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class FBO extends DGLObj
{
    private static String statusName(int status)
    {
        switch (status)
        {
            case GL30.GL_FRAMEBUFFER_COMPLETE​:
                return "GL_FRAMEBUFFER_COMPLETE​";
            case GL30.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT:
                return "GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT";
            case GL30.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT:
                return "GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT";
            case GL30.GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER:
                return "GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER";
            case GL30.GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER:
                return "GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER";
            case GL30.GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE:
                return "GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE";
            default: return null;
        }
    }
    
    public final int id;
    private boolean deleted;
    
    FBO()
    {
        id = GL30.glGenFramebuffers();
    }
    
    private void ensureBound()
    {
        if (deleted) throw new IllegalStateException("FBO deleted.");
        if (DGL.currentFBO() != this) throw new IllegalStateException("FBO not bound.");
    }
    
    void bind(int target)
    {
        GL30.glBindFramebuffer(target, id);
    }
    
    /**
     * Attaches the given texture to this frame buffer. Must be bound.
     * 
     * @param texture The texture to attach.
     * @param attachment The attachment type.
     */
    public void texture2D(Texture2D texture, int attachment)
    {
        ensureBound();
        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, attachment, texture.target, texture.id, 0);
    }
    
    /**
     * Attaches a layer of the given 3D texture to this frame buffer. This frame
     * buffer must be bound.
     * 
     * @param texture The 3D texture to attach.
     * @param layer The layer of the given texture to attach.
     * @param attachment The attachment type.
     */
    public void texture3D(Texture3D texture, int layer, int attachment)
    {
        ensureBound();
        if (layer < 0 || layer >= texture.getDepth()) throw new ArrayIndexOutOfBoundsException();
        GL30.glFramebufferTexture3D(GL30.GL_FRAMEBUFFER, attachment, texture.target, texture.id, 0, layer);
    }
    
    /**
     * Attaches a layer of the given 2D texture array to this frame buffer. This
     * frame buffer must be bound.
     * 
     * @param texture The 2D texture array to attach.
     * @param layer The layer of the given texture array to attach.
     * @param attachment The attachment type.
     */
    public void textureLayer(Texture2DArray texture, int layer, int attachment)
    {
        ensureBound();
        if (layer < 0 || layer >= texture.getDepth()) throw new ArrayIndexOutOfBoundsException();
        GL30.glFramebufferTextureLayer(GL30.GL_FRAMEBUFFER, attachment, texture.id, 0, layer);
    }
    
    /**
     * Enables each of the given color buffer attachments for this frame buffer.
     * 
     * @param a An array of draw buffers.
     */
    public void drawBuffers(int... a)
    {
        ensureBound();
        Memory mem = Memory.wrapi(a);
        GL20.nglDrawBuffers(a.length, mem.address);
        mem.free();
    }
    
    /**
     * Specified which color buffer to read from for this frame buffer.
     * 
     * @param buffer The buffer to read from.
     */
    public void readBuffer(int buffer)
    {
        ensureBound();
        GL11.glReadBuffer(buffer);
    }
    
    /**
     * @return The status of this frame buffer, as a string.
     */
    public String getStatus()
    {
        ensureBound();
        int status = GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER);
        return statusName(status);
    }
    
    @Override
    void delete()
    {
        if (deleted) return;
        if (DGL.currentReadFBO() == this) DGL.bindFBO(null, GL30.GL_READ_FRAMEBUFFER);
        if (DGL.currentDrawFBO() == this) DGL.bindFBO(null, GL30.GL_DRAW_FRAMEBUFFER);
        GL30.glDeleteFramebuffers(id);
    }
}
