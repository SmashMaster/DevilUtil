/*
 * Copyright (c) 2015 Sam Johnson
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.samrj.devil.gl;

import com.samrj.devil.io.Memory;
import java.nio.ByteBuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;

/**
 * Vertex data for unmodifiable vertex data. Suitable for data that is built
 * once on CPU, uploaded to the GPU, then drawn many times.
 * 
 * @author Samuel Johnson (SmashMaster)
 */
public final class VertexBuffer extends VertexBuilder
{
    private final int maxVertices, maxIndices;
    private State state;
    
    //Fields for 'ready' state
    private Memory vertexBlock, indexBlock;
    private ByteBuffer vertexBuffer, indexBuffer;
    private int numVertices, numIndices;
    
    //Fields for 'complete' state
    private int vbo, ibo;
    
    VertexBuffer(int maxVertices, int maxIndices)
    {
        DGL.checkState();
        if (!DGL.getCapabilities().OpenGL20) throw new UnsupportedOperationException(
                "Vertex builders unsupported in OpenGL < 2.0");
        this.maxVertices = maxVertices;
        this.maxIndices = maxIndices;
        state = State.NEW;
    }
    
    @Override
    public State getState()
    {
        return state;
    }

    @Override
    void onBegin()
    {
        vertexBlock = new Memory(maxVertices*vertexSize());
        vertexBuffer = vertexBlock.buffer;
        
        if (maxIndices > 0)
        {
            indexBlock = new Memory(maxIndices*4);
            indexBuffer = indexBlock.buffer;
        }
        
        state = State.READY;
    }
    
    @Override
    public int vertex()
    {
        ensureState(State.READY);
        if (numVertices >= maxVertices) throw new IllegalStateException(
                "Vertex capacity reached.");
        
        bufferVertex(vertexBuffer);
        return numVertices++;
    }

    @Override
    public void index(int index)
    {
        ensureState(State.READY);
        if (numIndices >= maxIndices) throw new IllegalStateException(
                "Index capacity reached.");
        if (index < 0 || index >= numVertices) throw new ArrayIndexOutOfBoundsException();
        
        indexBuffer.putInt(index);
        numIndices++;
    }
    
    /**
     * Completes this vertex buffer, uploading its vertex data to the GPU and
     * freeing local resources.
     */
    public void end()
    {
        ensureState(State.READY);
        if (numVertices <= 0) throw new IllegalStateException("No vertices emitted.");
        
        vbo = GL15.glGenBuffers();
        int prevBinding = GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL15.nglBufferData(GL15.GL_ARRAY_BUFFER, numVertices*vertexSize(), vertexBlock.address, GL15.GL_STATIC_DRAW);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, prevBinding);
        
        vertexBlock.free();
        vertexBlock = null;
        vertexBuffer = null;

        if (maxIndices > 0)
        {
            if (numIndices > 0)
            {
                ibo = GL15.glGenBuffers();
                prevBinding = GL11.glGetInteger(GL15.GL_ELEMENT_ARRAY_BUFFER_BINDING);
                GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, ibo);
                GL15.nglBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, numIndices*4, indexBlock.address, GL15.GL_STATIC_DRAW);
                GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, prevBinding);
            }
            
            indexBlock.free();
            indexBlock = null;
            indexBuffer = null;
        }
        
        state = State.COMPLETE;
    }
    
    @Override
    public int vbo()
    {
        ensureState(State.COMPLETE);
        return vbo;
    }

    @Override
    public int ibo()
    {
        ensureState(State.COMPLETE);
        return ibo;
    }

    @Override
    public int numVertices()
    {
        return numVertices;
    }

    @Override
    public int numIndices()
    {
        return maxIndices > 0 ? numIndices : -1;
    }

    @Override
    void onDelete()
    {
        if (state == State.READY)
        {
            vertexBlock.free();
            vertexBlock = null;
            vertexBuffer = null;
            
            if (maxIndices > 0)
            {
                indexBlock.free();
                indexBlock = null;
                indexBuffer = null;
            }
        }
        else if (state == State.COMPLETE)
        {
            GL15.glDeleteBuffers(vbo);
            if (numIndices > 0) GL15.glDeleteBuffers(ibo);
        }
        
        state = State.DELETED;
    }
}
