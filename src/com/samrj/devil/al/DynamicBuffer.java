package com.samrj.devil.al;

import java.nio.ByteBuffer;

import static org.lwjgl.system.MemoryUtil.*;

/**
 * A dynamically resizing direct byte buffer. Automatically allocates a bigger
 * buffer every time the capacity is reached.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2020 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class DynamicBuffer
{
    private ByteBuffer buffer;
    private boolean closed;
    
    public DynamicBuffer(int size)
    {
        buffer = memAlloc(size);
    }
    
    public DynamicBuffer()
    {
        this(32);
    }
    
    public boolean isOpen()
    {
        return !closed;
    }
    
    private void ensureOpen()
    {
        if (closed) throw new IllegalStateException("Dynamic buffer has been closed.");
    }
    
    private void ensureCapacity(int minCapacity)
    {
        if (minCapacity - buffer.capacity() > 0) grow(minCapacity);
    }
    
    private void grow(int minCapacity)
    {
        int newCapacity = buffer.capacity() << 1;
        if (newCapacity - minCapacity < 0) newCapacity = minCapacity;
        if (newCapacity < 0)
        {
            if (minCapacity < 0) throw new OutOfMemoryError();
            newCapacity = Integer.MAX_VALUE;
        }
        
        int size = buffer.position();
        buffer.flip();
        
        ByteBuffer newBuffer = memAlloc(newCapacity);
        memCopy(buffer, newBuffer);
        memFree(buffer);
        
        buffer = newBuffer;
        buffer.position(size);
    }
    
    public void put(byte b)
    {
        ensureOpen();
        ensureCapacity(buffer.position() + 1);
        buffer.put(b);
    }
    
    public void put(byte[] array)
    {
        ensureOpen();
        ensureCapacity(buffer.position() + array.length);
        buffer.put(array);
    }
    
    public void put(byte[] array, int offset, int length)
    {
        ensureOpen();
        ensureCapacity(buffer.position() + length);
        buffer.put(array, offset, length);
    }
    
    public void put(ByteBuffer buf)
    {
        ensureOpen();
        ensureCapacity(buffer.position() + buf.remaining());
        buffer.put(buf);
    }
    
    public void putChar(char c)
    {
        ensureOpen();
        ensureCapacity(buffer.position() + 2);
        buffer.putChar(c);
    }
    
    public void putShort(short s)
    {
        ensureOpen();
        ensureCapacity(buffer.position() + 2);
        buffer.putShort(s);
    }
    
    public void putInt(int i)
    {
        ensureOpen();
        ensureCapacity(buffer.position() + 4);
        buffer.putInt(i);
    }
    
    public void putLong(long l)
    {
        ensureOpen();
        ensureCapacity(buffer.position() + 8);
        buffer.putLong(l);
    }
    
    public void putFloat(float f)
    {
        ensureOpen();
        ensureCapacity(buffer.position() + 4);
        buffer.putFloat(f);
    }
    
    public void putDouble(double d)
    {
        ensureOpen();
        ensureCapacity(buffer.position() + 8);
        buffer.putDouble(d);
    }
    
    /**
     * Closes this dynamic buffer forever and returns the backing memory block.
     * This is the only way to access the data inside the data stored in this
     * buffer.
     * 
     * The returned ByteBuffer must be explicitly freed.
     * 
     * @return The memory block that backs this buffer.
     */
    public ByteBuffer close()
    {
        ensureOpen();
        ByteBuffer out = (ByteBuffer)buffer.flip();
        buffer = null;
        closed = true;
        return out;
    }
}
