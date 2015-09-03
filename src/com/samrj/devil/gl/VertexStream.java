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
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class VertexStream extends VertexData
{
    private final int maxVertices, maxIndices;
    private State state;
    
    //Fields for 'ready' state
    private int vboSize, eboSize;
    private Memory vertexBlock, indexBlock;
    private ByteBuffer vertexBuffer, indexBuffer;
    private int glVBO, glEBO;
    private int bufferedVerts, bufferedInds;
    private int uploadedVerts, uploadedInds;
    
    VertexStream(int maxVertices, int maxIndices)
    {
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
        vboSize = maxVertices*getVertexSize();
        vertexBlock = new Memory(vboSize);
        vertexBuffer = vertexBlock.buffer;
        glVBO = GL15.glGenBuffers();
        int prevBinding = GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, glVBO);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vboSize, GL15.GL_STREAM_DRAW);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, prevBinding);
        
        if (maxIndices > 0)
        {
            eboSize = maxIndices*4;
            indexBlock = new Memory(eboSize);
            indexBuffer = indexBlock.buffer;
            glEBO = GL15.glGenBuffers();
            prevBinding = GL11.glGetInteger(GL15.GL_ELEMENT_ARRAY_BUFFER_BINDING);
            GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, glEBO);
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
        if (DGL.currentData() != this) throw new IllegalStateException(
                "Vertex stream must be bound to upload data.");
        
        //Allocate new stores, orphaning the old ones to allow for asynchronous drawing.
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vboSize, GL15.GL_STREAM_DRAW);
        GL15.nglBufferSubData(GL15.GL_ARRAY_BUFFER, 0, bufferedVerts*getVertexSize(), vertexBlock.address);
        
        if (maxIndices > 0)
        {
            GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, eboSize, GL15.GL_STREAM_DRAW);
            GL15.nglBufferSubData(GL15.GL_ELEMENT_ARRAY_BUFFER, 0, bufferedInds*4, indexBlock.address);
        }
        
        uploadedVerts = bufferedVerts;
        uploadedInds = bufferedInds;
        
        clear();
    }

    @Override
    boolean canBind()
    {
        return state == State.READY;
    }

    @Override
    int getVBO()
    {
        return glVBO;
    }

    @Override
    int getEBO()
    {
        return glEBO;
    }

    @Override
    void draw(int mode)
    {
        if (maxIndices <= 0) GL11.glDrawArrays(mode, 0, uploadedVerts);
        else GL11.glDrawElements(mode, uploadedInds, GL11.GL_UNSIGNED_INT, 0);
    }

    @Override
    void onDelete()
    {
        if (state == State.READY)
        {
            vertexBlock.free();
            vertexBlock = null;
            vertexBuffer = null;
            GL15.glDeleteBuffers(glVBO);
            
            if (maxIndices > 0)
            {
                indexBlock.free();
                indexBlock = null;
                indexBuffer = null;
                GL15.glDeleteBuffers(glEBO);
            }
        }
        
        state = State.DELETED;
    }
}
