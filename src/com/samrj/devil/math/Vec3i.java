package com.samrj.devil.math;

import com.samrj.devil.util.Bufferable;
import com.samrj.devil.util.DataStreamable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

/**
 * Basic 3D integer vector class.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2020 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Vec3i implements Bufferable, DataStreamable
{
    public static final float dot(Vec3i v0, Vec3i v1)
    {
        return v0.x*v1.x + v0.y*v1.y + v0.z*v1.z;
    }
    
    public static final void add(Vec3i v0, Vec3i v1, Vec3i result)
    {
        result.x = v0.x + v1.x;
        result.y = v0.y + v1.y;
        result.z = v0.z + v1.z;
    }
    
    public static final void sub(Vec3i v0, Vec3i v1, Vec3i result)
    {
        result.x = v0.x - v1.x;
        result.y = v0.y - v1.y;
        result.z = v0.z - v1.z;
    }
    
    public static final void mult(Vec3i v, int s, Vec3i result)
    {
        result.x = v.x*s;
        result.y = v.y*s;
        result.z = v.z*s;
    }
    
    public static final void madd(Vec3i v0, Vec3i v1, int s, Vec3i result)
    {
        result.x = v0.x + v1.x*s;
        result.y = v0.y + v1.y*s;
        result.z = v0.z + v1.z*s;
    }
    
    public static final void cross(Vec3i v0, Vec3i v1, Vec3i result)
    {
        int x = v0.y*v1.z - v0.z*v1.y;
        int y = v0.z*v1.x - v0.x*v1.z;
        int z = v0.x*v1.y - v0.y*v1.x;
        result.x = x; result.y = y; result.z = z;
    }
    
    public static final void div(Vec3i v, int s, Vec3i result)
    {
        result.x = v.x/s;
        result.y = v.y/s;
        result.z = v.z/s;
    }
    
    public static final void negate(Vec3i v, Vec3i result)
    {
        result.x = -v.x;
        result.y = -v.y;
        result.z = -v.z;
    }
    
    public static final Vec3i add(Vec3i v0, Vec3i v1)
    {
        Vec3i result = new Vec3i();
        add(v0, v1, result);
        return result;
    }
    
    public static final Vec3i sub(Vec3i v0, Vec3i v1)
    {
        Vec3i result = new Vec3i();
        sub(v0, v1, result);
        return result;
    }
    
    public static final Vec3i mult(Vec3i v, int s)
    {
        Vec3i result = new Vec3i();
        mult(v, s, result);
        return result;
    }
    
    public static final Vec3i madd(Vec3i v0, Vec3i v1, int s)
    {
        Vec3i result = new Vec3i();
        madd(v0, v1, s, result);
        return result;
    }
    
    public static final Vec3i cross(Vec3i v0, Vec3i v1)
    {
        Vec3i result = new Vec3i();
        cross(v0, v1, result);
        return result;
    }
    
    public static final Vec3i div(Vec3i v, int s)
    {
        Vec3i result = new Vec3i();
        div(v, s, result);
        return result;
    }
    
    public static final Vec3i negate(Vec3i v)
    {
        Vec3i result = new Vec3i();
        negate(v, result);
        return result;
    }
    
    public int x, y, z;
    
    public Vec3i()
    {
    }
    
    public Vec3i(int x, int y, int z)
    {
        this.x = x; this.y = y; this.z = z;
    }
    
    public Vec3i(Vec3i v)
    {
        x = v.x; y = v.y; z = v.z;
    }
    
    public Vec3i(DataInputStream in) throws IOException
    {
        Vec3i.this.read(in);
    }
    
    public float dot(Vec3i v)
    {
        return dot(this, v);
    }
    
    public Vec3i set()
    {
        x = 0; y = 0; z = 0;
        return this;
    }
    
    public Vec3i set(int x, int y, int z)
    {
        this.x = x; this.y = y; this.z = z;
        return this;
    }
    
    public Vec3i set(Vec3i v)
    {
        x = v.x; y = v.y; z = v.z;
        return this;
    }
    
    public Vec3i add(Vec3i v)
    {
        add(this, v, this);
        return this;
    }
    
    public Vec3i sub(Vec3i v)
    {
        sub(this, v, this);
        return this;
    }

    public Vec3i mult(int s)
    {
        mult(this, s, this);
        return this;
    }
    
    public Vec3i madd(Vec3i v, int s)
    {
        madd(this, v, s, this);
        return this;
    }
    
    public Vec3i cross(Vec3i v)
    {
        cross(this, v, this);
        return this;
    }
    
    public Vec3i div(int s)
    {
        div(this, s, this);
        return this;
    }
    
    public Vec3i negate()
    {
        negate(this, this);
        return this;
    }
    
    @Override
    public void read(ByteBuffer buffer)
    {
        x = buffer.getInt();
        y = buffer.getInt();
        z = buffer.getInt();
    }

    @Override
    public void write(ByteBuffer buffer)
    {
        buffer.putInt(x);
        buffer.putInt(y);
        buffer.putInt(z);
    }
    
    public void read(IntBuffer buffer)
    {
        x = buffer.get();
        y = buffer.get();
        z = buffer.get();
    }
    
    public void write(IntBuffer buffer)
    {
        buffer.put(x);
        buffer.put(y);
        buffer.put(z);
    }

    @Override
    public int bufferSize()
    {
        return 12;
    }
    
    @Override
    public void read(DataInputStream in) throws IOException
    {
        x = in.readInt();
        y = in.readInt();
        z = in.readInt();
    }

    @Override
    public void write(DataOutputStream out) throws IOException
    {
        out.writeInt(x);
        out.writeInt(y);
        out.writeInt(z);
    }
    
    @Override
    public String toString()
    {
        return "(" + x + ", " + y + ", " + z + ")";
    }
    
    public boolean equals(Vec3i v)
    {
        if (v == null) return false;
        return x == v.x && y == v.y && z == v.z;
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (o == null) return false;
        if (o.getClass() != this.getClass()) return false;
        final Vec3i v = (Vec3i)o;
        return equals(v);
    }

    @Override
    public int hashCode()
    {
        int hash = 57 + this.x;
        hash = 19*hash + this.y;
        return 19*hash + this.z;
    }
}
