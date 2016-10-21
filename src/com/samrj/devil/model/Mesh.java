package com.samrj.devil.model;

import com.samrj.devil.io.IOUtil;
import com.samrj.devil.io.Memory;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    
    public final int positionOffset, normalOffset;
    public final int[] uvOffsets;
    public final int tangentOffset;
    public final int[] colorOffsets;
    public final int groupIndexOffset, groupWeightOffset;
    public final int materialOffset;
    
    public final List<DataPointer<Material>> materials;
    
    Mesh(Model model, int modelIndex, DataInputStream in) throws IOException
    {
        super(model, modelIndex, in);
        
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
        uvOffsets = new int[uvLayers.length];
        for (int i=0; i<uvLayers.length; i++)
        {
            uvOffsets[i] = intOffset*4;
            intOffset += numVertices*2;
        }
        
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
        
        numTriangles = in.readInt();
        int triangleIndexInts = numTriangles*3;
        indexBlock = new Memory(triangleIndexInts*4);
        indexData = indexBlock.buffer;
        
        for (int i=0; i<triangleIndexInts; i++) indexData.putInt(in.readInt());
        indexData.rewind();
        
        if (hasMaterials)
        {
            Set<Integer> matIndices = new HashSet<>();
            List<DataPointer<Material>> matList = new ArrayList<>();
            vertexData.position(materialOffset);
            for (int i=0; i<numVertices; i++)
            {
                int matIndex = vertexData.getInt();
                if (matIndex < 0) continue;
                if (matIndices.add(matIndex))
                    matList.add(new DataPointer(model, Type.MATERIAL, matIndex));
            }
            materials = Collections.unmodifiableList(matList);
        }
        else materials = Collections.EMPTY_LIST;
        vertexData.rewind();
    }
    
    /**
     * Returns the first material found in this mesh, or null if this mesh has
     * no materials. This mesh may have more than one material.
     */
    public Material getMaterial()
    {
        if (materials.isEmpty()) return null;
        else return materials.get(0).get();
    }
    
    @Override
    void destroy()
    {
        vertexBlock.free();
        indexBlock.free();
    }
}
