package com.samrj.devil.buffer;

import com.samrj.devil.math.Util;
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
    public static ByteBuffer wrap(int... data)
    {
        ByteBuffer out = new ByteBuffer(data.length);
        out.put(data);
        return out;
    }
    
    public static ByteBuffer wrap(byte... data)
    {
        ByteBuffer out = new ByteBuffer(data.length);
        out.put(data);
        return out;
    }
    
    public ByteBuffer(int capacity)
    {
        super(capacity, BYTE);
    }
    
    public void put(int... data)
    {
        write(data.length);
        java.nio.ByteBuffer buf = buffer();
        for (int i : data) buf.put((byte)i);
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
    
    public void put(java.nio.ByteBuffer data)
    {
        write(data.remaining());
        buffer().put(data);
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
    public Util.PrimType getType()
    {
        return Util.PrimType.BYTE;
    }

    @Override
    public void putIn(ByteBuffer buf)
    {
        buf.put(this);
    }
}
