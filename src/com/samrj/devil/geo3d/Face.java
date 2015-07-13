package com.samrj.devil.geo3d;

import com.samrj.devil.math.Vec3;

public class Face
{
    public Vec3 a, b, c;
    public Edge ab, bc, ca;
    
    public Face()
    {
    }
    
    public Face(Vec3 a, Vec3 b, Vec3 c)
    {
        this.a = a; this.b = b; this.c = c;
        ab = new Edge(a, b);
        bc = new Edge(b, c);
        ca = new Edge(c, a);
    }
}
