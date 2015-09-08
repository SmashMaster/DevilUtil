package com.samrj.devil.io;

import java.nio.ByteBuffer;
import static org.lwjgl.system.MemoryUtil.*;

/**
 * Access to native memory.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class Memory
{
    private static boolean debug;
    private static int allocations;
    
    /**
     * Enables or disables debug mode, which stores stack traces for all memory
     * allocations and then prints them out when a memory leak is detected.
     * 
     * Defaults to false.
     * 
     * @param debug Whether to enable debug.
     */
    public static void debug(boolean debug)
    {
        Memory.debug = debug;
    }
    
    /**
     * @return The number of currently active allocations.
     */
    public static int allocations()
    {
        return allocations;
    }
    
    /**
     * Wraps the given array of bytes into a newly allocated block of memory.
     * 
     * @param array An array of bytes to wrap.
     * @return A newly allocated block of memory containing the given bytes.
     */
    public static Memory wrap(byte... array)
    {
        Memory mem = new Memory(array.length);
        mem.buffer.put(array);
        return mem;
    }
    
    /**
     * Wraps the given array of shorts into a newly allocated block of memory.
     * 
     * @param array An array of shorts to wrap.
     * @return A newly allocated block of memory containing the given shorts.
     */
    public static Memory wraps(short... array)
    {
        Memory mem = new Memory(array.length*2);
        ByteBuffer buffer = mem.buffer;
        for (short s : array) buffer.putShort(s);
        return mem;
    }
    
    /**
     * Wraps the given array of ints into a newly allocated block of memory.
     * 
     * @param array An array of ints to wrap.
     * @return A newly allocated block of memory containing the given ints.
     */
    public static Memory wrapi(int... array)
    {
        Memory mem = new Memory(array.length*4);
        ByteBuffer buffer = mem.buffer;
        for (int i : array) buffer.putInt(i);
        return mem;
    }
    
    /**
     * Wraps the given array of longs into a newly allocated block of memory.
     * 
     * @param array An array of longs to wrap.
     * @return A newly allocated block of memory containing the given longs.
     */
    public static Memory wrapl(long... array)
    {
        Memory mem = new Memory(array.length*8);
        ByteBuffer buffer = mem.buffer;
        for (long l : array) buffer.putLong(l);
        return mem;
    }
    
    /**
     * Wraps the given array of floats into a newly allocated block of memory.
     * 
     * @param array An array of floats to wrap.
     * @return A newly allocated block of memory containing the given floats.
     */
    public static Memory wrapf(float... array)
    {
        Memory mem = new Memory(array.length*4);
        ByteBuffer buffer = mem.buffer;
        for (float f : array) buffer.putFloat(f);
        return mem;
    }
    
    /**
     * Writes the given Bufferable into a newly allocated block of memory.
     * 
     * @param obj A bufferable to write.
     * @return A newly allocated block of memory containing the bufferable.
     */
    public static Memory wrap(Bufferable obj)
    {
        Memory mem = new Memory(obj.bufferSize());
        obj.write(mem.buffer);
        return mem;
    }
    
    /**
     * Writes each of the given bufferables into a newly allocated block of
     * memory.
     * 
     * @param array An array of bufferables to write.
     * @return A newly allocated block of memory containing each bufferable.
     */
    public static Memory wrapv(Bufferable... array)
    {
        int size = 0;
        for (Bufferable obj : array) size += obj.bufferSize();
        Memory mem = new Memory(size);
        for (Bufferable obj : array) obj.write(mem.buffer);
        return mem;
    }
    
    public final int size;
    public final ByteBuffer buffer;
    public final long address;
    private boolean free;
    private final Throwable debugLeakTrace;
    
    public Memory(int size)
    {
        if (size <= 0) throw new IllegalArgumentException();
        
        this.size = size;
        address = memAlloc(size);
        if (address == NULL) throw new RuntimeException("Failed to allocate memory.");
        buffer = memByteBuffer(address, size);
        allocations++;
        
        debugLeakTrace = debug ? new Throwable() : null;
    }
    
    public boolean isFree()
    {
        return free;
    }
    
    public void free()
    {
        if (free) throw new IllegalStateException();
        memFree(address);
        free = true;
        allocations--;
    }
    
    /**
     * @return The first byte in this memory block.
     */
    public byte readByte()
    {
        return memGetByte(address);
    }

    /**
     * @return The first short in this memory block.
     */
    public short readShort()
    {
        if (size < 2) throw new ArrayIndexOutOfBoundsException();
        return memGetShort(address);
    }

    /**
     * @return The first integer in this memory block.
     */
    public int readInt()
    {
        if (size < 4) throw new ArrayIndexOutOfBoundsException();
        return memGetInt(address);
    }

    /**
     * @return The first long in this memory block.
     */
    public long readLong()
    {
        if (size < 8) throw new ArrayIndexOutOfBoundsException();
        return memGetLong(address);
    }

    /**
     * @return The first float in this memory block.
     */
    public float readFloat()
    {
        if (size < 4) throw new ArrayIndexOutOfBoundsException();
        return memGetFloat(address);
    }

    /**
     * @return The first long in this memory block.
     */
    public double readDouble()
    {
        if (size < 8) throw new ArrayIndexOutOfBoundsException();
        return memGetDouble(address);
    }
    
    @Override
    public void finalize()
    {
        if (debug && !free)
        {
            System.err.println("Memory leaked: ");
            debugLeakTrace.printStackTrace();
        }
    }
}
