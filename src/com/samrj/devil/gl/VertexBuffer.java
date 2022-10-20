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

import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11C.glGetInteger;
import static org.lwjgl.opengl.GL15C.*;
import static org.lwjgl.system.MemoryUtil.*;

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
    private ByteBuffer vertexBuffer, indexBuffer;
    private int numVertices, numIndices;
    
    //Fields for 'complete' state
    private int vbo, ibo;
    private long debugVRAMUsage;
    
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
    public ByteBuffer newVertexBufferView()
    {
        return viewBuffer(vertexBuffer);
    }

    @Override
    public ByteBuffer newIndexBufferView()
    {
        return viewBuffer(indexBuffer);
    }

    @Override
    public State getState()
    {
        return state;
    }

    @Override
    void onBegin()
    {
        vertexBuffer = memAlloc(maxVertices*vertexSize());
        if (maxIndices > 0) indexBuffer = memAlloc(maxIndices*4);
        
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
        
        vertexBuffer.flip();
        vbo = glGenBuffers();
        int prevBinding = glGetInteger(GL_ARRAY_BUFFER_BINDING);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, prevBinding);
        
        debugVRAMUsage += vertexBuffer.remaining()*8L;
        memFree(vertexBuffer);
        vertexBuffer = null;

        if (maxIndices > 0)
        {
            if (numIndices > 0)
            {
                indexBuffer.flip();
                ibo = glGenBuffers();
                prevBinding = glGetInteger(GL_ELEMENT_ARRAY_BUFFER_BINDING);
                glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo);
                glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW);
                glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, prevBinding);
            }
            
            debugVRAMUsage += indexBuffer.remaining()*8L;
            memFree(indexBuffer);
            indexBuffer = null;
        }
        
        state = State.COMPLETE;
        
        Profiler.addUsedVRAM(debugVRAMUsage);
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
            memFree(vertexBuffer);
            vertexBuffer = null;
            
            if (maxIndices > 0)
            {
                memFree(indexBuffer);
                indexBuffer = null;
            }
        }
        else if (state == State.COMPLETE)
        {
            glDeleteBuffers(vbo);
            if (numIndices > 0) glDeleteBuffers(ibo);
        }
        
        state = State.DELETED;
        
        Profiler.removeUsedVRAM(debugVRAMUsage);
    }
}
