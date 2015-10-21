package com.samrj.devil.io;

import java.nio.ByteBuffer;
import org.lwjgl.system.MemoryUtil;

/**
 * A dynamically resizing direct byte buffer. Automatically allocates a bigger
 * buffer every time the capacity is reached.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class DynamicBuffer
{
    private ByteBuffer buffer;
    private Memory memory;
    private boolean closed;
    
    public DynamicBuffer(int size)
    {
        memory = new Memory(size);
        buffer = memory.buffer;
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
        if (minCapacity - memory.size > 0) grow(minCapacity);
    }
    
    private void grow(int minCapacity)
    {
        int newCapacity = memory.size << 1;
        if (newCapacity - minCapacity < 0) newCapacity = minCapacity;
        if (newCapacity < 0)
        {
            if (minCapacity < 0) throw new OutOfMemoryError();
            newCapacity = Integer.MAX_VALUE;
        }
        
        Memory newMem = new Memory(newCapacity);
        
        int size = buffer.position();
        buffer = newMem.buffer;
        buffer.position(size);
        
        MemoryUtil.memCopy(memory.address, newMem.address, size);
        memory.free();
        memory = newMem;
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
     * @return The memory block that backs this buffer.
     */
    public Memory close()
    {
        ensureOpen();
        Memory out = memory;
        buffer = null;
        memory = null;
        closed = true;
        return out;
    }
}
