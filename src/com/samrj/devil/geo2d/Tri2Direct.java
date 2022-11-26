package com.samrj.devil.geo2d;

import com.samrj.devil.math.Vec2;

public class Tri2Direct implements Triangle2
{
    public final Vec2 a, b, c;

    public Tri2Direct(Vec2 a, Vec2 b, Vec2 c)
    {
        this.a = a;
        this.b = b;
        this.c = c;
    }

    @Override
    public Vec2 a()
    {
        return a;
    }

    @Override
    public Vec2 b()
    {
        return b;
    }

    @Override
    public Vec2 c()
    {
        return c;
    }
}
