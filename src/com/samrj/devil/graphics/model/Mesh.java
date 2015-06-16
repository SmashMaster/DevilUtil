package com.samrj.devil.graphics.model;

import com.samrj.devil.buffer.BufferUtil;
import com.samrj.devil.math.Vector3f;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class Mesh
{
    public final String name;
    public final boolean hasUVs, hasVertexColors;
    public final String[] textures;
    
    public final int numVertices;
    public final FloatBuffer vertexData;
    public final int numTriangles;
    public final IntBuffer triangleIndexData;
    
    public Mesh(DataInputStream in) throws IOException
    {
        name = DevilModel.readPaddedUTF(in);
        
        int bitFlags = in.readInt();
        hasUVs = (bitFlags & 1) == 1;
        hasVertexColors = (bitFlags & 2) == 2;
        
        textures = new String[in.readInt()];
        for (int i=0; i<textures.length; i++)
            textures[i] = DevilModel.readPaddedUTF(in);
        
        //The order and length of vertex data is defined in export_dvm.py
        numVertices = in.readInt();
        int bytesPerVertex = 3 + 3;
        if (hasUVs) bytesPerVertex += 2;
        if (hasVertexColors) bytesPerVertex += 3;
        int vertexDataLength = numVertices*bytesPerVertex;
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
    
    public Triangle[] getTriangles()
    {
        rewindBuffers();
        
        Vector3f[] verts = new Vector3f[numVertices];
        for (int v=0; v<numVertices; v++)
        {
            verts[v] = new Vector3f(vertexData.get(),
                                    vertexData.get(),
                                    vertexData.get());
        }
        
        Triangle[] out = new Triangle[numTriangles];
        for (int t=0; t<numTriangles; t++)
        {
            out[t] = new Triangle(verts[triangleIndexData.get()],
                                  verts[triangleIndexData.get()],
                                  verts[triangleIndexData.get()]);
        }
        
        return out;
    }
    
    public class Triangle
    {
        public final Vector3f a, b, c;
        
        private Triangle(Vector3f a, Vector3f b, Vector3f c)
        {
            this.a = a; this.b = b; this.c = c;
        }
    }
}
