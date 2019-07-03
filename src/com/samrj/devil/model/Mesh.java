package com.samrj.devil.model;

import com.samrj.devil.geo3d.Earcut;
import com.samrj.devil.geo3d.Vertex3;
import com.samrj.devil.io.Memory;
import com.samrj.devil.math.Mat3;
import com.samrj.devil.math.Vec2;
import com.samrj.devil.math.Vec3;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.IntFunction;
import org.blender.dna.MLoop;
import org.blender.dna.MPoly;
import org.blender.dna.MVert;

/**
 * DevilModel mesh.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2019 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class Mesh extends DataBlock
{
    private static class LoopTri
    {
        private final int poly;
        private final int va, vb, vc;
        
        private LoopTri(int poly, int start, int va, int vb, int vc)
        {
            this.poly = poly;
            this.va = start + va;
            this.vb = start + vb;
            this.vc = start + vc;
        }
    }
    
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
    
    Mesh(Model model, org.blender.dna.Mesh bMesh) throws IOException
    {
        super(model, bMesh.getId().getName().asString().substring(2));
        
        MPoly[] mPolys = bMesh.getMpoly().toArray(bMesh.getTotpoly());
        MLoop[] mLoops = bMesh.getMloop().toArray(bMesh.getTotloop());
        MVert[] mVerts = bMesh.getMvert().toArray(bMesh.getTotvert());
        
        Vec3[] verts = new Vec3[mVerts.length];
        for (int i=0; i<mVerts.length; i++) verts[i] = Blender.vec3(mVerts[i].getCo());
        
//        System.out.println(name + " " + mPolys.length + " " + mLoops.length + " " + mVerts.length);
        
        List<LoopTri> loopTris = new ArrayList<>();
        
        for (int iPoly=0; iPoly<mPolys.length; iPoly++)
        {
            MPoly mPoly = mPolys[iPoly];
            
            int start = mPoly.getLoopstart();
            int count = mPoly.getTotloop();
            int end = start + count;
            
            if (count < 3) {} //Degenerate poly
            else if (count == 3) //Single triangle poly
            {
                loopTris.add(new LoopTri(iPoly, start, 0, 1, 2));
            }
            else if (count == 4) //Quad; can be split into two tris, but might be concave.
            {
                Vec3 v0 = verts[mLoops[start].getV()];
                Vec3 v1 = verts[mLoops[start + 1].getV()];
                Vec3 v2 = verts[mLoops[start + 2].getV()];
                Vec3 v3 = verts[mLoops[start + 3].getV()];
                
                Vec3 e01 = Vec3.sub(v1, v0);
                Vec3 e02 = Vec3.sub(v2, v0);
                Vec3 e03 = Vec3.sub(v3, v0);
                
                Vec3 crossA = Vec3.cross(e01, e02);
                Vec3 crossB = Vec3.cross(e03, e02);
                
                if (crossA.dot(crossB) > 0.0f)
                {
                    loopTris.add(new LoopTri(iPoly, start, 0, 1, 3));
                    loopTris.add(new LoopTri(iPoly, start, 1, 2, 3));
                }
                else
                {
                    loopTris.add(new LoopTri(iPoly, start, 0, 1, 2));
                    loopTris.add(new LoopTri(iPoly, start, 0, 2, 3));
                }
            }
            else
            {
                //Calculate normal by Newell's method
                Vec3 normal = new Vec3();
                for (int i0=start; i0<end; i0++)
                {
                    int i1 = i0 + 1;
                    if (i1 == end) i1 = start;
                    
                    Vec3 v0 = verts[mLoops[i0].getV()];
                    Vec3 v1 = verts[mLoops[i1].getV()];
                    
                    normal.x += (v0.y - v1.y)*(v0.z + v1.z);
                    normal.y += (v0.z - v1.z)*(v0.x + v1.x);
                    normal.z += (v0.x - v1.x)*(v0.y + v1.y);
                }
                
                //Make a matrix for projection to 2D
                float nrmLen = normal.length();
                if (nrmLen == 0.0f) normal.z = 1.0f;
                else normal.div(nrmLen);
                Mat3 basis = Blender.orthogBasis(normal);
                
                double[] projData = new double[count*2];
                
                for (int i=0; i<count; i++)
                {
                    Vec3 v = verts[mLoops[start + i].getV()];
                    Vec3 projected = Vec3.mult(v, basis);
                    
                    projData[i*2] = projected.x;
                    projData[i*2 + 1] = projected.y;
                }
                
                //Compute the triangulation
                List<Integer> triangulated = Earcut.earcut(projData);
                
                for (int i=0; i<triangulated.size();)
                {
                    int a = triangulated.get(i++);
                    int b = triangulated.get(i++);
                    int c = triangulated.get(i++);
                    
                    loopTris.add(new LoopTri(iPoly, start, a, b, c));
                }
            }
        }
        
        numVertices = verts.length;
        numTriangles = loopTris.size();
        
        hasNormals = false;
        hasTangents = false;
        numGroups = 0;
        hasMaterials = false;
        
        uvLayers = new String[0];
        colorLayers = new String[0];
        
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
        intOffset += numVertices*numGroups;
        
        //Group weights
        groupWeightOffset = intOffset*4;
        intOffset += numVertices*numGroups;
        
        //Material indices
        materialOffset = intOffset*4;
        if (hasMaterials) intOffset += numVertices;
        
        vertexBlock = numVertices != 0 ? new Memory(intOffset*4) : null;
        vertexData = numVertices != 0 ? vertexBlock.buffer : null;
        for (Vec3 vert : verts)
        {
            vertexData.putFloat(vert.x);
            vertexData.putFloat(vert.y);
            vertexData.putFloat(vert.z);
        }
        vertexData.rewind();
        
        int triangleIndexInts = numTriangles*3;
        indexBlock = numTriangles != 0 ? new Memory(triangleIndexInts*4) : null;
        indexData = numTriangles != 0 ? indexBlock.buffer : null;
        if (indexData != null)
        {
            for (LoopTri loopTri : loopTris)
            {
                int ia = mLoops[loopTri.va].getV();
                int ib = mLoops[loopTri.vb].getV();
                int ic = mLoops[loopTri.vc].getV();
                
                indexData.putInt(ia);
                indexData.putInt(ib);
                indexData.putInt(ic);
            }
            indexData.rewind();
        }
        
//        if (hasMaterials && vertexData != null)
//        {
//            Set<Integer> matIndices = new HashSet<>();
//            List<DataPointer<Material>> matList = new ArrayList<>();
//            vertexData.position(materialOffset);
//            for (int i=0; i<numVertices; i++)
//            {
//                int matIndex = vertexData.getInt();
//                if (matIndex < 0) continue;
//                if (matIndices.add(matIndex))
//                    matList.add(new DataPointer(model, Type.MATERIAL, matIndex));
//            }
//            materials = Collections.unmodifiableList(matList);
//        }
//        else
            materials = Collections.EMPTY_LIST;
        
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
