package com.samrj.devil.buffer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;

/**
 * Static factory class for nio buffers. Also has some small, public buffers.
 * 
 * The public buffers are not thread-safe at all, so watch out.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 */
public class BufferUtil
{
    public static final ByteBuffer pubBufA = createByteBuffer(128);
    public static final ByteBuffer pubBufB = createByteBuffer(128);
    public static final ByteBuffer pubBufC = createByteBuffer(128);
    public static final ByteBuffer pubBufD = createByteBuffer(128);
    
    /**
     * Clears each of the public utility buffers.
     */
    public static void clearPublicBuffers()
    {
        pubBufA.clear();
        pubBufB.clear();
        pubBufC.clear();
        pubBufD.clear();
    }
    
    /**
     * Creates a byte buffer whose capacity (in bytes) is the given value.
     */
    public static ByteBuffer createByteBuffer(int size)
    {
        return ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder());
    }
    
    /**
     * Creates a short buffer whose capacity (in shorts) is the given value.
     */
    public static ShortBuffer createShortBuffer(int size)
    {
        return createByteBuffer(size << 1).asShortBuffer();
    }
    
    /**
     * Creates an int buffer whose capacity (in ints) is the given value.
     */
    public static IntBuffer createIntBuffer(int size)
    {
        return createByteBuffer(size << 2).asIntBuffer();
    }
    
    /**
     * Creates a long buffer whose capacity (in longs) is the given value.
     */
    public static LongBuffer createLongBuffer(int size)
    {
        return createByteBuffer(size << 3).asLongBuffer();
    }
    
    /**
     * Creates a float buffer whose capacity (in floats) is the given value.
     */
    public static FloatBuffer createFloatBuffer(int size)
    {
        return createByteBuffer(size << 2).asFloatBuffer();
    }
    
    /**
     * Creates a double buffer whose capacity (in doubles) is the given value.
     */
    public static DoubleBuffer createDoubleBuffer(int size)
    {
        return createByteBuffer(size << 3).asDoubleBuffer();
    }
    
    /**
     * Creates a char buffer whose capacity (in chars) is the given value.
     */
    public static CharBuffer createCharBuffer(int size)
    {
        return createByteBuffer(size << 1).asCharBuffer();
    }
    
    /**
     * Creates a byte buffer whose capacity is the length of the given array,
     * fills that buffer with the contents of the array, and then rewinds the
     * buffer.
     */
    public static ByteBuffer wrapBytes(byte... array)
    {
        ByteBuffer buffer = createByteBuffer(array.length);
        //Actually faster than bulk put due to bounds checking.
        for (byte b : array) buffer.put(b);
        buffer.rewind();
        return buffer;
    }
    
    /**
     * Creates a short buffer whose capacity is the length of the given array,
     * fills that buffer with the contents of the array, and then rewinds the
     * buffer.
     */
    public static ShortBuffer wrapShorts(short... array)
    {
        ShortBuffer buffer = createShortBuffer(array.length);
        for (short s : array) buffer.put(s);
        buffer.rewind();
        return buffer;
    }
    
    /**
     * Creates an int buffer whose capacity is the length of the given array,
     * fills that buffer with the contents of the array, and then rewinds the
     * buffer.
     */
    public static IntBuffer wrapInts(int... array)
    {
        IntBuffer buffer = createIntBuffer(array.length);
        for (int i : array) buffer.put(i);
        buffer.rewind();
        return buffer;
    }
    
    /**
     * Creates a long buffer whose capacity is the length of the given array,
     * fills that buffer with the contents of the array, and then rewinds the
     * buffer.
     */
    public static LongBuffer wrapLongs(long... array)
    {
        LongBuffer buffer = createLongBuffer(array.length);
        for (long l : array) buffer.put(l);
        buffer.rewind();
        return buffer;
    }
    
    /**
     * Creates a float buffer whose capacity is the length of the given array,
     * fills that buffer with the contents of the array, and then rewinds the
     * buffer.
     */
    public static FloatBuffer wrapFloats(float... array)
    {
        FloatBuffer buffer = createFloatBuffer(array.length);
        for (float f : array) buffer.put(f);
        buffer.rewind();
        return buffer;
    }
    
    /**
     * Creates a double buffer whose capacity is the length of the given array,
     * fills that buffer with the contents of the array, and then rewinds the
     * buffer.
     */
    public static DoubleBuffer wrapDoubles(double... array)
    {
        DoubleBuffer buffer = createDoubleBuffer(array.length);
        for (double d : array) buffer.put(d);
        buffer.rewind();
        return buffer;
    }
    
    /**
     * Creates a char buffer whose capacity is the length of the given array,
     * fills that buffer with the contents of the array, and then rewinds the
     * buffer.
     */
    public static CharBuffer wrapChars(char... array)
    {
        CharBuffer buffer = createCharBuffer(array.length);
        for (char c : array) buffer.put(c);
        buffer.rewind();
        return buffer;
    }
    
    /**
     * Creates a char buffer whose capacity is the length of the given string,
     * fills that buffer with the contents of the string, and then rewinds the
     * buffer.
     */
    public static CharBuffer wrapString(CharSequence string)
    {
        final int length = string.length();
        CharBuffer buffer = createCharBuffer(string.length());
        for (int i=0; i<length; i++) buffer.put(string.charAt(i));
        buffer.rewind();
        return buffer;
    }
    
    private BufferUtil()
    {
    }
}
