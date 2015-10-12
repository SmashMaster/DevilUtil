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
    public final String[] uvLayers, colorLayers;
    public final int numGroups;
    
    public final int numVertices;
    public final Memory vertexBlock;
    public final ByteBuffer vertexData;
    
    public final int numTriangles;
    public final Memory indexBlock;
    public final ByteBuffer indexData;
    
    Mesh(DataInputStream in) throws IOException
    {
        name = DevilModel.readPaddedUTF(in);
        
        int numUVLayers = in.readInt();
        uvLayers = new String[numUVLayers];
        for (int i=0; i<numUVLayers; i++) uvLayers[i] = DevilModel.readPaddedUTF(in);
        
        int numColorLayers = in.readInt();
        colorLayers = new String[numColorLayers];
        for (int i=0; i<numColorLayers; i++) colorLayers[i] = DevilModel.readPaddedUTF(in);
        
        numGroups = in.readInt();
        
        //The order and length of vertex data is defined by io_mesh_dvm.
        int floatsPerVertex = 3; //Positions
        floatsPerVertex += 3; //Normals
        floatsPerVertex += 2*numUVLayers; //UVs
        floatsPerVertex += 3*numColorLayers; //Colors
        floatsPerVertex += numGroups; //Group indices
        floatsPerVertex += numGroups; //Group weights
        
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
