package com.samrj.devil.io;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;

/**
 * Static utility wrapper for a small block of native memory. Also includes some
 * factory classes for nio buffers in case you want to make your own buffers.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class BufferUtil
{
    private static final Memory utilMem = new Memory(4096);
    
    /**
     * Creates a byte buffer whose capacity (in bytes) is the given value.
     */
    @Deprecated
    public static final ByteBuffer createByteBuffer(int size)
    {
        return ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder());
    }
    
    /**
     * Creates a short buffer whose capacity (in shorts) is the given value.
     */
    @Deprecated
    public static final ShortBuffer createShortBuffer(int size)
    {
        return createByteBuffer(size << 1).asShortBuffer();
    }
    
    /**
     * Creates an int buffer whose capacity (in ints) is the given value.
     */
    @Deprecated
    public static final IntBuffer createIntBuffer(int size)
    {
        return createByteBuffer(size << 2).asIntBuffer();
    }
    
    /**
     * Creates a long buffer whose capacity (in longs) is the given value.
     */
    @Deprecated
    public static final LongBuffer createLongBuffer(int size)
    {
        return createByteBuffer(size << 3).asLongBuffer();
    }
    
    /**
     * Creates a float buffer whose capacity (in floats) is the given value.
     */
    @Deprecated
    public static final FloatBuffer createFloatBuffer(int size)
    {
        return createByteBuffer(size << 2).asFloatBuffer();
    }
    
    /**
     * Creates a double buffer whose capacity (in doubles) is the given value.
     */
    @Deprecated
    public static final DoubleBuffer createDoubleBuffer(int size)
    {
        return createByteBuffer(size << 3).asDoubleBuffer();
    }
    
    /**
     * Creates a char buffer whose capacity (in chars) is the given value.
     */
    @Deprecated
    public static final CharBuffer createCharBuffer(int size)
    {
        return createByteBuffer(size << 1).asCharBuffer();
    }
    
    /**
     * @see com.samrj.devil.io.Memory#capacity() 
     */
    public static final int capacity()
    {
        return utilMem.capacity();
    }
    
    /**
     * @see com.samrj.devil.io.Memory#alloc(int) 
     */
    public static final Block alloc(int capacity)
    {
        return utilMem.alloc(capacity);
    }
    
    /**
     * @see com.samrj.devil.io.Memory#free(com.samrj.devil.io.Block) 
     */
    public static final void free(Block block)
    {
        utilMem.free(block);
    }
    
    /**
     * @see com.samrj.devil.io.Memory#read(com.samrj.devil.io.Block) 
     */
    public static final ByteBuffer read(Block block)
    {
        return utilMem.read(block);
    }
    
    /**
     * @see com.samrj.devil.io.Memory#wrap(byte...) 
     */
    public static final Block wrap(byte... array)
    {
        return utilMem.wrap(array);
    }
    
    /**
     * @see com.samrj.devil.io.Memory#wraps(short...) 
     */
    public static final Block wraps(short... array)
    {
        return utilMem.wraps(array);
    }
    
    /**
     * @see com.samrj.devil.io.Memory#wrapi(int...) 
     */
    public static final Block wrapi(int... array)
    {
        return utilMem.wrapi(array);
    }
    
    /**
     * @see com.samrj.devil.io.Memory#wrapf(float...) 
     */
    public static final Block wrapf(float... array)
    {
        return utilMem.wrapf(array);
    }
    
    /**
     * @see com.samrj.devil.io.Memory#alloc(com.samrj.devil.io.Bufferable) 
     */
    public static final Block alloc(Bufferable obj)
    {
        return utilMem.alloc(obj);
    }
    
    /**
     * @see com.samrj.devil.io.Memory#root() 
     */
    public static final Block root()
    {
        return utilMem.root();
    }
}
