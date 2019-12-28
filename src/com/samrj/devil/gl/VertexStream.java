/*
 * Copyright (c) 2019 Sam Johnson
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

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL15C.*;
import static org.lwjgl.system.MemoryUtil.*;

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
    private ByteBuffer vertexBuffer, indexBuffer;
    private int vbo, ebo;
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
        vertexBuffer = memAlloc(vboSize);
        vbo = glGenBuffers();
        int prevBinding = glGetInteger(GL_ARRAY_BUFFER_BINDING);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vboSize, GL_STREAM_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, prevBinding);
        
        if (maxIndices > 0)
        {
            eboSize = maxIndices*4;
            indexBuffer = memAlloc(eboSize);
            ebo = glGenBuffers();
            prevBinding = glGetInteger(GL_ELEMENT_ARRAY_BUFFER_BINDING);
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, eboSize, GL_STREAM_DRAW);
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, prevBinding);
        }
        
        state = State.READY;
        
        Profiler.addUsedVRAM(vboSize*8L);
        Profiler.addUsedVRAM(eboSize*8L);
    }
    
    /**
     * Clears this vertex stream, allowing a new set of vertices to be uploaded.
     * Does not affect any uploaded data.
     */
    public void clear()
    {
        vertexBuffer.clear();
        bufferedVerts = 0;
        
        if (maxIndices > 0)
        {
            indexBuffer.clear();
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
     * Updates a single vertex using the current attribute values. If the vertex
     * is uploaded, it is also updated on the GPU.
     */
    public void updateVertex(int index)
    {
        ensureState(State.READY);
        
        if (index < 0 || index >= bufferedVerts) throw new ArrayIndexOutOfBoundsException();
        int curOffset = vertexBuffer.position();
        int offset = vertexSize()*index;
        vertexBuffer.position(offset);
        bufferVertex(vertexBuffer);
        vertexBuffer.position(curOffset);
        
        if (index < uploadedVerts)
        {
            int prevBinding = glGetInteger(GL_ARRAY_BUFFER_BINDING);
            glBindBuffer(GL_ARRAY_BUFFER, vbo);
            nglBufferSubData(GL_ARRAY_BUFFER, offset, vertexSize(), memAddress0(vertexBuffer) + offset);
            glBindBuffer(GL_ARRAY_BUFFER, prevBinding);
        }
    }
    
    /**
     * Uploads any buffered vertices that have not yet been sent to the GPU.
     * Much faster than uploading the entire stream for a few new vertices.
     */
    public void uploadNew()
    {
        if (uploadedVerts < bufferedVerts)
        {
            int numNew = bufferedVerts - uploadedVerts;
            int size = numNew*vertexSize();
            int offset = uploadedVerts*vertexSize();
            
            int prevBinding = glGetInteger(GL_ARRAY_BUFFER_BINDING);
            glBindBuffer(GL_ARRAY_BUFFER, vbo);
            nglBufferSubData(GL_ARRAY_BUFFER, offset, size, memAddress0(vertexBuffer) + offset);
            glBindBuffer(GL_ARRAY_BUFFER, prevBinding);
            
            uploadedVerts = bufferedVerts;
        }
        
        if (uploadedInds < bufferedInds)
        {
            int numNew = bufferedInds - uploadedInds;
            int size = numNew*4;
            int offset = uploadedVerts*4;
            
            int prevBinding = glGetInteger(GL_ELEMENT_ARRAY_BUFFER_BINDING);
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
            nglBufferSubData(GL_ELEMENT_ARRAY_BUFFER, offset, size, memAddress0(vertexBuffer) + offset);
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, prevBinding);
            
            uploadedInds = bufferedInds;
        }
    }
    
    /**
     * Uploads this vertex data to the GPU and clears the stream, allowing new
     * data to be emitted.
     */
    public void upload()
    {
        ensureState(State.READY);
        
        //Allocate new stores, orphaning the old ones to allow for asynchronous drawing.
        vertexBuffer.flip();
        int prevBinding = glGetInteger(GL_ARRAY_BUFFER_BINDING);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        nglBufferData(GL_ARRAY_BUFFER, vboSize, NULL, GL_STREAM_DRAW);
        glBufferSubData(GL_ARRAY_BUFFER, 0, vertexBuffer);
        glBindBuffer(GL_ARRAY_BUFFER, prevBinding);
        
        if (maxIndices > 0)
        {
            indexBuffer.flip();
            prevBinding = glGetInteger(GL_ELEMENT_ARRAY_BUFFER_BINDING);
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
            nglBufferData(GL_ELEMENT_ARRAY_BUFFER, eboSize, NULL, GL_STREAM_DRAW);
            glBufferSubData(GL_ELEMENT_ARRAY_BUFFER, 0, indexBuffer);
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, prevBinding);
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
        return ebo;
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
            memFree(vertexBuffer);
            vertexBuffer = null;
            glDeleteBuffers(vbo);
            
            if (maxIndices > 0)
            {
                memFree(indexBuffer);
                indexBuffer = null;
                glDeleteBuffers(ebo);
            }
        }
        
        state = State.DELETED;
        
        Profiler.removeUsedVRAM(vboSize*8L);
        Profiler.removeUsedVRAM(eboSize*8L);
    }
}
