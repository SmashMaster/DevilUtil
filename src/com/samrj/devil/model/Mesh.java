package com.samrj.devil.model;

import com.samrj.devil.io.IOUtil;
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
public class Mesh implements DataBlock
{
    public final String name;
    
    public final boolean hasNormals, hasTangents;
    public final int numGroups;
    
    public final String[] uvLayers, colorLayers;
    public final int numVertices;
    public final Memory vertexBlock;
    public final ByteBuffer vertexData;
    
    public final int numTriangles;
    public final Memory indexBlock;
    public final ByteBuffer indexData;
    
    public final int[] materials;
    
    Mesh(DataInputStream in) throws IOException
    {
        name = IOUtil.readPaddedUTF(in);
        
        int flags = in.readInt();
        hasNormals = (flags & 1) != 0;
        hasTangents = (flags & 2) != 0;
        boolean hasGroups = (flags & 4) != 0;
        boolean hasMaterials = (flags & 8) != 0;
        
        uvLayers = IOUtil.arrayFromStream(in, String.class, stream -> IOUtil.readPaddedUTF(stream));
        colorLayers = IOUtil.arrayFromStream(in, String.class, stream -> IOUtil.readPaddedUTF(stream));
        
        if (hasGroups) numGroups = in.readInt();
        else
        {
            numGroups = 0;
            in.skip(4);
        }
        
        //The order and length of vertex data is defined by io_mesh_dvm.
        int floatsPerVertex = 3; //Positions
        if (hasNormals) floatsPerVertex += 3; //Normals
        if (hasTangents) floatsPerVertex += 3; //Tangents
        floatsPerVertex += 2*uvLayers.length; //UVs
        floatsPerVertex += 3*colorLayers.length; //Colors
        if (hasGroups)
        {
            floatsPerVertex += numGroups; //Group indices
            floatsPerVertex += numGroups; //Group weights
        }
        
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
        
        if (hasMaterials)
        {
            materials = new int[numTriangles];
            for (int i=0; i<numTriangles; i++)
                materials[i] = in.readInt();
        }
        else materials = null;
    }
    
    final void destroy()
    {
        vertexBlock.free();
        indexBlock.free();
    }
    
    @Override
    public String getName()
    {
        return name;
    }
}
