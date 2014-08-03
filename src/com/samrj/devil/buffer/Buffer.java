package com.samrj.devil.buffer;

import com.samrj.devil.math.Util.PrimType;
import java.nio.BufferOverflowException;
import org.lwjgl.BufferUtils;

/**
 * This class is an abstraction of java.nio.Buffer which ensures correct read/
 * write state, and allows for easy overwriting, resizing, and instantiation.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 * @param <TYPE> the java.nio buffer type to wrap.
 */
public abstract class Buffer<TYPE extends java.nio.Buffer>
{
    private java.nio.ByteBuffer buffer;
    private int size = 0, typeSize = 0;
    
    /**
     * Instantiates a buffer of the given capacity.
     * 
     * @param capacity the capacity of the new buffer, in elements.
     * @param type     the primitive type of this buffer.
     * @throws  IllegalArgumentException
     *          If capacity is less than or equal to zero, or if type has no
     *          size, as in the case of PrimType.BOOLEAN.
     */
    Buffer(int capacity, PrimType type)
    {
        if (type.size <= 0) throw new IllegalArgumentException();
        if (capacity <= 0) throw new IllegalArgumentException();
        
        typeSize = type.size;
        capacity *= typeSize;
        buffer = BufferUtils.createByteBuffer(capacity);
    }
    
    /**
     * Re-instantiates this buffer with a new capacity. Does nothing if the
     * given capacity is equal to the current capacity.
     * 
     * @param capacity the capacity of the new buffer.
     */
    public final void resize(int capacity)
    {
        if (capacity <= 0) throw new IllegalArgumentException();
        capacity *= typeSize;
        if (capacity == buffer.capacity()) return;
        
        read();
        if (buffer.limit() > capacity) buffer.limit(capacity);
        
        buffer = BufferUtils.createByteBuffer(capacity).put(buffer);
    }
    
    /**
     * Prepares this buffer for a series of write operations which will increase
     * its size by length.
     * 
     * @param length the number of elements to write to this buffer.
     * @return the java.nio buffer associated with this buffer.
     */
    public final TYPE write(int length)
    {
        length *= typeSize;
        
        if (length < 0) throw new IllegalArgumentException();
        if (length == 0) return buffer();
        if (size + length > buffer.capacity()) throw new BufferOverflowException();
        
        buffer.limit(buffer.capacity());
        buffer.position(size);
        
        size += length;
        return buffer();
    }
    
    private void read()
    {
        buffer.limit(size);
        buffer.position(0);
    }
    
    /**
     * Prepares this buffer to read from a subsequence.
     * 
     * @param offset the first element in the subsequence.
     * @param length the length of the subsequence.
     * @return the java.nio buffer associated with this buffer.
     */
    public final TYPE get(int offset, int length)
    {
        if (offset < 0 || length <= 0) throw new IllegalArgumentException();
        offset *= typeSize;
        length *= typeSize;
        
        final int newLimit = offset + length;
        if (offset > size || newLimit > size) throw new IllegalArgumentException();
       
        buffer.limit(newLimit);
        buffer.position(offset);
        
        return buffer();
    }
    
    /**
     * Prepares this buffer to be read entirely.
     * 
     * @return the java.nio buffer associated with this buffer.
     */
    public final TYPE get()
    {
        read();
        return buffer();
    }
    
    /**
     * Returns the size of this buffer, in elements.
     */
    public final int size()
    {
        return size/typeSize;
    }
    
    /**
     * Returns the capacity of this buffer, in elements.
     */
    public final int capacity()
    {
        return buffer.capacity()/typeSize;
    }
    
    /**
     * Returns the java.nio buffer type of this buffer.
     */
    abstract TYPE buffer();
    
    /**
     * Returns the ByteBuffer holding this buffer's data.
     */
    final java.nio.ByteBuffer byteBuffer()
    {
        return buffer;
    }
    
    /**
     * Clears this buffer. No data is actually deleted, meaning this method is
     * nearly instantaneous, but this buffer will behave as though it were
     * empty.
     */
    public final void clear()
    {
        buffer.limit(buffer.capacity());
        buffer.position(0);
        size = 0;
    }
}