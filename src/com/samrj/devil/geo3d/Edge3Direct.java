package com.samrj.devil.geo3d;

import com.samrj.devil.math.Vec3;

public class Edge3Direct implements  Edge3
{
    public final Vec3 a, b;

    public Edge3Direct(Vec3 a, Vec3 b)
    {
        this.a = a;
        this.b = b;
    }

    @Override
    public Vec3 a()
    {
        return a;
    }

    @Override
    public Vec3 b()
    {
        return b;
    }
}
