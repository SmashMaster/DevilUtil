package com.samrj.devil.graphics.mesh;

import java.io.DataInputStream;
import java.io.IOException;

public class Triangle
{
    public final Vertex a, b, c;
    public final float nx, ny, nz;
    
    public Triangle(Vertex[] vertices, DataInputStream in) throws IOException
    {
        a = vertices[in.readInt()];
        b = vertices[in.readInt()];
        c = vertices[in.readInt()];
        nx = in.readFloat();
        ny = in.readFloat();
        nz = in.readFloat();
    }
}
