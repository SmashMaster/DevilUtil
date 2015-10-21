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
 * Vertex data for streaming vertex data. Suitable for data that is built and
 * rebuilt many times, and uploaded to the GPU as many times as it is drawn.
 * 
 * @author Samuel Johnson (SmashMaster)
 */
public final class VertexStream extends VertexBuilder
{
    private final int maxVertices, maxIndices;
    private State state;
    
    //Fields for 'ready' state
    private int vboSize, eboSize;
    private Memory vertexBlock, indexBlock;
    private ByteBuffer vertexBuffer, indexBuffer;
    private int vbo, ibo;
    private int bufferedVerts, bufferedInds;
    private int uploadedVerts, uploadedInds;
    
    VertexStream(int maxVertices, int maxIndices)
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
        vboSize = maxVertices*vertexSize();
        vertexBlock = new Memory(vboSize);
        vertexBuffer = vertexBlock.buffer;
        vbo = GL15.glGenBuffers();
        int prevBinding = GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vboSize, GL15.GL_STREAM_DRAW);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, prevBinding);
        
        if (maxIndices > 0)
        {
            eboSize = maxIndices*4;
            indexBlock = new Memory(eboSize);
            indexBuffer = indexBlock.buffer;
            ibo = GL15.glGenBuffers();
            prevBinding = GL11.glGetInteger(GL15.GL_ELEMENT_ARRAY_BUFFER_BINDING);
            GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, ibo);
            GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, eboSize, GL15.GL_STREAM_DRAW);
            GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, prevBinding);
        }
        
        state = State.READY;
    }
    
    /**
     * Clears this vertex stream, allowing a new set of vertices to be uploaded.
     * Does not affect any uploaded data.
     */
    public void clear()
    {
        vertexBuffer.rewind();
        bufferedVerts = 0;
        
        if (maxIndices > 0)
        {
            indexBuffer.rewind();
            bufferedInds = 0;
        }
    }

    @Override
    public int vertex()
    {
        ensureState(State.READY);
        if (bufferedVerts >= maxVertices) throw new IllegalStateException(
                "Vertex capacity reached.");
        
        bufferVertex(vertexBuffer);
        return bufferedVerts++;
    }

    @Override
    public void index(int index)
    {
        ensureState(State.READY);
        if (bufferedInds >= maxIndices) throw new IllegalStateException(
                "Index capacity reached.");
        if (index < 0 || index >= bufferedVerts) throw new ArrayIndexOutOfBoundsException();
        
        indexBuffer.putInt(index);
        bufferedInds++;
    }
    
    /**
     * Uploads this vertex data to the GPU and clears the stream, allowing new
     * data to be emitted.
     */
    public void upload()
    {
        ensureState(State.READY);
        
        //Allocate new stores, orphaning the old ones to allow for asynchronous drawing.
        int prevBinding = GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vboSize, GL15.GL_STREAM_DRAW);
        GL15.nglBufferSubData(GL15.GL_ARRAY_BUFFER, 0, bufferedVerts*vertexSize(), vertexBlock.address);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, prevBinding);
        
        if (maxIndices > 0)
        {
            prevBinding = GL11.glGetInteger(GL15.GL_ELEMENT_ARRAY_BUFFER_BINDING);
            GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, ibo);
            GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, eboSize, GL15.GL_STREAM_DRAW);
            GL15.nglBufferSubData(GL15.GL_ELEMENT_ARRAY_BUFFER, 0, bufferedInds*4, indexBlock.address);
            GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, prevBinding);
        }
        
        uploadedVerts = bufferedVerts;
        uploadedInds = bufferedInds;
        
        clear();
    }
    
    @Override
    public int vbo()
    {
        ensureState(State.READY);
        return vbo;
    }

    @Override
    public int ibo()
    {
        ensureState(State.READY);
        return ibo;
    }

    @Override
    public int numVertices()
    {
        return uploadedVerts;
    }

    @Override
    public int numIndices()
    {
        return maxIndices > 0 ? uploadedInds : -1;
    }

    @Override
    void onDelete()
    {
        if (state == State.READY)
        {
            vertexBlock.free();
            vertexBlock = null;
            vertexBuffer = null;
            GL15.glDeleteBuffers(vbo);
            
            if (maxIndices > 0)
            {
                indexBlock.free();
                indexBlock = null;
                indexBuffer = null;
                GL15.glDeleteBuffers(ibo);
            }
        }
        
        state = State.DELETED;
    }
}
