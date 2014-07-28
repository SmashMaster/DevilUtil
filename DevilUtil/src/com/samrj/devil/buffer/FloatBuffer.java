package com.samrj.devil.buffer;

import static com.samrj.devil.math.Util.PrimType.FLOAT;

/**
 * Implementation of Buffer, using java.nio.FloatBuffer.
 * 
 * @author Samuel Johnson (SmashMaster)
 */

public class FloatBuffer extends Buffer<java.nio.FloatBuffer>
                         implements Bufferable<FloatBuffer>
{
    public static FloatBuffer wrap(float... data)
    {
        FloatBuffer out = new FloatBuffer(data.length);
        out.put(data);
        return out;
    }
    
    public FloatBuffer(int capacity)
    {
        super(capacity, FLOAT);
    }
    
    public void put(float... data)
    {
        write(data.length);
        buffer().put(data);
    }
    
    public void put(FloatBuffer data)
    {
        write(data.size());
        buffer().put(data.get());
    }
    
    public void put(Bufferable<FloatBuffer> data)
    {
        data.putIn(this);
    }
    
    public void put(Bufferable<FloatBuffer>... data)
    {
        for (Bufferable<FloatBuffer> b : data) b.putIn(this);
    }

    @Override
    java.nio.FloatBuffer buffer()
    {
        return byteBuffer().asFloatBuffer();
    }

    @Override
    public void putIn(FloatBuffer buf)
    {
        buf.put(this);
    }
}