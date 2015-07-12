package com.samrj.devil.geo2d;

import com.samrj.devil.math.Vec2;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Seg extends Line
{
    public Seg(Vec2 a, Vec2 b)
    {
        super(a, b);
    }
    
    @Override public Line set(Vec2 a, Vec2 b)
    {
        this.a.set(a);
        this.b.set(b);
        return super.set(a, b);
    }
    
    @Override public Line translate(Vec2 v)
    {
        return super.translate(v);
    }
    
    @Override public float dist(Vec2 v)
    {
        float t = projScalT(v);
        
        if (t >= 1f) return b.dist(v);
        if (t <= 0f) return a.dist(v);
        
        return super.dist(v);
    }
    
    @Override public Vec2 projVec(Vec2 v)
    {
        float t = projScalT(v);
        
        if (t >= 1f) return new Vec2(b);
        if (t <= 0f) return new Vec2(a);
        
        return super.projVec(v);
    }
}
