package com.samrj.devil.geo2d;

import com.samrj.devil.math.Util;
import com.samrj.devil.math.Vec2;
import org.lwjgl.opengl.GL11;

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
    public static Box lineBox(Vec2 a, Vec2 b, float padTan, float padNrm)
    {
        if (a.equals(b)) return new Box(padTan, padNrm, a);
        
        Box out = new Box();
        out.dir.set(b).sub(a);
        out.pos.set(a).add(b).mult(0.5f);
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
    
    public static Box fromAngle(float w, float h, Vec2 pos, float angle)
    {
        Box out = new Box();
        out.dir.set((float)Math.cos(angle), (float)Math.sin(angle));
        out.pos.set(pos);
        out.rx = w/2f;
        out.ry = h/2f;
        return out;
    }
    
    public static Box fromAAB(AAB aab)
    {
        Box out = new Box();
        out.pos.set(aab.center());
        Vec2 size = aab.size().div(2f);
        out.rx = size.x;
        out.ry = size.y;
        return out;
    }
    
    public static Box fromCorners(Vec2 a, Vec2 b, Vec2 c)
    {
        Box out = new Box();
        out.pos.set(Vec2.add(a, c).mult(0.5f));
        Vec2 dir = Vec2.sub(b, a);
        float sizeX = dir.length();
        out.rx = sizeX/2f;
        dir.div(sizeX);
        out.ry = c.dist(b)/2f;
        out.dir.set(dir);
        return out;
    }
    
    public final Vec2 pos = new Vec2();
    public final Vec2 dir = new Vec2(1f, 0f);
    public float rx = 0f, ry = 0f;
    
    public Box() {}
    
    public Box(float w, float h)
    {
        rx = w/2f;
        ry = h/2f;
    }
    
    public Box(float w, float h, Vec2 p)
    {
        this(w, h);
        pos.set(p);
    }
    
    public Box(float w, float h, Vec2 p, Vec2 d)
    {
        this(w, h, p);
        Vec2.normalize(d, dir);
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
    
    public Box translate(Vec2 v)
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
    
    public boolean contains(Vec2 v)
    {
        v = Vec2.sub(v, pos);
        v.set(v.x*dir.y - v.y*dir.x, v.x*dir.x + v.y*dir.y);
        return Util.isZero(v.x, ry) &&
               Util.isZero(v.y, rx);
    }
    
    public Vec2[] vertices()
    {
        Vec2 x = Vec2.mult(dir, rx);
        Vec2 y = new Vec2(-dir.y, dir.x).mult(ry);
        
        Vec2[] out = {
            Vec2.negate(x).sub(y).add(pos),
            Vec2.negate(x).add(y).add(pos),
            Vec2.add(x, y).add(pos),
            Vec2.sub(x, y).add(pos)};
        
        return out;
    }
    
    public void glVertex()
    {
        for (Vec2 v : vertices()) GL11.glVertex2f(v.x, v.y);
    }
    
    @Override
    public Box clone()
    {
        return new Box(this);
    }
}
