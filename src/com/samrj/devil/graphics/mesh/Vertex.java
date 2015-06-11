package com.samrj.devil.graphics.mesh;

import java.io.DataInputStream;
import java.io.IOException;

public class Vertex
{
    public final float x, y, z, nx, ny, nz;
    
    public Vertex(DataInputStream in) throws IOException
    {
        x = in.readFloat();
        y = in.readFloat();
        z = in.readFloat();
        nx = in.readFloat();
        ny = in.readFloat();
        nz = in.readFloat();
    }
}
