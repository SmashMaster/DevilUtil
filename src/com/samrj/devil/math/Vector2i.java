package com.samrj.devil.math;

/**
 * A very simple, immutable, 32 bit, 2 component integer vector class.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 */
public final class Vector2i
{
    public final int x, y;
    
    public Vector2i(int x, int y)
    {
        this.x = x; this.y = y;
    }
    
    public Vector2i()
    {
        this(0, 0);
    }
    
    @Override
    public String toString()
    {
        return "("+x+", "+y+")";
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (o.getClass() != Vector2i.class) return false;
        
        Vector2i v = (Vector2i)o;
        return v.x == x && v.y == y;
    }
}
