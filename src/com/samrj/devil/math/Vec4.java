package com.samrj.devil.math;

import com.samrj.devil.io.Bufferable;
import com.samrj.devil.io.Streamable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * 4D vector class.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Vec4 implements Bufferable, Streamable
{
    /**
     * Copies {@code source} into {@code target}.
     * 
     * @param source The vector to copy.
     * @param target The vector in which to store the result.
     */
    public static final void copy(Vec4 source, Vec4 target)
    {
        target.x = source.x;
        target.y = source.y;
        target.z = source.z;
        target.w = source.w;
    }
    
    public float x, y, z, w;
    
    /**
     * Creates a new zero vector.
     */
    public Vec4()
    {
    }
    
    /**
     * Creates a new vector with the given coordinates.
     */
    public Vec4(float x, float y, float z, float w)
    {
        this.x = x; this.y = y; this.z = z; this.w = w;
    }
    
    /**
     * Sets this to the given vector.
     * 
     * @param v The vector to set this to.
     * @return This vector.
     */
    public Vec4 set(Vec4 v)
    {
        copy(v, this);
        return this;
    }
    
    /**
     * Sets the coordinates of this vector.
     * 
     * @return This vector.
     */
    public Vec4 set(float x, float y, float z, float w)
    {
        this.x = x; this.y = y; this.z = z; this.w = w;
        return this;
    }
    
    /**
     * Sets each component of this vector to the given scalar.
     * 
     * @param s The scalar to set this to.
     * @return This vector.
     */
    public Vec4 set(float s)
    {
        x = s; y = s; z = s; w = s;
        return this;
    }
    
    /**
     * Sets this to the zero vector.
     * 
     * @return This vector.
     */
    public Vec4 set()
    {
        x = 0.0f; y = 0.0f; z = 0.0f; w = 0.0f;
        return this;
    }
    
    // <editor-fold defaultstate="collapsed" desc="Overriden/implemented methods">
    @Override
    public void read(ByteBuffer buffer)
    {
        x = buffer.getFloat();
        y = buffer.getFloat();
        z = buffer.getFloat();
        w = buffer.getFloat();
    }

    @Override
    public void write(ByteBuffer buffer)
    {
        buffer.putFloat(x);
        buffer.putFloat(y);
        buffer.putFloat(z);
        buffer.putFloat(w);
    }
    
    @Override
    public int bufferSize()
    {
        return 4*4;
    }

    @Override
    public void read(DataInputStream in) throws IOException
    {
        x = in.readFloat();
        y = in.readFloat();
        z = in.readFloat();
        w = in.readFloat();
    }

    @Override
    public void write(DataOutputStream out) throws IOException
    {
        out.writeFloat(x);
        out.writeFloat(y);
        out.writeFloat(z);
        out.writeFloat(w);
    }
    // </editor-fold>
}
