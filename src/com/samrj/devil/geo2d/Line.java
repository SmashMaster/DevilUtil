package com.samrj.devil.geo2d;

import com.samrj.devil.math.Util;
import com.samrj.devil.math.Vector2f;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Line
{
    public final Vector2f a = new Vector2f(), b = new Vector2f();
    
    public Line(Vector2f a, Vector2f b)
    {
        set(a, b);
    }
    
    public Line set(Vector2f a, Vector2f b)
    {
        this.a.set(a);
        this.b.set(b);
        return this;
    }
    
    public Line translate(Vector2f v)
    {
        a.add(v);
        b.add(v);
        return this;
    }
    
    public Vector2f intersect(Line l)
    {
        Vector2f r = b.csub(a);
        Vector2f s = l.b.csub(l.a);
        
        float rxs = r.cross(s);
        
        if (rxs == 0f) return null;
        
        s.div(rxs);
        
        float t = l.a.csub(a).cross(s);
        return r.mult(t).add(a);
    }
    
    public int side(Vector2f v)
    {
        Vector2f d = b.csub(a);
        Vector2f w = v.csub(a);
        return Util.signum(d.cross(w));
    }
    
    public float sigDist(Vector2f v)
    {
        Vector2f d = b.csub(a).normalize();
        Vector2f w = v.csub(a);
        return d.cross(w);
    }
    
    public float dist(Vector2f v)
    {
        return Math.abs(sigDist(v));
    }
    
    public Vector2f normal()
    {
        Vector2f ab = b.csub(a);
        return ab.crotCCW().normalize();
    }
    
    public float projScalT(Vector2f v)
    {
        Vector2f d = v.csub(a);
        Vector2f ab = b.csub(a);
        return d.dot(ab)/ab.squareLength();
    }
    
    public int projSide(Vector2f v)
    {
        float t = projScalT(v);
        if (t > 1f) return 1;
        if (t < 0f) return -1;
        return 0;
    }
    
    public Vector2f projVec(Vector2f v)
    {
        Vector2f d = v.csub(a);
        Vector2f ab = b.csub(a);
        return d.projVec(ab).add(a);
    }
}