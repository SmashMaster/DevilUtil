package com.samrj.devil.graphics.model;

import com.samrj.devil.io.Memory;
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
    public final Memory vertexBlock, indexBlock;
    public final ByteBuffer vertexData, indexData;
    
    Mesh(DataInputStream in, Armature armature, boolean hasTangents) throws IOException
    {
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
        vertexBlock = new Memory(vertexDataLength*4);
        vertexData = vertexBlock.buffer;
        
        for (int i=0; i<vertexDataLength; i++)
            vertexData.putFloat(in.readFloat());
        vertexData.rewind();
        
        numTriangles = in.readInt();
        int triangleIndexDataLength = numTriangles*3;
        indexBlock = new Memory(triangleIndexDataLength*4);
        indexData = indexBlock.buffer;
        
        for (int i=0; i<triangleIndexDataLength; i++)
            indexData.putInt(in.readInt());
        indexData.rewind();
    }
    
    final void destroy()
    {
        vertexBlock.free();
        indexBlock.free();
    }
}
