package com.samrj.devil.graphics.model;

import com.samrj.devil.io.BufferUtil;
import com.samrj.devil.math.Vec3;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

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
    public final boolean hasUVs, hasVertexColors;
    public final String[] textures;
    public final int numVertexGroups;
    
    public final int numVertices;
    public final FloatBuffer vertexData;
    public final int numTriangles;
    public final IntBuffer triangleIndexData;
    
    public Mesh(DataInputStream in, Armature armature) throws IOException
    {
        name = DevilModel.readPaddedUTF(in);
        
        int bitFlags = in.readInt();
        hasUVs = (bitFlags & 1) == 1;
        hasVertexColors = (bitFlags & 2) == 2;
        
        textures = new String[in.readInt()];
        for (int i=0; i<textures.length; i++)
            textures[i] = DevilModel.readPaddedUTF(in);
        
        if (armature != null) numVertexGroups = in.readInt();
        else numVertexGroups = 0;
        
        //The order and length of vertex data is defined in export_dvm.py
        int floatsPerVertex = 3 + 3;
        if (hasUVs) floatsPerVertex += 2;
        if (hasVertexColors) floatsPerVertex += 3;
        floatsPerVertex += numVertexGroups*2;
        
        numVertices = in.readInt();
        int vertexDataLength = numVertices*floatsPerVertex;
        vertexData = BufferUtil.createFloatBuffer(vertexDataLength);
        for (int i=0; i<vertexDataLength; i++)
            vertexData.put(in.readFloat());
        
        numTriangles = in.readInt();
        int triangleIndexDataLength = numTriangles*3;
        triangleIndexData = BufferUtil.createIntBuffer(triangleIndexDataLength);
        for (int i=0; i<triangleIndexDataLength; i++)
            triangleIndexData.put(in.readInt());
    }
    
    public final void rewindBuffers()
    {
        vertexData.rewind();
        triangleIndexData.rewind();
    }
    
    public Geometry getGeometry()
    {
        rewindBuffers();
        
        Vec3[] verts = new Vec3[numVertices];
        for (int v=0; v<numVertices; v++)
        {
            verts[v] = new Vec3(vertexData.get(),
                                vertexData.get(),
                                vertexData.get());
        }
        
        Triangle[] tris = new Triangle[numTriangles];
        for (int t=0; t<numTriangles; t++)
        {
            tris[t] = new Triangle(verts[triangleIndexData.get()],
                                   verts[triangleIndexData.get()],
                                   verts[triangleIndexData.get()]);
        }
        
        return new Geometry(verts, tris);
    }
    
    public class Geometry
    {
        public final Vec3[] vertices;
        public final Triangle[] triangles;
        
        private Geometry(Vec3[] vertices, Triangle[] triangles)
        {
            this.vertices = vertices;
            this.triangles = triangles;
        }
    }
    
    public class Triangle
    {
        public final Vec3 a, b, c;
        
        private Triangle(Vec3 a, Vec3 b, Vec3 c)
        {
            this.a = a; this.b = b; this.c = c;
        }
        
        public Vec3 getCentroid()
        {
            return Vec3.add(a, b).add(c).div(3.0f);
        }
    }
}
