package com.samrj.devil.gl;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

/**
 * OpenGL frame buffer wrapper. Requires OpenGL 3.0.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class FBO
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
     * @return The status of this frame buffer, as a string.
     */
    public String getStatus()
    {
        ensureBound();
        int status = GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER);
        return statusName(status);
    }
    
    void delete()
    {
        if (deleted) return;
        GL30.glDeleteFramebuffers(id);
    }
}
