package com.samrj.devil.geo2d;

import com.samrj.devil.math.Vec2;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Line
{
    public final Vec2 a = new Vec2(), b = new Vec2();
    
    public Line(Vec2 a, Vec2 b)
    {
        set(a, b);
    }
    
    public Line set(Vec2 a, Vec2 b)
    {
        this.a.set(a);
        this.b.set(b);
        return this;
    }
    
    public Line translate(Vec2 v)
    {
        a.add(v);
        b.add(v);
        return this;
    }
    
    public Vec2 intersect(Line l)
    {
        Vec2 r = Vec2.sub(b, a);
        Vec2 s = Vec2.sub(l.b, l.a);
        
        float rxs = r.cross(s);
        
        if (rxs == 0f) return null;
        
        s.div(rxs);
        
        float t = Vec2.sub(l.a, a).cross(s);
        return r.mult(t).add(a);
    }
    
    public int side(Vec2 v)
    {
        Vec2 d = Vec2.sub(b, a);
        Vec2 w = Vec2.sub(v, a);
        return (int)Math.signum(d.cross(w));
    }
    
    public float sigDist(Vec2 v)
    {
        Vec2 d = Vec2.sub(b, a).normalize();
        Vec2 w = Vec2.sub(v, a);
        return d.cross(w);
    }
    
    public float dist(Vec2 v)
    {
        return Math.abs(sigDist(v));
    }
    
    public Vec2 normal()
    {
        Vec2 ab = Vec2.sub(b, a);
        return new Vec2(-ab.y, ab.x).normalize();
    }
    
    public float projScalT(Vec2 v)
    {
        Vec2 d = Vec2.sub(v, a);
        Vec2 ab = Vec2.sub(b, a);
        return d.dot(ab)/ab.squareLength();
    }
    
    public int projSide(Vec2 v)
    {
        float t = projScalT(v);
        if (t > 1f) return 1;
        if (t < 0f) return -1;
        return 0;
    }
    
    public Vec2 projVec(Vec2 v)
    {
        Vec2 d = Vec2.sub(v, a);
        Vec2 ab = Vec2.sub(b, a);
        return d.project(ab).add(a);
    }
}
