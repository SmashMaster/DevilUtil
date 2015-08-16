package com.samrj.devilgl;

import com.samrj.devil.util.IntSet;
import com.samrj.devil.util.SparseArray;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

/**
 * Vertex array object wrapper, emulates VAO for OpenGL <3.0
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilGL/blob/master/LICENSE
 */
abstract class VertexArrayObject
{
    static VertexArrayObject gen()
    {
        if (DGL.getCapabilities().OpenGL30) return new GLVAO();
        else return new VAO();
    }
    
    void ensureBound()
    {
        if (DGL.getBoundVAO() != this) throw new IllegalStateException("VAO not bound.");
    }
    
    abstract void enableVertexAttribArray(int index);
    abstract void disableVertexAttribArray(int index);
    abstract void vertexAttribPointer(int index, int size, int type, boolean normalized, int stride, long pointerOffset);
    abstract void bindElementArrayBuffer(int buffer);
    abstract void bind();
    abstract void unbind();
    abstract void delete();
    
    static class GLVAO extends VertexArrayObject
    {
        private final int vao;
        
        private GLVAO()
        {
            vao = GL30.glGenVertexArrays();
        }

        @Override
        void enableVertexAttribArray(int index)
        {
            ensureBound();
            GL20.glEnableVertexAttribArray(index);
        }

        @Override
        void disableVertexAttribArray(int index)
        {
            ensureBound();
            GL20.glDisableVertexAttribArray(index);
        }

        @Override
        void vertexAttribPointer(int index, int size, int type, boolean normalized, int stride, long pointerOffset)
        {
            ensureBound();
            GL20.nglVertexAttribPointer(index, size, type, normalized, stride, pointerOffset);
        }

        @Override
        void bindElementArrayBuffer(int buffer)
        {
            ensureBound();
            GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, buffer);
        }

        @Override
        void bind()
        {
            VertexArrayObject bound = DGL.getBoundVAO();
            if (bound == this) return;
            if (bound != null) bound.unbind();
            DGL.setBoundVAO(this);
            GL30.glBindVertexArray(vao);
        }

        @Override
        void unbind()
        {
            if (DGL.getBoundVAO() != this) return;
            DGL.setBoundVAO(null);
            GL30.glBindVertexArray(0);
        }

        @Override
        void delete()
        {
            unbind();
            GL30.glDeleteVertexArrays(vao);
        }
    }
    
    static class VAO extends VertexArrayObject
    {
        private final IntSet enabledArrays;
        private final SparseArray<VertexAttribPointer> attribPointers;
        private int elementArrayBuffer;
        
        private VAO()
        {
            enabledArrays = new IntSet();
            attribPointers = new SparseArray<>();
        }
        
        @Override
        void enableVertexAttribArray(int index)
        {
            ensureBound();
            enabledArrays.add(index);
            GL20.glEnableVertexAttribArray(index);
        }

        @Override
        void disableVertexAttribArray(int index)
        {
            ensureBound();
            enabledArrays.remove(index);
            GL20.glDisableVertexAttribArray(index);
        }

        @Override
        void vertexAttribPointer(int index, int size, int type, boolean normalized, int stride, long pointerOffset)
        {
            ensureBound();
            attribPointers.put(index, new VertexAttribPointer(index, size, type, normalized, stride, pointerOffset));
            GL20.nglVertexAttribPointer(index, size, type, normalized, stride, pointerOffset);
        }

        @Override
        void bindElementArrayBuffer(int buffer)
        {
            ensureBound();
            elementArrayBuffer = buffer;
            GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, buffer);
        }

        @Override
        void bind()
        {
            VertexArrayObject bound = DGL.getBoundVAO();
            if (bound == this) return;
            if (bound != null) bound.unbind();
            DGL.setBoundVAO(this);
            
            for (int i=0; i<enabledArrays.size(); i++)
                GL20.glEnableVertexAttribArray(enabledArrays.get(i));
            
            for (VertexAttribPointer pointer : attribPointers) pointer.gl();
            
            GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, elementArrayBuffer);
        }

        @Override
        void unbind()
        {
            if (DGL.getBoundVAO() != this) return;
            DGL.setBoundVAO(null);
            
            for (int i=0; i<enabledArrays.size(); i++)
                GL20.glDisableVertexAttribArray(enabledArrays.get(i));
            
            GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
        }

        @Override
        void delete()
        {
            unbind();
            enabledArrays.clear();
            attribPointers.clear();
            elementArrayBuffer = 0;
        }
    }
    
    private static final class VertexAttribPointer
    {
        private final int index, size, type;
        private final boolean normalized;
        private final int stride;
        private final long pointerOffset;
        
        private VertexAttribPointer(int index, int size, int type, boolean normalized, int stride, long pointerOffset)
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
