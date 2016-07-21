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
 * @copyright 2016 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class Mesh extends DataBlock
{
    public final boolean hasNormals, hasTangents;
    public final int numGroups;
    public final boolean hasMaterials;
    
    public final String[] uvLayers, colorLayers;
    public final int numVertices;
    public final Memory vertexBlock;
    public final ByteBuffer vertexData;
    
    public final int numTriangles;
    public final Memory indexBlock;
    public final ByteBuffer indexData;
    
    public final int positionOffset, normalOffset, uvOffset, tangentOffset;
    public final int[] colorOffsets;
    public final int groupIndexOffset, groupWeightOffset;
    public final int materialOffset;
    
    Mesh(Model model, DataInputStream in) throws IOException
    {
        super(model, in);
        
        int flags = in.readInt();
        hasNormals = (flags & 1) != 0;
        hasTangents = (flags & 2) != 0;
        boolean hasGroups = (flags & 4) != 0;
        hasMaterials = (flags & 8) != 0;
        
        uvLayers = IOUtil.arrayFromStream(in, String.class, IOUtil::readPaddedUTF);
        colorLayers = IOUtil.arrayFromStream(in, String.class, IOUtil::readPaddedUTF);
        
        numGroups = in.readInt();
        numVertices = in.readInt();
        
        //The order and length of vertex data is defined by io_mesh_dvm.
        
        //Positions
        positionOffset = 0;
        int intOffset = numVertices*3;
        
        //Normals
        normalOffset = intOffset*4;
        if (hasNormals) intOffset += numVertices*3;
        
        //UVs
        uvOffset = intOffset*4;
        intOffset += numVertices*uvLayers.length*2;
        
        //Tangents
        tangentOffset = intOffset*4;
        if (hasTangents) intOffset += numVertices*3; 
        
        //Colors
        colorOffsets = new int[colorLayers.length];
        for (int i=0; i<colorLayers.length; i++)
        {
            colorOffsets[i] = intOffset*4;
            intOffset += numVertices*3;
        }
        
        //Group indices
        groupIndexOffset = intOffset*4;
        if (hasGroups) intOffset += numVertices*numGroups;
        
        //Group weights
        groupWeightOffset = intOffset*4;
        if (hasGroups) intOffset += numVertices*numGroups;
        
        //Material indices
        materialOffset = intOffset*4;
        if (hasMaterials) intOffset += numVertices;
        
        vertexBlock = new Memory(intOffset*4);
        vertexData = vertexBlock.buffer;
        
        for (int i=0; i<intOffset; i++) vertexData.putInt(in.readInt());
        vertexData.rewind();
        
        numTriangles = in.readInt();
        int triangleIndexInts = numTriangles*3;
        indexBlock = new Memory(triangleIndexInts*4);
        indexData = indexBlock.buffer;
        
        for (int i=0; i<triangleIndexInts; i++) indexData.putInt(in.readInt());
        indexData.rewind();
    }
    
    /**
     * Returns the first material found in this mesh. This mesh may have other
     * materials.
     */
    public Material getMaterial()
    {
        if (!hasMaterials) return null;
        int index = vertexData.get(materialOffset);
        return model.materials.get(index);
    }
    
    @Override
    void destroy()
    {
        vertexBlock.free();
        indexBlock.free();
    }
}
