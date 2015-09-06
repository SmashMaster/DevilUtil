package com.samrj.devil.gl;

import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

/**
 * OpenGL VAO wrapper.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class VAO extends DGLObj
{
    private final int id;
    private boolean deleted;
    
    VAO()
    {
        id = GL30.glGenVertexArrays();
    }
    
    private void ensureBound()
    {
        if (deleted) throw new IllegalStateException("VAO deleted.");
        if (DGL.currentVAO() != this) throw new IllegalStateException("VAO not bound.");
    }
    
    public void enableVertexAttribArray(int index)
    {
        ensureBound();
        GL20.glEnableVertexAttribArray(index);
    }

    public void disableVertexAttribArray(int index)
    {
        ensureBound();
        GL20.glDisableVertexAttribArray(index);
    }
    
    public void vertexAttribPointer(int index, int size, int type, boolean normalized, int stride, long pointerOffset)
    {
        ensureBound();
        GL20.nglVertexAttribPointer(index, size, type, normalized, stride, pointerOffset);
    }

    public void bindElementArrayBuffer(int buffer)
    {
        ensureBound();
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, buffer);
    }

    void bind()
    {
        if (deleted) throw new IllegalStateException("VAO deleted.");
        GL30.glBindVertexArray(id);
    }

    void unbind()
    {
        if (deleted) throw new IllegalStateException("VAO deleted.");
        GL30.glBindVertexArray(0);
    }

    @Override
    void delete()
    {
        if (deleted) return;
        GL30.glDeleteVertexArrays(id);
        deleted = true;
    }
}
