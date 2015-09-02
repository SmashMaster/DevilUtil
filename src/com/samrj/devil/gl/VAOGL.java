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
final class VAOGL extends VAO
{
    private final int id;
    private boolean deleted;
    
    VAOGL()
    {
        id = GL30.glGenVertexArrays();
    }
    
    private void ensureBound()
    {
        if (deleted) throw new IllegalStateException("VAO deleted.");
        if (DGL.currentVAO() != this) throw new IllegalStateException("VAO not bound.");
    }
    
    @Override
    public void enableVertexAttribArray(int index)
    {
        ensureBound();
        GL20.glEnableVertexAttribArray(index);
    }

    @Override
    public void disableVertexAttribArray(int index)
    {
        ensureBound();
        GL20.glDisableVertexAttribArray(index);
    }

    @Override
    public void vertexAttribPointer(int index, int size, int type, boolean normalized, int stride, long pointerOffset)
    {
        ensureBound();
        GL20.nglVertexAttribPointer(index, size, type, normalized, stride, pointerOffset);
    }

    @Override
    public void bindElementArrayBuffer(int buffer)
    {
        ensureBound();
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, buffer);
    }

    @Override
    void bind()
    {
        if (deleted) throw new IllegalStateException("VAO deleted.");
        GL30.glBindVertexArray(id);
    }

    @Override
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
