package com.samrj.devil.geo3d;

import com.samrj.devil.math.Vec3;

public class Tri3Direct implements Triangle3
{
    public final Vec3 a, b, c;

    public Tri3Direct(Vec3 a, Vec3 b, Vec3 c)
    {
        this.a = a;
        this.b = b;
        this.c = c;
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

    @Override
    public Vec3 c()
    {
        return c;
    }
}
