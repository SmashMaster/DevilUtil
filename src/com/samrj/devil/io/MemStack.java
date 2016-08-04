package com.samrj.devil.io;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

/**
 * Provides one kibibyte of always-accessible native memory for very frequently
 * called methods like ShaderProgram.uniformMat4(). Is not thread safe. Allows
 * for a maximum of 32 allocations at any given time.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2016 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class MemStack
{
    private static final int MAX_COUNT = 32;
    private static final int MAX_LENGTH = 1024;
    
    private static final Memory MEM = new Memory(MAX_LENGTH);
    private static final ByteBuffer BUF = MEM.buffer;
    private static final long ADDRESS = MEM.address;
    private static final int[] ALLOCS = new int[MAX_COUNT];
    
    private static int length;
    private static int count;
    
    public static long push(int bytes)
    {
        if (bytes <= 0) throw new IllegalArgumentException();
        if (length + bytes > MAX_LENGTH) throw new OutOfMemoryError();
        if (count == MAX_COUNT) throw new StackOverflowError();
        
        int startLength = length;
        ALLOCS[count++] = bytes;
        length += bytes;
        return ADDRESS + startLength;
    }
    
    public static long wrap(byte... array)
    {
        long address = push(array.length);
        BUF.put(array);
        return address;
    }
    
    public static long wraps(short... array)
    {
        long address = push(array.length*2);
        for (short s : array) BUF.putShort(s);
        return address;
    }
    
    public static long wrapi(int... array)
    {
        long address = push(array.length*4);
        for (int i : array) BUF.putInt(i);
        return address;
    }
    
    public static long wrapl(long... array)
    {
        long address = push(array.length*8);
        for (long l : array) BUF.putLong(l);
        return address;
    }
    
    public static long wrapf(float... array)
    {
        long address = push(array.length*4);
        for (float f : array) BUF.putFloat(f);
        return address;
    }
    
    public static long wrap(Bufferable obj)
    {
        long address = push(obj.bufferSize());
        obj.write(BUF);
        return address;
    }
    
    public static long wrapv(Bufferable... array)
    {
        int size = 0;
        for (Bufferable obj : array) size += obj.bufferSize();
        long address = push(size);
        for (Bufferable obj : array) obj.write(BUF);
        return address;
    }
    
    public static long wrap(String string)
    {
        try
        {
            byte[] bytes = string.getBytes("UTF-8");
            long address = push(bytes.length + 1);
            BUF.put(bytes);
            BUF.put((byte)0);
            return address;
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException(e);
        }
    }
    
    public static void pop()
    {
        if (count == 0) throw new IllegalStateException();
        length -= ALLOCS[--count];
        BUF.position(length);
    }
    
    public static void pop(int iterations)
    {
        for (int i=0; i<iterations; i++) pop();
    }
    
    private MemStack()
    {
    }
}
