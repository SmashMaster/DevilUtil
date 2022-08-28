/*
 * Copyright (c) 2022 Sam Johnson
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
import com.samrj.devil.geo3d.Geo3D;
import com.samrj.devil.math.Mat3;
import com.samrj.devil.math.Vec2;
import com.samrj.devil.math.Vec3;
import com.samrj.devil.util.TriConsumer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.IntFunction;

import static org.lwjgl.system.MemoryUtil.memAlloc;
import static org.lwjgl.system.MemoryUtil.memFree;

/**
 * Blender mesh object.
 * 
 * @author Samuel Johnson (SmashMaster)
 */
public final class Mesh extends DataBlockAnimatable
{
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

    private record LoopEdge(int va, int vb)
    {
        @Override
        public int hashCode()
        {
            return va*vb;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (!(obj instanceof LoopEdge)) return false;
            LoopEdge edge = (LoopEdge)obj;
            return (va == edge.va && vb == edge.vb) || (va == edge.vb && vb == edge.va);
        }
    }

    public final boolean hasTangents;
    public final int numGroups; //Maximum number of groups per vertex, not total number of groups.
    public final List<String> vertexGroups;
    public final boolean hasMaterials;
    
    public final String[] uvLayers, colorLayers;
    public final int numVertices;
    public final ByteBuffer vertexData;
    
    public final int numTriangles;
    public final ByteBuffer indexData;
    
    public final int numEdges;
    public final ByteBuffer edgeIndexData;
    
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
        int totcol = bMesh.getField("totcol").asShort();
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
        Vec3[] vertSmoothNormals = new Vec3[totvert];
        for (int i=0; i<totvert; i++)
        {
            verts[i] = mVerts[i].getField("co").asVec3();
            vertSmoothNormals[i] = new Vec3();
        }
        
        int totloop = bMesh.getField("totloop").asInt();
        BlendFile.Pointer[] mLoops = bMesh.getField("mloop").dereference().asArray(totloop);
        Vec3[] loopNormals = new Vec3[totloop];
        int[] loopMats = materials.length == 0 ? null : new int[totloop];
        
        int totpoly = bMesh.getField("totpoly").asInt();
        BlendFile.Pointer[] mPolys = bMesh.getField("mpoly").dereference().asArray(totpoly);
        List<LoopTri> loopTris = new ArrayList<>();
        Set<LoopEdge> loopEdges = new HashSet<>();
        Vec3[] polyFlatNormals = new Vec3[totpoly];
        
        for (int iPoly=0; iPoly<totpoly; iPoly++)
        {
            BlendFile.Pointer mPoly = mPolys[iPoly];

            int start = mPoly.getField("loopstart").asInt();
            int count = mPoly.getField("totloop").asInt();
            int end = start + count;

            //Calculate polygon flat normal by Newell's method.
            Vec3 flatNormal = new Vec3();
            polyFlatNormals[iPoly] = flatNormal;

            for (int i0 = start; i0 < end; i0++)
            {
                int i1 = i0 + 1;
                if (i1 == end) i1 = start;

                Vec3 v0 = verts[mLoops[i0].getField("v").asInt()];
                Vec3 v1 = verts[mLoops[i1].getField("v").asInt()];

                flatNormal.x += (v0.y - v1.y)*(v0.z + v1.z);
                flatNormal.y += (v0.z - v1.z)*(v0.x + v1.x);
                flatNormal.z += (v0.x - v1.x)*(v0.y + v1.y);
            }

            float flatNormalLength = flatNormal.length();
            if (flatNormalLength == 0.0f) flatNormal.z = 1.0f;
            else flatNormal.div(flatNormalLength);

            //Accumulate flat normal weighted by corner angles into each vertex's smooth normal.
            //TODO: Optimise this.
            for (int i = start; i < end; i++)
            {
                int iPrev = i - 1, iNext = i + 1;
                if (iPrev == start - 1) iPrev = end - 1;
                if (iNext == end) iNext = start;

                int mVertIndex = mLoops[i].getField("v").asInt();

                //Could optimise this.
                Vec3 vPrev = verts[mLoops[iPrev].getField("v").asInt()];
                Vec3 v = verts[mVertIndex];
                Vec3 vNext = verts[mLoops[iNext].getField("v").asInt()];

                Vec3 edge0 = Vec3.sub(v, vPrev).normalize();
                Vec3 edge1 = Vec3.sub(vNext, v).normalize();

                float angle = (float)Math.acos(edge0.dot(edge1)); //Unsafe.

                vertSmoothNormals[mVertIndex].madd(flatNormal, angle);
            }

            //Triangulate the face
            if (count < 3) //Degenerate poly
            {
            }
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
                Mat3 basis = Geo3D.orthonormalBasis(flatNormal);
                basis.g = 0.0f;
                basis.h = 0.0f;
                basis.i = 0.0f;
                double[] projData = new double[count*2];
                for (int i = 0; i < count; i++)
                {
                    Vec3 v = verts[mLoops[start + i].getField("v").asInt()];
                    Vec3 projected = Vec3.mult(v, basis);

                    projData[i*2] = projected.x;
                    projData[i*2 + 1] = projected.y;
                }

                //Compute the triangulation
                List<Integer> triangulated = Earcut.earcut(projData);
                for (int i = 0; i < triangulated.size(); )
                {
                    int a = triangulated.get(i++);
                    int b = triangulated.get(i++);
                    int c = triangulated.get(i++);

                    loopTris.add(new LoopTri(start, a, b, c));
                }
            }

            //Make edges
            int eiLast = end - 1;
            for (int ei = start; ei < end; ei++)
            {
                loopEdges.add(new LoopEdge(eiLast, ei));
                eiLast = ei;
            }

            //Store normals
            boolean isSmooth = (mPoly.getField("flag").asByte() & 1) != 0;
            if (isSmooth)
                for (int i=start; i<end; i++)
                    loopNormals[i] = vertSmoothNormals[mLoops[i].getField("v").asInt()]; //Warning: these aren't normalized yet.
            else for (int i=start; i<end; i++)
                    loopNormals[i] = polyFlatNormals[iPoly];
            
            //Store face material
            if (loopMats != null)
            {
                int polyMat = materials[mPoly.getField("mat_nr").asShort()].get().modelIndex;
                for (int i=start; i<end; i++)
                    loopMats[i] = polyMat;
            }
        }

        for (Vec3 smoothNormal : vertSmoothNormals) smoothNormal.normalize();
        
        //Prepare vertex group data
        vertexGroups = new ArrayList<>();
        BlendFile.Pointer bVGroups = bMesh.getField("vertex_group_names");
        if (bVGroups != null) for (BlendFile.Pointer group : bVGroups.asList("bDeformGroup"))
            vertexGroups.add(group.getField("name").asString());

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
        numEdges = loopEdges.size();

        hasTangents = !uvLayerNames.isEmpty();
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

            vertexData.rewind();
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
            indexData.rewind();
        }
        
        int edgeIndexInts = numEdges*2;
        edgeIndexData = numEdges != 0 ? memAlloc(edgeIndexInts*4) : null;
        if (edgeIndexData != null)
        {
            for (LoopEdge loopEdge : loopEdges)
            {
                edgeIndexData.putInt(loopEdge.va);
                edgeIndexData.putInt(loopEdge.vb);
            }
            edgeIndexData.rewind();
        }

        //Calculate tangents & bitangents. Must be done after indices.
        if (vertexData != null && hasTangents)
        {
            Vec3[] tangent = new Vec3[numVertices];
            Vec3[] bitangent = new Vec3[numVertices];

            for (int i=0; i<numVertices; i++)
            {
                tangent[i] = new Vec3();
                bitangent[i] = new Vec3();
            }

            for (int tri=0; tri<numTriangles; tri++)
            {
                int i0 = indexData.getInt();
                int i1 = indexData.getInt();
                int i2 = indexData.getInt();

                Vec3 p0 = getPosition(i0);
                Vec3 p1 = getPosition(i1);
                Vec3 p2 = getPosition(i2);
                Vec2 w0 = getUV(i0);
                Vec2 w1 = getUV(i1);
                Vec2 w2 = getUV(i2);

                Vec3 e1 = Vec3.sub(p1, p0), e2 = Vec3.sub(p2, p0);
                float x1 = w1.x - w0.x, x2 = w2.x - w0.x;
                float y1 = w1.y - w0.y, y2 = w2.y - w0.y;

                float r = 1.0f/(x1*y2 - x2*y1);
                Vec3 t = Vec3.mult(e1, y2).sub(Vec3.mult(e2, y1)).mult(r);
                Vec3 b = Vec3.mult(e2, x1).sub(Vec3.mult(e1, x2)).mult(r);

                tangent[i0].add(t);
                tangent[i1].add(t);
                tangent[i2].add(t);
                bitangent[i0].add(b);
                bitangent[i1].add(b);
                bitangent[i2].add(b);
            }
            indexData.rewind();

            vertexData.position(tangentOffset);
            for (int i=0; i<numVertices; i++)
            {
                Vec3 t = tangent[i];
                Vec3 b = bitangent[i];
                Vec3 n = getNormal(i);

                Vec3 vTangent = Vec3.reject(t, n).normalize();
                vertexData.putFloat(vTangent.x);
                vertexData.putFloat(vTangent.y);
                vertexData.putFloat(vTangent.z);

                //Might need handedness later
//                boolean handedness = Vec3.cross(t, b).dot(n) > 0.0f;
            }
            vertexData.rewind();
        }
    }

    /**
     * Returns a new vector of the position of the vertex at the given index.
     */
    public Vec3 getPosition(int index)
    {
        int offset = positionOffset + index*12;
        return new Vec3(vertexData.getFloat(offset),
                        vertexData.getFloat(offset + 4),
                        vertexData.getFloat(offset + 8));
    }

    public Vec3 getNormal(int index)
    {
        int offset = normalOffset + index*12;
        return new Vec3(vertexData.getFloat(offset),
                        vertexData.getFloat(offset + 4),
                        vertexData.getFloat(offset + 8));
    }

    /**
     * Returns the first UV value of the vertex at the given index.
     */
    public Vec2 getUV(int index)
    {
        int offset = uvOffsets[0] + index*8;
        return new Vec2(vertexData.getFloat(offset),
                        vertexData.getFloat(offset + 4));
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
        for (MeshVertex v : vertices) for (int i=0; i<numGroups; i++)
            v.groupIndex[i] = vertexData.getInt();
        for (MeshVertex v : vertices) for (int i=0; i<numGroups; i++)
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
    public <V> void forEachTriangle(IntFunction<V> vertexFunction, TriConsumer<V, V, V> consumer)
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
    public void forEachTriangle(TriConsumer<MeshVertex, MeshVertex, MeshVertex> consumer)
    {
        MeshVertex[] vertices = readVertices();
        forEachTriangle(i -> vertices[i], consumer);
    }
    
    /**
     * Reads the edge indices for this mesh, performing the given operation on
     * each edge in this mesh, using the given index function.
     */
    public <V> void forEachEdge(IntFunction<V> vertexFunction, BiConsumer<V, V> consumer)
    {
        if (edgeIndexData == null) return;
        
        edgeIndexData.rewind();
        for (int i=0; i<numTriangles; i++)
        {
            V a = vertexFunction.apply(edgeIndexData.getInt());
            V b = vertexFunction.apply(edgeIndexData.getInt());
            consumer.accept(a, b);
        }
        edgeIndexData.rewind();
    }
    
    /**
     * Reads this mesh data, then performs the given operation on each edge.
     */
    public void forEachEdge(BiConsumer<MeshVertex, MeshVertex> consumer)
    {
        MeshVertex[] vertices = readVertices();
        forEachEdge(i -> vertices[i], consumer);
    }
    
    @Override
    void destroy()
    {
        if (vertexData != null) memFree(vertexData);
        if (indexData != null) memFree(indexData);
        if (edgeIndexData != null) memFree(edgeIndexData);
    }
    
    public class MeshVertex
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
    }
}
