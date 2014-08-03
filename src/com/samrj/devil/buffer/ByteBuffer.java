package com.samrj.devil.buffer;

import static com.samrj.devil.math.Util.PrimType.BYTE;

/**
 * Implementation of Buffer, using java.nio.ByteBuffer.
 * 
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */

public class ByteBuffer extends Buffer<java.nio.ByteBuffer>
                        implements Bufferable<ByteBuffer>
{
    public ByteBuffer(int capacity)
    {
        super(capacity, BYTE);
    }
    
    public void put(byte... data)
    {
        write(data.length);
        buffer().put(data);
    }
    
    public void put(ByteBuffer data)
    {
        write(data.size());
        buffer().put(data.get());
    }
    
    public void put(Bufferable<ByteBuffer> data)
    {
        data.putIn(this);
    }

    @Override
    java.nio.ByteBuffer buffer()
    {
        return byteBuffer();
    }

    @Override
    public void putIn(ByteBuffer buf)
    {
        buf.put(this);
    }
}