package com.samrj.devil.buffer;

import static com.samrj.devil.math.Util.PrimType.SHORT;

/**
 * Implementation of Buffer, using java.nio.ShortBuffer.
 * 
 * @author Samuel Johnson (SmashMaster)
 */

public class ShortBuffer extends Buffer<java.nio.ShortBuffer>
                         implements Bufferable<ShortBuffer>
{
    public ShortBuffer(int capacity)
    {
        super(capacity, SHORT);
    }
    
    public void put(short... data)
    {
        write(data.length);
        buffer().put(data);
    }
    
    public void put(ShortBuffer data)
    {
        write(data.size());
        buffer().put(data.get());
    }
    
    public void put(Bufferable<ShortBuffer> data)
    {
        data.putIn(this);
    }

    @Override
    java.nio.ShortBuffer buffer()
    {
        return byteBuffer().asShortBuffer();
    }
    
    @Override
    public void putIn(ShortBuffer buf)
    {
        buf.put(this);
    }
}