package com.samrj.devil.gl;

import com.samrj.devil.util.IntSet;
import com.samrj.devil.util.SparseArray;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;

/**
 * OpenGL VAO emulator for versions < 3.0.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
final class VAODGL extends VAO
{
    private final IntSet enabledArrays;
    private final SparseArray<Pointer> attribPointers;
    private int elementArrayBuffer;
    private boolean deleted;
    
    VAODGL()
    {
        enabledArrays = new IntSet();
        attribPointers = new SparseArray<>();
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
        enabledArrays.add(index);
        GL20.glEnableVertexAttribArray(index);
    }
    
    @Override
    public void disableVertexAttribArray(int index)
    {
        ensureBound();
        enabledArrays.remove(index);
        GL20.glDisableVertexAttribArray(index);
    }
    
    @Override
    public void vertexAttribPointer(int index, int size, int type, boolean normalized, int stride, long pointerOffset)
    {
        ensureBound();
        Pointer pointer = new Pointer(index, size, type, normalized, stride, pointerOffset);
        attribPointers.put(index, pointer);
        pointer.gl();
    }
    
    @Override
    public void bindElementArrayBuffer(int buffer)
    {
        ensureBound();
        elementArrayBuffer = buffer;
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, buffer);
    }
    
    @Override
    void bind()
    {
        if (deleted) throw new IllegalStateException("VAO deleted.");
        
        for (int i=0; i<enabledArrays.size(); i++)
            GL20.glEnableVertexAttribArray(enabledArrays.get(i));
        
        for (Pointer pointer : attribPointers) pointer.gl();

        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, elementArrayBuffer);
    }
    
    @Override
    void unbind()
    {
        if (deleted) throw new IllegalStateException("VAO deleted.");
        
        for (int i=0; i<enabledArrays.size(); i++)
            GL20.glDisableVertexAttribArray(enabledArrays.get(i));
            
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
    }
    
    @Override
    void delete()
    {
        if (deleted) return;
        enabledArrays.clear();
        attribPointers.clear();
        elementArrayBuffer = 0;
        deleted = true;
    }
    
    private static final class Pointer
    {
        private final int index, size, type;
        private final boolean normalized;
        private final int stride;
        private final long pointerOffset;
        
        private Pointer(int index, int size, int type, boolean normalized, int stride, long pointerOffset)
        {
            this.index = index;
            this.size = size;
            this.type = type;
            this.normalized = normalized;
            this.stride = stride;
            this.pointerOffset = pointerOffset;
        }
        
        private void gl()
        {
            GL20.glVertexAttribPointer(index, size, type, normalized, stride, pointerOffset);
        }
    }
}
