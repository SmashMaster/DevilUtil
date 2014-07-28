package com.samrj.devil.geo2d;

import com.samrj.devil.math.Vector2f;

public class Seg extends Line
{
    public Seg(Vector2f a, Vector2f b)
    {
        super(a, b);
    }
    
    @Override public Line set(Vector2f a, Vector2f b)
    {
        this.a.set(a);
        this.b.set(b);
        return super.set(a, b);
    }
    
    @Override public Line translate(Vector2f v)
    {
        return super.translate(v);
    }
    
    @Override public float dist(Vector2f v)
    {
        float t = projScalT(v);
        
        if (t >= 1f) return b.dist(v);
        if (t <= 0f) return a.dist(v);
        
        return super.dist(v);
    }
    
    @Override public Vector2f projVec(Vector2f v)
    {
        float t = projScalT(v);
        
        if (t >= 1f) return b.clone();
        if (t <= 0f) return a.clone();
        
        return super.projVec(v);
    }
}