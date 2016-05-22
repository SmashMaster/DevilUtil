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
    
    Mesh(Model model, DataInputStream in) throws IOException
    {
        name = IOUtil.readPaddedUTF(in);
        
        int flags = in.readInt();
        hasNormals = (flags & 1) != 0;
        hasTangents = (flags & 2) != 0;
        boolean hasGroups = (flags & 4) != 0;
        boolean hasMaterials = (flags & 8) != 0;
        
        uvLayers = IOUtil.arrayFromStream(in, String.class, IOUtil::readPaddedUTF);
        colorLayers = IOUtil.arrayFromStream(in, String.class, IOUtil::readPaddedUTF);
        
        numGroups = in.readInt();
        
        //The order and length of vertex data is defined by io_mesh_dvm.
        int intsPerVertex = 3; //Positions
        if (hasNormals) intsPerVertex += 3; //Normals
        intsPerVertex += uvLayers.length*2; //UVs
        if (hasTangents) intsPerVertex += 3; //Tangents
        intsPerVertex += colorLayers.length*3; //Colors
        if (hasGroups)
        {
            intsPerVertex += numGroups; //Group indices
            intsPerVertex += numGroups; //Group weights
        }
        if (hasMaterials) intsPerVertex += 1; //Material indices
        
        numVertices = in.readInt();
        int vertexInts = numVertices*intsPerVertex;
        vertexBlock = new Memory(vertexInts*4);
        vertexData = vertexBlock.buffer;
        
        for (int i=0; i<vertexInts; i++) vertexData.putInt(in.readInt());
        vertexData.rewind();
        
        numTriangles = in.readInt();
        int triangleIndexInts = numTriangles*3;
        indexBlock = new Memory(triangleIndexInts*4);
        indexData = indexBlock.buffer;
        
        for (int i=0; i<triangleIndexInts; i++) indexData.putInt(in.readInt());
        indexData.rewind();
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
