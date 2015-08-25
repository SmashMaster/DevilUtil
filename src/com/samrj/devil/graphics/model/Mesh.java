package com.samrj.devil.graphics.model;

import com.samrj.devil.io.BufferUtil;
import com.samrj.devil.io.Memory;
import com.samrj.devil.io.Memory.Block;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * DevilModel mesh.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Mesh
{
    public final String name;
    public final boolean hasTangents, hasUVs, hasVertexColors;
    public final String[] textures;
    public final int numVertexGroups;
    
    public final int numVertices;
    public final int numTriangles;
    private final Memory memory;
    private Block vertexBlock, indexBlock;
    private ByteBuffer vertexData, indexData;
    
    Mesh(DataInputStream in, Memory memory, Armature armature, boolean hasTangents) throws IOException
    {
        this.memory = memory;
        name = DevilModel.readPaddedUTF(in);
        
        int bitFlags = in.readInt();
        this.hasTangents = hasTangents;
        hasUVs = (bitFlags & 1) == 1;
        hasVertexColors = (bitFlags & 2) == 2;
        
        textures = new String[in.readInt()];
        for (int i=0; i<textures.length; i++)
            textures[i] = DevilModel.readPaddedUTF(in);
        
        if (armature != null) numVertexGroups = in.readInt();
        else numVertexGroups = 0;
        
        //The order and length of vertex data is defined in export_dvm.py
        int floatsPerVertex = 3 + 3;
        if (hasTangents) floatsPerVertex += 3;
        if (hasUVs) floatsPerVertex += 2;
        if (hasVertexColors) floatsPerVertex += 3;
        floatsPerVertex += numVertexGroups*2;
        
        numVertices = in.readInt();
        int vertexDataLength = numVertices*floatsPerVertex;
        if (memory != null)
        {
            vertexBlock = memory.alloc(vertexDataLength*4);
            vertexData = vertexBlock.read();
        }
        else
        {
            vertexBlock = null;
            vertexData = BufferUtil.createByteBuffer(vertexDataLength*4);
        }
        
        for (int i=0; i<vertexDataLength; i++)
            vertexData.putFloat(in.readFloat());
        
        numTriangles = in.readInt();
        int triangleIndexDataLength = numTriangles*3;
        if (memory != null)
        {
            indexBlock = memory.alloc(triangleIndexDataLength*4);
            indexData = indexBlock.read();
        }
        else
        {
            indexBlock = null;
            indexData = BufferUtil.createByteBuffer(triangleIndexDataLength*4);
        }
        
        for (int i=0; i<triangleIndexDataLength; i++)
            indexData.putInt(in.readInt());
    }
    
    public ByteBuffer vertexData()
    {
        return vertexData;
    }
    
    public ByteBuffer indexData()
    {
        return indexData;
    }
    
    public final void rewindBuffers()
    {
        vertexData.rewind();
        indexData.rewind();
    }
    
    final void destroy()
    {
        if (memory != null)
        {
            vertexBlock.free();
            vertexBlock = null;
            indexBlock.free();
            indexBlock = null;
        }
        
        vertexData = null;
        indexData = null;
    }
}
