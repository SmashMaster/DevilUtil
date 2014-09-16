package com.samrj.devil.geo2d;

import com.samrj.devil.math.Util;
import com.samrj.devil.math.Vector2f;

/**
 * 2D unaligned box. Rotates around its origin. {@code rx} is its half-width
 * along {@code dir}. {@code ry} is its half-width along an axis perpendicular
 * to {@code dir}. {@code dir} is always assumed to be a unit vector.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class Box
{
    public static Box lineBox(Vector2f a, Vector2f b, float padTan, float padNrm)
    {
        if (a.equals(b)) return new Box(padTan, padNrm, a);
        
        Box out = new Box();
        out.dir.set(b).sub(a);
        out.pos.set(a).avg(b);
        float len = out.dir.length();
        out.dir.div(len);
        out.rx = len/2f + padTan;
        out.ry = padNrm;
        return out;
    }
    
    public static Box lineBox(Line line, float padTan, float padNrm)
    {
        return lineBox(line.a, line.b, padTan, padNrm);
    }
    
    public static Box fromAngle(float w, float h, Vector2f pos, float angle)
    {
        Box out = new Box();
        out.dir.set(Util.cos(angle), Util.sin(angle));
        out.pos.set(pos);
        out.rx = w/2f;
        out.ry = h/2f;
        return out;
    }
    
    public static Box fromAAB(AAB aab)
    {
        Box out = new Box();
        out.pos.set(aab.center());
        Vector2f size = aab.size().div(2f);
        out.rx = size.x;
        out.ry = size.y;
        return out;
    }
    
    public static Box fromCorners(Vector2f a, Vector2f b, Vector2f c)
    {
        Box out = new Box();
        out.pos.set(a.cavg(c));
        Vector2f dir = b.csub(a);
        float sizeX = dir.length();
        out.rx = sizeX/2f;
        dir.div(sizeX);
        out.ry = c.dist(b)/2f;
        out.dir.set(dir);
        return out;
    }
    
    public final Vector2f pos = new Vector2f();
    public final Vector2f dir = new Vector2f(1f, 0f);
    public float rx = 0f, ry = 0f;
    
    public Box() {}
    
    public Box(float w, float h)
    {
        rx = w/2f;
        ry = h/2f;
    }
    
    public Box(float w, float h, Vector2f p)
    {
        this(w, h);
        pos.set(p);
    }
    
    public Box(float w, float h, Vector2f p, Vector2f d)
    {
        this(w, h, p);
        dir.set(d.normalize());
    }
    
    public Box(Box box)
    {
        set(box);
    }
    
    public Box set(Box box)
    {
        pos.set(box.pos);
        dir.set(box.dir);
        rx = box.rx; ry = box.ry;
        return this;
    }
    
    public Box translate(Vector2f v)
    {
        pos.add(v);
        return this;
    }
    
    public boolean touches(Box box)
    {
        ConvexPoly thisPoly = new ConvexPoly(this);
        ConvexPoly boxPoly = new ConvexPoly(box);
        
        return thisPoly.touches(boxPoly);
    }
    
    public boolean contains(Vector2f v)
    {
        v = v.csub(pos).rotate(dir.x, dir.y);
        return Util.isZero(v.x, ry) &&
               Util.isZero(v.y, rx);
    }
    
    public Vector2f[] vertices()
    {
        Vector2f x = dir.cmult(rx);
        Vector2f y = dir.crotCCW().mult(ry);
        
        Vector2f[] out = {
            x.cnegate().sub(y).add(pos),
            x.cnegate().add(y).add(pos),
            x.cadd(y).add(pos),
            x.csub(y).add(pos)};
        
        return out;
    }
    
    public void glVertex()
    {
        for (Vector2f v : vertices()) v.glVertex();
    }
    
    @Override
    public Box clone()
    {
        return new Box(this);
    }
}
