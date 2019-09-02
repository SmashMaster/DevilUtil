/*
 * Copyright (c) 2019 Sam Johnson
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.samrj.devil.model;

import com.samrj.devil.geo2d.Earcut;
import com.samrj.devil.geo3d.Vertex3;
import com.samrj.devil.math.Mat3;
import com.samrj.devil.math.Vec2;
import com.samrj.devil.math.Vec3;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;

import static org.lwjgl.system.MemoryUtil.*;

/**
 * Blender mesh object.
 * 
 * @author Samuel Johnson (SmashMaster)
 */
public final class Mesh extends DataBlock
{
    /**
     * This lets us project points onto a plane defined by a normal, and ignore the z coordinate.
     */
    private static void orthogBasis(Vec3 n, Mat3 result)
    {
        float len = n.length();

        if (len != 0)
        {
            //Todo: Optimize this
            Vec3 b = Vec3.cross(n, new Vec3(1.0f, 0.0f, 0.0f));
            if (b.isZero(0.01f)) Vec3.cross(n, new Vec3(0.0f, 1.0f, 0.0f), b);
            b.normalize();
            
            Vec3 t = Vec3.cross(n, b).normalize();
            
            result.set(b.x, b.y, b.z,
                       t.x, t.y, t.z,
                       0.0f, 0.0f, 0.0f);
        }
        else result.setIdentity(); //Cannot create basis from zero vector
    }
    
    private static Mat3 orthogBasis(Vec3 n)
    {
        Mat3 result = new Mat3();
        orthogBasis(n, result);
        return result;
    }
    
    private static class LoopTri
    {
        private final int va, vb, vc;
        
        private LoopTri(int start, int va, int vb, int vc)
        {
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
    public final ByteBuffer vertexData;
    
    public final int numTriangles;
    public final ByteBuffer indexData;
    
    public final int positionOffset, normalOffset;
    public final int[] uvOffsets;
    public final int tangentOffset;
    public final int[] colorOffsets;
    public final int groupIndexOffset, groupWeightOffset;
    public final int materialOffset;
    
    public final DataPointer<Material>[] materials;
    
    Mesh(Model model, BlendFile.Pointer bMesh) throws IOException
    {
        super(model, bMesh);
        
        /**
         * PREPARE MESH DATA
         */
        
        //Populate materials list
        int totcol = bMesh.getField("totcol").asInt();
        materials = new DataPointer[totcol];
        BlendFile.Pointer mats = bMesh.getField("mat").dereference();
        if (mats != null) for (int i=0; i<totcol; i++)
        {
            //Need special handling because mats is an array of pointers.
            BlendFile.Pointer mat = mats.add(mats.getAddressSize()*i).dereference();
            String matName = mat.getField(0).getField("name").asString().substring(2);
            materials[i] = new DataPointer<>(model, Type.MATERIAL, matName);
        }
        
        //Vertex copy could probably be sped up massively by grabbing the whole array with asBuffer().
        int totvert = bMesh.getField("totvert").asInt();
        BlendFile.Pointer[] mVerts = bMesh.getField("mvert").dereference().asArray(totvert);
        Vec3[] verts = new Vec3[totvert];
        Vec3[] normals = new Vec3[totvert];
        for (int i=0; i<totvert; i++)
        {
            BlendFile.Pointer mVert = mVerts[i];
            verts[i] = mVert.getField("co").asVec3();
            normals[i] = mVert.getField("no").asNormalVec3();
        }
        
        int totloop = bMesh.getField("totloop").asInt();
        BlendFile.Pointer[] mLoops = bMesh.getField("mloop").dereference().asArray(totloop);
        Vec3[] loopNormals = new Vec3[totloop];
        int[] loopMats = materials.length == 0 ? null : new int[totloop];
        
        int totpoly = bMesh.getField("totpoly").asInt();
        BlendFile.Pointer[] mPolys = bMesh.getField("mpoly").dereference().asArray(totpoly);
        List<LoopTri> loopTris = new ArrayList<>();
        
        for (int iPoly=0; iPoly<totpoly; iPoly++)
        {
            BlendFile.Pointer mPoly = mPolys[iPoly];
            
            int start = mPoly.getField("loopstart").asInt();
            int count = mPoly.getField("totloop").asInt();
            int end = start + count;
            
            //Calculate normal by Newell's method
            Vec3 normal = new Vec3();
            for (int i0=start; i0<end; i0++)
            {
                int i1 = i0 + 1;
                if (i1 == end) i1 = start;
                
                Vec3 v0 = verts[mLoops[i0].getField("v").asInt()];
                Vec3 v1 = verts[mLoops[i1].getField("v").asInt()];

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
                loopTris.add(new LoopTri(start, 0, 1, 2));
            }
            else if (count == 4) //Quad; can be split into two tris, but might be concave.
            {
                Vec3 v0 = verts[mLoops[start].getField("v").asInt()];
                Vec3 v1 = verts[mLoops[start + 1].getField("v").asInt()];
                Vec3 v2 = verts[mLoops[start + 2].getField("v").asInt()];
                Vec3 v3 = verts[mLoops[start + 3].getField("v").asInt()];
                
                Vec3 e01 = Vec3.sub(v1, v0);
                Vec3 e02 = Vec3.sub(v2, v0);
                Vec3 e03 = Vec3.sub(v3, v0);
                
                Vec3 crossA = Vec3.cross(e01, e02);
                Vec3 crossB = Vec3.cross(e03, e02);
                
                if (crossA.dot(crossB) > 0.0f)
                {
                    loopTris.add(new LoopTri(start, 0, 1, 3));
                    loopTris.add(new LoopTri(start, 1, 2, 3));
                }
                else
                {
                    loopTris.add(new LoopTri(start, 0, 1, 2));
                    loopTris.add(new LoopTri(start, 0, 2, 3));
                }
            }
            else //Need to triangulate by ear clipping
            {
                //Project to 2D
                Mat3 basis = orthogBasis(normal);
                double[] projData = new double[count*2];
                for (int i=0; i<count; i++)
                {
                    Vec3 v = verts[mLoops[start + i].getField("v").asInt()];
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
                    
                    loopTris.add(new LoopTri(start, a, b, c));
                }
            }
            
            //Store normals
            boolean isSmooth = (mPoly.getField("flag").asByte() & 1) != 0;
            if (isSmooth)
                for (int i=start; i<end; i++)
                    loopNormals[i] = normals[mLoops[i].getField("v").asInt()];
            else for (int i=start; i<end; i++)
                    loopNormals[i] = normal;
            
            //Store face material
            if (loopMats != null)
            {
                int polyMat = materials[mPoly.getField("mat_nr").asShort()].get().modelIndex;
                for (int i=start; i<end; i++)
                    loopMats[i] = polyMat;
            }
        }
        
        //Prepare vertex group data
        int maxGroup = -1;
        BlendFile.Pointer dVertArr = bMesh.getField("dvert").dereference();
        BlendFile.Pointer[] dVerts = dVertArr == null ? null : dVertArr.asArray(totvert);
        if (dVerts != null)
            for (int i=0; i<mVerts.length; i++)
                maxGroup = Math.max(maxGroup, dVerts[i].getField("totweight").asInt());
        
        int[][] groupIndices = new int[mVerts.length][maxGroup + 1];
        float[][] groupWeights = new float[mVerts.length][maxGroup + 1];
        
        if (dVerts != null && maxGroup >= 0) for (int vi=0; vi<totvert; vi++)
        {
            BlendFile.Pointer dVert = dVerts[vi];
            int totweight = dVert.getField("totweight").asInt();
            BlendFile.Pointer[] weights = dVert.getField("dw").dereference().asArray(totweight);
            
            for (int wi=0; wi<weights.length; wi++)
            {
                BlendFile.Pointer weight = weights[wi];
                groupIndices[vi][wi] = weight.getField("def_nr").asInt();
                groupWeights[vi][wi] = weight.getField("weight").asFloat();
            }
        }
        
        //Loop data: uv and colors
        List<String> uvLayerNames = new ArrayList<>();
        List<BlendFile.Pointer[]> uvLayerData = new ArrayList<>();
        List<String> colorLayerNames = new ArrayList<>();
        List<BlendFile.Pointer[]> colorLayerData = new ArrayList<>();
        BlendFile.Pointer ldata = bMesh.getField("ldata");
        int totlayer = ldata.getField("totlayer").asInt();
        BlendFile.Pointer layersPtr = ldata.getField("layers").dereference();
        if (layersPtr != null)
        {
            for (BlendFile.Pointer layer : layersPtr.asArray(totlayer))
            {
                String layerName = layer.getField("name").asString();
                
                switch (layer.getField("type").asInt())
                {
                    case 16: //uv
                        BlendFile.Pointer[] uvData = layer.getField("data").dereference().cast("MLoopUV").asArray(totloop);
                        if (uvData != null)
                        {
                            uvLayerNames.add(layerName);
                            uvLayerData.add(uvData);
                        }
                        break;
                    case 17: //colors
                        BlendFile.Pointer[] colData = layer.getField("data").dereference().cast("MLoopCol").asArray(totloop);
                        if (colData != null)
                        {
                            colorLayerNames.add(layerName);
                            colorLayerData.add(colData);
                        }
                        break;
                }
            }
        }
        
        /**
         * CALCULATE BUFFER POINTERS
         */
        
        numVertices = mLoops.length;
        numTriangles = loopTris.size();
        
        hasTangents = false;
        numGroups = maxGroup + 1;
        hasMaterials = loopMats != null;
        
        uvLayers = uvLayerNames.toArray(new String[uvLayerNames.size()]);
        colorLayers = colorLayerNames.toArray(new String[colorLayerNames.size()]);
        
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
        
        vertexData = numVertices != 0 ? memAlloc(intOffset*4) : null;
        if (vertexData != null)
        {
            vertexData.position(positionOffset);
            for (int i=0; i<numVertices; i++)
            {
                Vec3 vert = verts[mLoops[i].getField("v").asInt()];
                
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
                
                BlendFile.Pointer[] uvs = uvLayerData.get(layer);
                for (int i=0; i<numVertices; i++)
                {
                    float[] uv = uvs[i].getField("uv").asFloats(2);
                    vertexData.putFloat(uv[0]);
                    vertexData.putFloat(uv[1]);
                }
            }
            
            if (hasTangents)
            {
                vertexData.position(tangentOffset);
                //tangents need to be calculated from normals and uvs.
            }
            
            for (int layer=0; layer<colorLayers.length; layer++)
            {
                vertexData.position(colorOffsets[layer]);
                
                BlendFile.Pointer[] colors = colorLayerData.get(layer);
                for (int i=0; i<numVertices; i++)
                {
                    BlendFile.Pointer color = colors[i];
                    vertexData.putFloat((color.getField("r").asByte() & 0xFF)/255.0f);
                    vertexData.putFloat((color.getField("g").asByte() & 0xFF)/255.0f);
                    vertexData.putFloat((color.getField("b").asByte() & 0xFF)/255.0f);
                }
            }
            
            if (numGroups > 0)
            {
                vertexData.position(groupIndexOffset);
                for (int lvi=0; lvi<numVertices; lvi++)
                {
                    int vi = mLoops[lvi].getField("v").asInt();
                    for (int gi=0; gi<numGroups; gi++)
                        vertexData.putInt(groupIndices[vi][gi]);
                }
                
                vertexData.position(groupWeightOffset);
                for (int lvi=0; lvi<numVertices; lvi++)
                {
                    int vi = mLoops[lvi].getField("v").asInt();
                    for (int gi=0; gi<numGroups; gi++)
                        vertexData.putFloat(groupWeights[vi][gi]);
                }
            }
            
            if (loopMats != null)
            {
                vertexData.position(materialOffset);
                for (int i=0; i<numVertices; i++)
                    vertexData.putInt(loopMats[i]);
            }
            
            vertexData.flip();
        }
        
        int triangleIndexInts = numTriangles*3;
        indexData = numTriangles != 0 ? memAlloc(triangleIndexInts*4) : null;
        if (indexData != null)
        {
            for (LoopTri loopTri : loopTris)
            {
                indexData.putInt(loopTri.va);
                indexData.putInt(loopTri.vb);
                indexData.putInt(loopTri.vc);
            }
            indexData.flip();
        }
    }
    
    /**
     * Returns the first material found in this mesh, or null if this mesh has
     * no materials. This mesh may have more than one material.
     */
    public Material getMaterial()
    {
        if (materials.length == 0) return null;
        else return materials[0].get();
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
        if (vertexData != null) memFree(vertexData);
        if (indexData != null) memFree(indexData);
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
