package com.samrj.devil.math;

import com.samrj.devil.io.Bufferable;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

/**
 * Basic 3D integer vector class.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2019 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Vec3i implements Bufferable
{
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
    public String toString()
    {
        return "(" + x + ", " + y + ", " + z + ")";
    }
}
