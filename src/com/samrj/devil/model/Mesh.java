package com.samrj.devil.model;

import com.samrj.devil.geo3d.Vertex3;
import com.samrj.devil.io.IOUtil;
import com.samrj.devil.io.Memory;
import com.samrj.devil.math.Vec2;
import com.samrj.devil.math.Vec3;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.IntFunction;

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
        
        vertexBlock = numVertices != 0 ? new Memory(intOffset*4) : null;
        vertexData = numVertices != 0 ? vertexBlock.buffer : null;
        
        for (int i=0; i<intOffset; i++) vertexData.putInt(in.readInt());
        
        numTriangles = in.readInt();
        int triangleIndexInts = numTriangles*3;
        indexBlock = numTriangles != 0 ? new Memory(triangleIndexInts*4) : null;
        indexData = numTriangles != 0 ? indexBlock.buffer : null;
        
        for (int i=0; i<triangleIndexInts; i++) indexData.putInt(in.readInt());
        if (indexData != null) indexData.rewind();
        
        if (hasMaterials && vertexData != null)
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
        
        if (vertexData != null) vertexData.rewind();
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
    
    /**
     * Reads this mesh data, allocating a new object for each vertex.
     * 
     * @return An array of newly allocated MeshVertices.
     */
    public MeshVertex[] readVertices()
    {
        MeshVertex[] vertices = new MeshVertex[numVertices];
        for (int i=0; i<numVertices; i++) vertices[i] = new MeshVertex();
        
        if (numVertices == 0) return vertices;
        
        vertexData.rewind();
        for (MeshVertex v : vertices) v.position.read(vertexData);
        if (hasNormals) for (MeshVertex v : vertices) v.normal.read(vertexData);
        for (int uv=0; uv<uvLayers.length; uv++)
            for (MeshVertex v : vertices) v.uvs[uv].read(vertexData);
        if (hasTangents) if (hasNormals) for (MeshVertex v : vertices) v.tangent.read(vertexData);
        for (int color=0; color<colorLayers.length; color++)
            for (MeshVertex v : vertices) v.colors[color].read(vertexData);
        for (int i=0; i<numGroups; i++) for (MeshVertex v : vertices)
            v.groupIndex[i] = vertexData.getInt();
        for (int i=0; i<numGroups; i++) for (MeshVertex v : vertices)
            v.groupWeight[i] = vertexData.getFloat();
        vertexData.rewind();
        
        return vertices;
    }
    
    /**
     * Reads the indices for this mesh, performing the given operation on each
     * triangle in this mesh, using the given index function.
     * 
     * @param <V> The type of vertex to operate on.
     * @param vertexFunction A function which converts indices into vertices.
     * @param consumer A consumer which accepts three vertices at a time.
     */
    public <V> void forEachTriangle(IntFunction<V> vertexFunction, TriConsumer<V> consumer)
    {
        if (indexData == null) return;
        
        indexData.rewind();
        for (int i=0; i<numTriangles; i++)
        {
            V a = vertexFunction.apply(indexData.getInt());
            V b = vertexFunction.apply(indexData.getInt());
            V c = vertexFunction.apply(indexData.getInt());
            consumer.accept(a, b, c);
        }
        indexData.rewind();
    }
    
    /**
     * Reads this mesh data, then performs the given operation on each triangle.
     * 
     * @param consumer The operation to perform on each triangle.
     */
    public void forEachTriangle(TriConsumer<MeshVertex> consumer)
    {
        MeshVertex[] vertices = readVertices();
        forEachTriangle(i -> vertices[i], consumer);
    }
    
    @Override
    void destroy()
    {
        if (vertexBlock != null) vertexBlock.free();
        if (indexBlock != null) indexBlock.free();
    }
    
    public class MeshVertex implements Vertex3
    {
        public final Vec3 position = new Vec3();
        public final Vec3 normal;
        public final Vec2[] uvs;
        public final Vec3 tangent;
        public final Vec3[] colors;
        public int[] groupIndex;
        public float[] groupWeight;
        public int material = -1;

        private MeshVertex()
        {
            normal = hasNormals ? new Vec3() : null;
            uvs = new Vec2[uvLayers.length];
            for (int i=0; i<uvs.length; i++) uvs[i] = new Vec2();
            tangent = hasTangents ? new Vec3() : null;
            colors = new Vec3[colorLayers.length];
            for (int i=0; i<colors.length; i++) colors[i] = new Vec3();
            groupIndex = new int[numGroups];
            groupWeight = new float[numGroups];
        }

        @Override
        public Vec3 p()
        {
            return position;
        }
    }
    
    @FunctionalInterface
    public interface TriConsumer<V>
    {
        public void accept(V a, V b, V c);
    }
}
