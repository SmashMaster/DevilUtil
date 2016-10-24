package com.samrj.devil.math;

import com.samrj.devil.io.Bufferable;
import java.nio.ByteBuffer;

/**
 * Basic 2D integer vector class.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2016 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Vec2i implements Bufferable
{
    public int x, y;
    
    public Vec2i()
    {
    }
    
    public Vec2i(int x, int y)
    {
        this.x = x; this.y = y;
    }
    
    public Vec2i(Vec2i v)
    {
        x = v.x; y = v.y;
    }
    
    public Vec2i set()
    {
        x = 0; y = 0;
        return this;
    }
    
    public Vec2i set(int x, int y)
    {
        this.x = x; this.y = y;
        return this;
    }
    
    public Vec2i set(Vec2i v)
    {
        x = v.x; y = v.y;
        return this;
    }
    
    @Override
    public void read(ByteBuffer buffer)
    {
        x = buffer.getInt();
        y = buffer.getInt();
    }

    @Override
    public void write(ByteBuffer buffer)
    {
        buffer.putInt(x);
        buffer.putInt(y);
    }

    @Override
    public int bufferSize()
    {
        return 8;
    }
    
    @Override
    public String toString()
    {
        return "(" + x + ", " + y + ")";
    }
    
    @Override
    public int hashCode()
    {
        int hash = 5;
        hash = 19 * hash + this.x;
        hash = 19 * hash + this.y;
        return hash;
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == null) return false;
        if (o.getClass() != this.getClass()) return false;
        final Vec2i v = (Vec2i)o;
        return v.x == x && v.y == y;
    }
}
