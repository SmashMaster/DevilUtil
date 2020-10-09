/*
 * Copyright (c) 2020 Sam Johnson
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
public final class GrowableVertexStream extends VertexBuilder
{
    private static final int INITIAL_MAX_VERTS = 4;
    private static final int INITIAL_MAX_INDICES = 4;
    
    private final boolean indicesEnabled;
    
    private State state;
    
    //Fields for 'ready' state
    private ByteBuffer vertexBuffer, indexBuffer;
    private int vbo, ebo;
    private int bufferedVerts, bufferedInds;
    private int uploadedVerts, uploadedInds;
    
    GrowableVertexStream(boolean enableIndices)
    {
        indicesEnabled = enableIndices;
        DGL.checkState();
        if (!DGL.getCapabilities().OpenGL20) throw new UnsupportedOperationException(
                "Vertex builders unsupported in OpenGL < 2.0");
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
        vertexBuffer = memAlloc(INITIAL_MAX_VERTS*vertexSize());
        vbo = glGenBuffers();
        
        if (indicesEnabled)
        {
            indexBuffer = memAlloc(INITIAL_MAX_INDICES*4);
            ebo = glGenBuffers();
        }
        
        state = State.READY;
    }
    
    /**
     * Clears this vertex stream, allowing a new set of vertices to be uploaded.
     * Does not affect any uploaded data.
     */
    public void clear()
    {
        vertexBuffer.clear();
        bufferedVerts = 0;
        
        if (indicesEnabled)
        {
            indexBuffer.clear();
            bufferedInds = 0;
        }
    }
    
    private ByteBuffer growBuffer(ByteBuffer buffer, int bytes)
    {
        int newPosition = buffer.position() + bytes;
        if (newPosition > buffer.limit())
        {
            ByteBuffer newBuffer = memAlloc(buffer.capacity()*2);
            buffer.flip();
            memCopy(buffer, newBuffer);
            memFree(buffer);
            return newBuffer;
        }
        return buffer;
    }

    @Override
    public int vertex()
    {
        ensureState(State.READY);
        vertexBuffer = growBuffer(vertexBuffer, vertexSize());
        bufferVertex(vertexBuffer);
        return bufferedVerts++;
    }

    @Override
    public void index(int index)
    {
        ensureState(State.READY);
        if (index < 0 || index >= bufferedVerts) throw new ArrayIndexOutOfBoundsException();
        indexBuffer = growBuffer(indexBuffer, 4);
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
        vertexBuffer.flip();
        int prevBinding = glGetInteger(GL_ARRAY_BUFFER_BINDING);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STREAM_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, prevBinding);
        
        if (indicesEnabled)
        {
            indexBuffer.flip();
            prevBinding = glGetInteger(GL_ELEMENT_ARRAY_BUFFER_BINDING);
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STREAM_DRAW);
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
        return uploadedInds;
    }

    @Override
    void onDelete()
    {
        if (state == State.READY)
        {
            memFree(vertexBuffer);
            vertexBuffer = null;
            glDeleteBuffers(vbo);
            
            if (indicesEnabled)
            {
                memFree(indexBuffer);
                indexBuffer = null;
                glDeleteBuffers(ebo);
            }
        }
        
        state = State.DELETED;
    }
}
