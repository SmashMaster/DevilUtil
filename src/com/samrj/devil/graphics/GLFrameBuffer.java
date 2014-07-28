package com.samrj.devil.graphics;

import static com.samrj.devil.buffer.PublicBuffers.ibuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

public class GLFrameBuffer
{
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
        
        ibuffer.clear();
        ibuffer.put(a);
        GL20.glDrawBuffers(ibuffer.get());
    }
    
    public void glTexture2D(Texture t, int attachment)
    {
        if (isDeleted()) throw new IllegalStateException();
        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, attachment, GL11.GL_TEXTURE_2D, t.id(), 0);
    }
    
    public void glDelete()
    {
        GL30.glDeleteFramebuffers(id);
        id = -1;
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