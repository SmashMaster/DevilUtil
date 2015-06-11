package com.samrj.devil.graphics.mesh;

import java.io.DataInputStream;
import java.io.IOException;

public class Mesh
{
    public final Vertex[] vertices;
    public final Triangle[] triangles;
    
    public Mesh(DataInputStream in) throws IOException
    {
        vertices = new Vertex[in.readInt()];
        for (int i=0; i<vertices.length; i++)
            vertices[i] = new Vertex(in);
        
        triangles = new Triangle[in.readInt()];
        for (int i=0; i<triangles.length; i++)
            triangles[i] = new Triangle(vertices, in);
    }
}
