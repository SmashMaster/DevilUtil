package com.samrj.devil.buffer;

import com.samrj.devil.math.Util;
import static com.samrj.devil.math.Util.PrimType.INT;

/**
 * Implementation of Buffer, using java.nio.IntBuffer.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class IntBuffer extends Buffer<java.nio.IntBuffer>
                       implements Bufferable<IntBuffer>
{
    public static IntBuffer wrap(int... data)
    {
        IntBuffer out = new IntBuffer(data.length);
        out.put(data);
        return out;
    }
    
    public IntBuffer(int capacity)
    {
        super(capacity, INT);
    }
    
    public void put(int... data)
    {
        write(data.length);
        buffer().put(data);
    }
    
    public void put(IntBuffer data)
    {
        write(data.size());
        buffer().put(data.get());
    }
    
    public void put(Bufferable<IntBuffer> data)
    {
        data.putIn(this);
    }

    @Override
    java.nio.IntBuffer buffer()
    {
        return byteBuffer().asIntBuffer();
    }
    
    @Override
    public Util.PrimType getType()
    {
        return Util.PrimType.INT;
    }
    
    @Override
    public void putIn(IntBuffer buf)
    {
        buf.put(this);
    }
}
