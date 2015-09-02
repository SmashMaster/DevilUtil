package com.samrj.devil.graphics;

import com.samrj.devil.gl.Texture;
import com.samrj.devil.io.Memory.Block;
import static com.samrj.devil.io.Memory.memUtil;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

/**
 * OpenGL FBO wrapper.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class FrameBuffer
{
    public static String glStatusName(int statusEnum)
    {
        switch (statusEnum)
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
    
    private int id, target = -1;
    
    public FrameBuffer()
    {
        id = GL30.glGenFramebuffers();
    }
    
    /**
     * Binds this frame buffer to the given target.
     * 
     * @param target An OpenGL FBO target.
     */
    public void bind(int target)
    {
        if (isDeleted()) throw new IllegalStateException();
        GL30.glBindFramebuffer(target, id);
        this.target = target;
    }
    
    /**
     * Binds this frame buffer to the default FBO target.
     */
    public void bind()
    {
        bind(GL30.GL_FRAMEBUFFER);
    }
    
    /**
     * Unbinds this FBO.
     */
    public void unbind()
    {
        if (!isBound()) return;
        GL30.glBindFramebuffer(target, 0);
        target = -1;
    }
    
    /**
     * Enables the given OpenGL draw buffers for all subsequent draw calls.
     * 
     * @param a An array of draw buffers to use.
     */
    public void drawBuffers(int... a)
    {
        if (isDeleted()) throw new IllegalStateException();
        for (int i=0; i<a.length; i++) if (a[i] < GL30.GL_COLOR_ATTACHMENT0)
            a[i] += GL30.GL_COLOR_ATTACHMENT0;
        
        Block block = memUtil.wrapi(a);
        GL20.glDrawBuffers(block.readUnsafe().asIntBuffer());
        block.free();
    }
    
    /**
     * Attaches the given texture to this frame buffer.
     * 
     * @param texture A texture to attach.
     * @param attachment The frame buffer attachment to use.
     */
    public void texture(Texture texture, int attachment)
    {
        if (isDeleted()) throw new IllegalStateException();
        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, attachment, texture.target, texture.id, 0);
    }
    
    /**
     * Deletes this frame buffer, releasing any associated resources.
     */
    public void delete()
    {
        GL30.glDeleteFramebuffers(id);
        id = -1;
    }
    
    /**
     * @return The OpenGL status for this frame buffer.
     */
    public int getGLStatus()
    {
        if (!isBound()) throw new IllegalStateException();
        return GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER);
    }
    
    /**
     * @return Whether this frame buffer has been deleted.
     */
    public boolean isDeleted()
    {
        return id == -1;
    }
    
    /**
     * @return Whether this frame buffer is currently bound.
     */
    public boolean isBound()
    {
        return target != -1;
    }
    
    @Override
    public String toString()
    {
        return glStatusName(getGLStatus());
    }
}
