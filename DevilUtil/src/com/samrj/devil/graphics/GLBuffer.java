package com.samrj.devil.graphics;

import com.samrj.devil.buffer.FloatBuffer;
import org.lwjgl.opengl.GL15;

public final class GLBuffer
{
    private int id, target = -1;
    
    public GLBuffer()
    {
        id = GL15.glGenBuffers();
    }
    
    public GLBuffer(FloatBuffer data, int target, int usage)
    {
        this();
        glBind(target);
        glBufferData(data, usage);
    }
    
    public void glBind(int target)
    {
        if (isDeleted()) throw new IllegalStateException();
        GL15.glBindBuffer(target, id);
        this.target = target;
    }
    
    public void glUnbind()
    {
        if (!isBound()) return;
        GL15.glBindBuffer(target, 0);
        target = -1;
    }
    
    public void glBufferData(FloatBuffer data, int usage)
    {
        if (isDeleted()) throw new IllegalStateException();
        GL15.glBufferData(target, data.get(), usage);
    }
    
    public void glDelete()
    {
        glUnbind();
        GL15.glDeleteBuffers(id);
        target = -1; id = -1;
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