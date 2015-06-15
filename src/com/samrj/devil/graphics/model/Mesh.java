package com.samrj.devil.graphics.model;

import com.samrj.devil.buffer.BufferUtil;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class Mesh
{
    public final String name;
    public final int numVertices;
    public final FloatBuffer vertexData;
    public final int numTriangles;
    public final IntBuffer triangleIndexData;
    
    public Mesh(DataInputStream in) throws IOException
    {
        name = DevilModel.readPaddedUTF(in);
        
        numVertices = in.readInt();
        int vertexDataLength = numVertices*(3 + 3 + 2);
        vertexData = BufferUtil.createFloatBuffer(vertexDataLength);
        for (int i=0; i<vertexDataLength; i++)
            vertexData.put(in.readFloat());
        
        numTriangles = in.readInt();
        int triangleIndexDataLength = numTriangles*3;
        triangleIndexData = BufferUtil.createIntBuffer(triangleIndexDataLength);
        for (int i=0; i<triangleIndexDataLength; i++)
            triangleIndexData.put(in.readInt());
        
        rewindBuffers();
    }
    
    public final void rewindBuffers()
    {
        vertexData.rewind();
        triangleIndexData.rewind();
    }
}
