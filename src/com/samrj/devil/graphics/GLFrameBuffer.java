package com.samrj.devil.graphics;

import com.samrj.devil.io.Memory.Block;
import static com.samrj.devil.io.Memory.memUtil;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class GLFrameBuffer
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
    
    public GLFrameBuffer()
    {
        id = GL30.glGenFramebuffers();
    }
    
    public void glBind(int target)
    {
        if (isDeleted()) throw new IllegalStateException();
        GL30.glBindFramebuffer(target, id);
        this.target = target;
    }
    
    public void glBind()
    {
        glBind(GL30.GL_FRAMEBUFFER);
    }
    
    public void glUnbind()
    {
        if (!isBound()) return;
        GL30.glBindFramebuffer(target, 0);
        target = -1;
    }
    
    public void glDrawBuffers(int... a)
    {
        if (isDeleted()) throw new IllegalStateException();
        for (int i=0; i<a.length; i++) if (a[i] < GL30.GL_COLOR_ATTACHMENT0)
            a[i] += GL30.GL_COLOR_ATTACHMENT0;
        
        Block block = memUtil.wrapi(a);
        GL20.glDrawBuffers(block.readUnsafe().asIntBuffer());
        block.free();
    }
    
    public void glTexture2D(GLTexture2D t, int attachment)
    {
        if (isDeleted()) throw new IllegalStateException();
        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, attachment, GL11.GL_TEXTURE_2D, t.id(), 0);
    }
    
    public void glDelete()
    {
        GL30.glDeleteFramebuffers(id);
        id = -1;
    }
    
    public int glGetStatus()
    {
        if (!isBound()) throw new IllegalStateException();
        return GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER);
    }
    
    public String glGetStatusString()
    {
        return glStatusName(glGetStatus());
    }
    
    public boolean isDeleted()
    {
        return id == -1;
    }
    
    public boolean isBound()
    {
        return target != -1;
    }
}
