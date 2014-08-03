package com.samrj.devil.graphics;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class GLVertexArray
{
    public static GLVertexArray glGetCurrentVertexArray()
    {
        int id = GL11.glGetInteger(GL30.GL_VERTEX_ARRAY_BINDING);
        if (id == 0) return null;
        return new GLVertexArray(id);
    }
    
    private int id;
    
    private GLVertexArray(int id)
    {
        this.id = id;
    }
    
    public GLVertexArray()
    {
        id = GL30.glGenVertexArrays();
    }
    
    public void glBind()
    {
        GL30.glBindVertexArray(id);
    }
    
    public void glUnbind()
    {
        GL30.glBindVertexArray(0);
    }
    
    public boolean glIsBound()
    {
        if (id == -1) return false;
        return id == GL11.glGetInteger(GL30.GL_VERTEX_ARRAY_BINDING);
    }
    
    public void glDelete()
    {
        GL30.glDeleteVertexArrays(id);
        id = -1;
    }
    
    private int getAttLoc(String name)
    {
        GLShader shader = GLShader.getCurrentShader();
        if (shader == null) throw new IllegalStateException("No shader currently in use.");
        return shader.glGetAttribLocation(name);
    }
    
    public void glVertexAttribPointer(String name, int size, int type, boolean normalized, int stride, int bufferOffset)
    {
        if (!glIsBound()) throw new IllegalStateException();
        GL20.glVertexAttribPointer(getAttLoc(name), size, type, normalized, stride, bufferOffset);
    }
    
    public void glEnableVertexAttribArray(String name)
    {
        if (!glIsBound()) throw new IllegalStateException();
        GL20.glEnableVertexAttribArray(getAttLoc(name));
    }
    
    public void glDisableVertexAttribArray(String name)
    {
        if (!glIsBound()) throw new IllegalStateException();
        GL20.glDisableVertexAttribArray(getAttLoc(name));
    }
}