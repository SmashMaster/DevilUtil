package com.samrj.devil.geo2d;

import com.samrj.devil.math.Vec2;
import org.lwjgl.opengl.GL11;

/**
 * 2D Axis-aligned box.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class AAB
{
    // <editor-fold defaultstate="collapsed" desc="Static Factories">
    /**
     * Constructs infinitely exclusive AAB. In other words, this AAB does not
     * contain every point in 2D space. This means more than simply containing
     * nothing. Use with the expand function to create a bounding box from a set
     * of points.
     */
    public static AAB empty()
    {
        return new AAB(Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY,
                       Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY);
    }
    
    /**
     * Returns the minimum axis-aligned bounding box for a set of points.
     * 
     * @param points a set of points to bound.
     */
    public static AAB bounds(Vec2... points)
    {
        AAB out = empty();
        for (Vec2 v : points) out.expand(v);
        return out;
    }
    
    public static AAB fromHalfWidth(Vec2 pos, float rx, float ry)
    {
        return new AAB(pos.x - rx, pos.x + rx,
                       pos.y - ry, pos.y + ry);
    }
    // </editor-fold>
    
    public float x0, x1, y0, y1;
    
    public AAB(float x0, float x1, float y0, float y1)
    {
        set(x0, x1, y0, y1);
    }
    
    public AAB set(float x0, float x1, float y0, float y1)
    {
        this.x0 = x0; this.x1 = x1;
        this.y0 = y0; this.y1 = y1;
        return this;
    }
    
    public AAB set(AAB b)
    {
        return set(b.x0, b.x1, b.y0, b.y1);
    }
    
    public AAB mult(float s)
    {
        return set(x0*s, x1*s, y0*s, y1*s);
    }
    
    public AAB mult(float x, float y)
    {
        return set(x0*x, x1*x, y0*y, y1*y);
    }
    
    public AAB mult(Vec2 v)
    {
        return mult(v.x, v.y);
    }
    
    public AAB expandX(float x)
    {
        if (x < x0) x0 = x;
        if (x > x1) x1 = x;
        return this;
    }
    
    public AAB expandY(float y)
    {
        if (y < y0) y0 = y;
        if (y > y1) y1 = y;
        return this;
    }
    
    public AAB expand(Vec2 v)
    {
        expandX(v.x);
        expandY(v.y);
        return this;
    }
    
    public AAB expand(AAB b)
    {
        if (b.x0 < x0) x0 = b.x0;
        if (b.x1 > x1) x1 = b.x1;
        if (b.y0 < y0) y0 = b.y0;
        if (b.y1 > y1) y1 = b.y1;
        return this;
    }
    
    public AAB intersect(AAB b)
    {
        if (b.x0 > x0) x0 = b.x0;
        if (b.x1 < x1) x1 = b.x1;
        if (b.y0 > y0) y0 = b.y0;
        if (b.y1 < y1) y1 = b.y1;
        return this;
    }
    
    public AAB translate(Vec2 v)
    {
        x0 += v.x; x1 += v.x;
        y0 += v.y; y1 += v.y;
        return this;
    }
    
    public AAB setCenter(Vec2 v)
    {
        float rx = (x1 - x0)/2f;
        float ry = (y1 - y0)/2f;
        
        return set(v.x - rx, v.x + rx,
                   v.y - ry, v.y + ry);
    }
    
    /**
     * Returns true if there exists any point that lies on l and is contained by
     * this.
     */
    public boolean touches(Line l)
    {
        int sa = l.side(new Vec2(x0, y0));
        int sb = l.side(new Vec2(x0, y1));
        int sc = l.side(new Vec2(x1, y1));
        int sd = l.side(new Vec2(x1, y0));
        
        //The segment touches an edge or corner.
        if (sa == 0 || sb == 0 || sc == 0 || sd == 0) return true;
        
        //The segment passes through the area of this box.
        return !((sa == sb) && (sb == sc) && (sc == sd));
    }
    
    /**
     * Returns true if there exists any point that lies on s and is contained by
     * this.
     */
    public boolean touches(Seg s)
    {
        AAB sbox = AAB.bounds(s.a, s.b);
        if (!touches(sbox)) return false;
        return touches((Line)s);
    }
    
    /**
     * Returns true if there exists any point that is contained by both this and
     * b.
     */
    public boolean touches(AAB b)
    {
        return x1 >= b.x0 && b.x1 >= x0 &&
               y1 >= b.y0 && b.y1 >= y0;
    }
    
    /**
     * Returns true if the intersection between this and b creates a non-zero
     * area.
     */
    public boolean intersects(AAB b)
    {
        return x1 > b.x0 && b.x1 > x0 &&
               y1 > b.y0 && b.y1 > y0;
    }
    
    /**
     * Returns true if v lies inside this.
     */
    public boolean contains(Vec2 v)
    {
        return v.x >= x0 && v.x <= x1 &&
               v.y >= y0 && v.y <= y1;
    }
    
    /**
     * Returns true if b contains no point that this does not contain.
     */
    public boolean contains(AAB b)
    {
        return b.x0 >= x0 && x1 >= b.x1 &&
               b.y0 >= y0 && y1 >= b.y1;
    }
    
    public Vec2 size()
    {
        return new Vec2(x1 - x0, y1 - y0);
    }
    
    public Vec2 center()
    {
        return new Vec2(x0 + x1, y0 + y1).mult(.5f);
    }
    
    /**
     * Returns the point on this AAB that is closest to {@code v}.
     * 
     * @param v the vector to find the closest point to.
     */
    public Vec2 closest(Vec2 v)
    {
        Vec2 out = new Vec2(v);
        
        if (out.x > x1) out.x = x1;
        else if (out.x < x0) out.x = x0;
        
        if (out.y > y1) out.y = y1;
        else if (out.y < y0) out.y = y0;
        
        return out;
    }
    
    public float chebyDist(AAB box)
    {
        float[] distances = {x0 - box.x1,
                             box.x0 - x1,
                             y0 - box.y1,
                             box.y0 - y1};
        
        float min = Float.POSITIVE_INFINITY;
        for (float dist : distances) if (dist < min && dist >= 0f) min = dist;
        
        if (Float.isInfinite(min)) return 0f;
        else return min;
    }
    
    public float chebyDist(Vec2 v)
    {
        Vec2 center = center();
        
        float dx, dy;
        if (v.x >= center.x) dx = v.x - x1;
        else                 dx = x0 - v.x;
        
        if (v.y >= center.y) dy = v.y - y1;
        else                 dy = y0 - v.y;
        
        return Math.max(dx, dy);
    }
    
    /**
     * Returns the signed area of this box. Will always return a negative value
     * or zero if this box contains nothing.
     */
    public float area()
    {
        Vec2 size = size();
        if (size.x < 0 && size.y < 0) return -size.x*size.y;
        
        return size.x*size.y;
    }
    
    public float intersectionArea(AAB b)
    {
        return clone().intersect(b).area();
    }
    
    public float sharedArea(AAB b)
    {
        return area() + b.area() - intersectionArea(b);
    }
    
    /**
     * Returns whether or not this box has a negative or zero area. A box with
     * zero area may still contain points.
     */
    public boolean isEmpty()
    {
        return x1 <= x0 || y1 <= y0;
    }
    
    /**
     * Returns whether or not this box has a negative area, and therefore cannot
     * contain any point.
     */
    public boolean isNegative()
    {
        return x1 < x0 || y1 < y0;
    }
    
    public void normalize()
    {
        float temp;
        if (x1 < x0)
        {
            temp = x0;
            x0 = x1;
            x1 = temp;
        }
        
        if (y1 < y0)
        {
            temp = y0;
            y0 = y1;
            y1 = temp;
        }
    }
    
    public void glVertex()
    {
        GL11.glVertex2f(x0, y0);
        GL11.glVertex2f(x0, y1);
        GL11.glVertex2f(x1, y1);
        GL11.glVertex2f(x1, y0);
    }
    
    public void glTexVertex()
    {
        GL11.glTexCoord2f(0f, 0f); GL11.glVertex2f(x0, y0);
        GL11.glTexCoord2f(0f, 1f); GL11.glVertex2f(x0, y1);
        GL11.glTexCoord2f(1f, 1f); GL11.glVertex2f(x1, y1);
        GL11.glTexCoord2f(1f, 0f); GL11.glVertex2f(x1, y0);
    }
    
    @Override public String toString()
    {
        return "x[" + x0 + ", " + x1 + "]y[" + y0 + ", " + y1 + "]";
    }
    
    @Override public AAB clone()
    {
        return new AAB(x0, x1, y0, y1);
    }
    
    @Override public boolean equals(Object obj)
    {
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        final AAB other = (AAB) obj;
        if (Float.floatToIntBits(this.x0) != Float.floatToIntBits(other.x0)) return false;
        if (Float.floatToIntBits(this.x1) != Float.floatToIntBits(other.x1)) return false;
        if (Float.floatToIntBits(this.y0) != Float.floatToIntBits(other.y0)) return false;
        if (Float.floatToIntBits(this.y1) != Float.floatToIntBits(other.y1)) return false;
        return true;
    }
}
