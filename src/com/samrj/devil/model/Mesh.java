package com.samrj.devil.model;

import com.samrj.devil.geo2d.Earcut;
import com.samrj.devil.geo3d.Vertex3;
import com.samrj.devil.io.Memory;
import com.samrj.devil.math.Mat3;
import com.samrj.devil.math.Vec2;
import com.samrj.devil.math.Vec3;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntFunction;
import org.blender.dna.MDeformVert;
import org.blender.dna.MDeformWeight;
import org.blender.dna.MFace;
import org.blender.dna.MLoop;
import org.blender.dna.MPoly;
import org.blender.dna.MVert;
import org.cakelab.blender.nio.CPointer;

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
    
    public final boolean hasTangents;
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
        super(model, bMesh.getId());
        
        /**
         * PREPARE MESH DATA
         */
        
        materials = new ArrayList<>();
        Map<Integer, Integer> materialIndexMap = new HashMap<>();
        CPointer<org.blender.dna.Material>[] mats = bMesh.getMat().toArray(bMesh.getTotcol());
        if (mats != null)
        {
            int blenderMatIndex = 0;
            for (CPointer<org.blender.dna.Material> ptr : mats)
            {
                if (!ptr.isNull())
                {
                    int matIndex = materials.size();
                    String matName = ptr.get().getId().getName().asString().substring(2);
                    materials.add(new DataPointer<>(model, Type.MATERIAL, matName));
                    materialIndexMap.put(blenderMatIndex, matIndex);
                }
                blenderMatIndex++;
            }
        }
        
        MVert[] mVerts = bMesh.getMvert().toArray(bMesh.getTotvert());
        Vec3[] verts = new Vec3[mVerts.length];
        for (int i=0; i<mVerts.length; i++) verts[i] = Blender.vec3(mVerts[i].getCo());
        Vec3[] normals = new Vec3[mVerts.length];
        for (int i=0; i<mVerts.length; i++) normals[i] = Blender.normal(mVerts[i].getNo());
        
        MLoop[] mLoops = bMesh.getMloop().toArray(bMesh.getTotloop());
        Vec3[] loopNormals = new Vec3[mLoops.length];
        
        MPoly[] mPolys = bMesh.getMpoly().toArray(bMesh.getTotpoly());
        List<LoopTri> loopTris = new ArrayList<>();
        
        for (int iPoly=0; iPoly<mPolys.length; iPoly++)
        {
            MPoly mPoly = mPolys[iPoly];
            
            int start = mPoly.getLoopstart();
            int count = mPoly.getTotloop();
            int end = start + count;
            
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
            
            float nrmLen = normal.length();
            if (nrmLen == 0.0f) normal.z = 1.0f;
            else normal.div(nrmLen);
            
            //Triangulate the face
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
            else //Need to triangulate by ear clipping
            {
                //Make a matrix for projection to 2D
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
            
            //Store normals
            boolean isSmooth = (mPoly.getFlag() & 1) != 0;
            if (isSmooth)
                for (int i=start; i<end; i++)
                    loopNormals[i] = normals[mLoops[i].getV()];
            else for (int i=start; i<end; i++)
                    loopNormals[i] = normal;
        }
        
        //Prepare vertex group data
        int maxGroup = -1;
        CPointer<MDeformVert> dVertPtr = bMesh.getDvert();
        MDeformVert[] dVerts = dVertPtr.isNull() ? null : dVertPtr.toArray(mVerts.length);
        if (dVerts != null)
            for (int i=0; i<mVerts.length; i++)
                maxGroup = Math.max(maxGroup, dVerts[i].getTotweight());
        
        int[][] groupIndices = new int[mVerts.length][maxGroup + 1];
        float[][] groupWeights = new float[mVerts.length][maxGroup + 1];
        
        if (dVerts != null && maxGroup >= 0) for (int vi=0; vi<mVerts.length; vi++)
        {
            MDeformVert dVert = dVerts[vi];
            MDeformWeight[] weights = dVert.getDw().toArray(dVert.getTotweight());
            
            for (int wi=0; wi<weights.length; wi++)
            {
                MDeformWeight weight = weights[wi];
                groupIndices[vi][wi] = weight.getDef_nr();
                groupWeights[vi][wi] = weight.getWeight();
            }
        }
        
        /**
         * CALCULATE BUFFER POINTERS
         */
        
        numVertices = mLoops.length;
        numTriangles = loopTris.size();
        
        hasTangents = false;
        numGroups = maxGroup + 1;
        hasMaterials = false;
        
        uvLayers = new String[0];
        colorLayers = new String[0];
        
        //Positions
        positionOffset = 0;
        int intOffset = numVertices*3;
        
        //Normals
        normalOffset = intOffset*4;
        intOffset += numVertices*3;
        
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
        
        /**
         * ALLOCATE AND FILL BUFFERS
         */
        
        vertexBlock = numVertices != 0 ? new Memory(intOffset*4) : null;
        vertexData = numVertices != 0 ? vertexBlock.buffer : null;
        if (vertexData != null)
        {
            vertexData.position(positionOffset);
            for (int i=0; i<numVertices; i++)
            {
                Vec3 vert = verts[mLoops[i].getV()];
                
                vertexData.putFloat(vert.x);
                vertexData.putFloat(vert.y);
                vertexData.putFloat(vert.z);
            }
            
            vertexData.position(normalOffset);
            for (int i=0; i<numVertices; i++)
            {
                Vec3 normal = loopNormals[i];
                if (normal != null)
                {
                    vertexData.putFloat(normal.x);
                    vertexData.putFloat(normal.y);
                    vertexData.putFloat(normal.z);
                }
                else
                {
                    vertexData.putFloat(0.0f);
                    vertexData.putFloat(0.0f);
                    vertexData.putFloat(0.0f);
                }
            }
            
            for (int layer=0; layer<uvLayers.length; layer++)
            {
                vertexData.position(uvOffsets[layer]);
                //put uvs
            }
            
            if (hasTangents)
            {
                vertexData.position(tangentOffset);
                //put tangents
            }
            
            for (int layer=0; layer<colorLayers.length; layer++)
            {
                vertexData.position(colorOffsets[layer]);
                //put colors
            }
            
            if (numGroups > 0)
            {
                vertexData.position(groupIndexOffset);
                for (int lvi=0; lvi<numVertices; lvi++)
                {
                    int vi = mLoops[lvi].getV();
                    for (int gi=0; gi<numGroups; gi++)
                        vertexData.putInt(groupIndices[vi][gi]);
                }
                
                vertexData.position(groupWeightOffset);
                for (int lvi=0; lvi<numVertices; lvi++)
                {
                    int vi = mLoops[lvi].getV();
                    for (int gi=0; gi<numGroups; gi++)
                        vertexData.putFloat(groupWeights[vi][gi]);
                }
            }
            
            if (hasMaterials)
            {
                vertexData.position(materialOffset);
                //put materials
            }
            
            vertexData.rewind();
        }
        
        int triangleIndexInts = numTriangles*3;
        indexBlock = numTriangles != 0 ? new Memory(triangleIndexInts*4) : null;
        indexData = numTriangles != 0 ? indexBlock.buffer : null;
        if (indexData != null)
        {
            for (LoopTri loopTri : loopTris)
            {
                indexData.putInt(loopTri.va);
                indexData.putInt(loopTri.vb);
                indexData.putInt(loopTri.vc);
            }
            indexData.rewind();
        }
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
        for (MeshVertex v : vertices) v.normal.read(vertexData);
        for (int uv=0; uv<uvLayers.length; uv++)
            for (MeshVertex v : vertices) v.uvs[uv].read(vertexData);
        if (hasTangents) for (MeshVertex v : vertices) v.tangent.read(vertexData);
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
        public final Vec3 normal = new Vec3();
        public final Vec2[] uvs;
        public final Vec3 tangent;
        public final Vec3[] colors;
        public int[] groupIndex;
        public float[] groupWeight;
        public int material = -1;

        private MeshVertex()
        {
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
