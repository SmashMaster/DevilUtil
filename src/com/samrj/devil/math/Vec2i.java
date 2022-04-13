package com.samrj.devil.math;

import com.samrj.devil.util.Bufferable;
import com.samrj.devil.util.DataStreamable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

/**
 * Basic 2D integer vector class.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2019 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Vec2i implements Bufferable, DataStreamable<Vec2i>
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

    public Vec2i(ByteBuffer buffer)
    {
        Vec2i.this.read(buffer);
    }

    public Vec2i(DataInputStream in) throws IOException
    {
        Vec2i.this.read(in);
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
    
    public void read(IntBuffer buffer)
    {
        x = buffer.get();
        y = buffer.get();
    }
    
    public void write(IntBuffer buffer)
    {
        buffer.put(x);
        buffer.put(y);
    }

    @Override
    public int bufferSize()
    {
        return 8;
    }

    @Override
    public Vec2i read(DataInputStream in) throws IOException
    {
        x = in.readInt();
        y = in.readInt();
        return this;
    }

    @Override
    public Vec2i write(DataOutputStream out) throws IOException
    {
        out.writeInt(x);
        out.writeInt(y);
        return this;
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
