package com.samrj.devil.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Utility methods for data munging.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2020 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class IOUtil
{
    /**
     * Removes the entry at the given index without preserving the order of the
     * remaining entries. Very fast for array lists because subsequent elements
     * are not shifted.
     */
    public static <T> void quickRemove(List<T> list, int index)
    {
        int end = list.size() - 1;
        T last = list.remove(end);
        if (index != end) list.set(index, last);
    }
    
    /**
     * Creates a new array with the same length as the given array and type,
     * mapping each element using the given function.
     */
    public static <T, R> R[] mapArray(T[] array, Class<R> type, Function<T, R> func)
    {
        R[] out = (R[])Array.newInstance(type, array.length);
        for (int i=0; i<out.length; i++) out[i] = func.apply(array[i]);
        return out;
    }
    
    /**
     * Creates a new list by mapping every element in the given list, in order,
     * using the given function.
     */
    public static <T, R> List<R> mapList(List<T> list, Function<T, R> func)
    {
        List<R> out = new ArrayList<>(list.size());
        for (T value : list) out.add(func.apply(value));
        return out;
    }
    
    /**
     * Runs the given function on each pair of elements in the given list.
     */
    public static <T> void forEachPair(List<T> list, BiConsumer<T, T> func)
    {
        int len = list.size();
        for (int i0=0; i0<len-1; i0++)
        {
            T e0 = list.get(i0);
            for (int i1=i0+1; i1<len; i1++)
                func.accept(e0, list.get(i1));
        }
    }
    
    /**
     * Runs the given function on each pair of elements in the given array.
     */
    public static <T> void forEachPair(T[] array, BiConsumer<T, T> func)
    {
        int len = array.length;
        for (int i0=0; i0<len-1; i0++)
        {
            T e0 = array[i0];
            for (int i1=i0+1; i1<len; i1++)
                func.accept(e0, array[i1]);
        }
    }
    
    /**
     * Removes all elements from the given collection that do not fulfill the
     * given predicate.
     */
    public static <T> void filter(Collection<T> collection, Predicate<T> predicate)
    {
        Iterator<T> it = collection.iterator();
        while (it.hasNext())
        {
            T value = it.next();
            if (!predicate.test(value)) it.remove();
        }
    }
    
    /**
     * Reads a string from the given ByteBuffer, with the given character set
     * and length, in bytes.
     */
    public static String readString(ByteBuffer buffer, int length, Charset charset)
    {
        byte[] bytes = new byte[length];
        buffer.get(bytes);
        return new String(bytes, charset);
    }
    
    /**
     * Reads an ASCII string from the given ByteBuffer, with the given length.
     * @return 
     */
    public static String readString(ByteBuffer buffer, int length)
    {
        return readString(buffer, length, StandardCharsets.US_ASCII);
    }
    
    /**
     * Reads a null-terminated string from the given ByteBuffer and charset.
     */
    public static String readNullTermString(ByteBuffer buffer, Charset charset)
    {
        int start = buffer.position();
        while (true)
        {
            char c = (char)buffer.get();
            if (c == '\0') break;
        }
        int end = buffer.position() - 1;
        
        buffer.position(start);
        String result = readString(buffer, end - start, charset);
        buffer.get(); //Skip null terminator
        
        return result;
    }
    
    /**
     * Reads a null-terminated ASCII string from the given buffer.
     */
    public static String readNullTermString(ByteBuffer buffer)
    {
        return readNullTermString(buffer, StandardCharsets.US_ASCII);
    }
    
    public static byte[] readAllBytes(InputStream in) throws IOException
    {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int count;
        byte[] array = new byte[16384];
        while ((count = in.read(array, 0, array.length)) != -1) buffer.write(array, 0, count);

        return buffer.toByteArray();
    }

    private static final int VLQ_CONTINUE = 0b10000000;
    private static final int VLQ_SIGN     = 0b01000000;
    private static final int VLQ_MASK1    = 0b00111111;
    private static final int VLQ_MASK_N   = 0b01111111;
    private static final byte[] VLQ_MIN_VALUE = {(byte)VLQ_CONTINUE, (byte)VLQ_CONTINUE, (byte)VLQ_CONTINUE, (byte)VLQ_CONTINUE, (byte)0b00010000};

    /**
     * Reads a signed variable-length quantity assuming it was encoded by writeVLQ().
     */
    public static int readVLQ(ByteBuffer buffer)
    {
        byte firstByte = buffer.get();
        boolean sign = (firstByte & VLQ_SIGN) != 0;
        int abs = firstByte & VLQ_MASK1;
        boolean cont = (firstByte & VLQ_CONTINUE) != 0;

        if (cont)
        {
            byte nextByte = buffer.get();
            abs |= (nextByte & VLQ_MASK_N) << 6;
            cont = (nextByte & VLQ_CONTINUE) != 0;

            if (cont)
            {
                nextByte = buffer.get();
                abs |= (nextByte & VLQ_MASK_N) << 13;
                cont = (nextByte & VLQ_CONTINUE) != 0;

                if (cont)
                {
                    nextByte = buffer.get();
                    abs |= (nextByte & VLQ_MASK_N) << 20;
                    cont = (nextByte & VLQ_CONTINUE) != 0;

                    if (cont) abs |= (buffer.get() & VLQ_MASK_N) << 27;
                }
            }
        }

        return sign ? abs : -abs;
    }

    /**
     * Writes a signed variable-length quantity the given buffer such that it could be decoded by readVLQ().
     */
    public static void writeVLQ(ByteBuffer buffer, int value)
    {
        if (value == Integer.MIN_VALUE) //There is no absolute value of Integer.MIN_VALUE without long. Need a special case.
        {
            buffer.put(VLQ_MIN_VALUE);
            return;
        }

        boolean sign = value >= 0;
        int abs = sign ? value : -value;
        boolean cont = abs >= 64;

        int firstByte = abs & VLQ_MASK1;
        if (sign) firstByte |= VLQ_SIGN;
        if (cont) firstByte |= VLQ_CONTINUE;
        buffer.put((byte)firstByte);

        if (cont)
        {
            cont = abs >= 8192;

            int nextByte = (abs >>> 6) & VLQ_MASK_N;
            if (cont) nextByte |= VLQ_CONTINUE;
            buffer.put((byte)nextByte);

            if (cont)
            {
                cont = abs >= 1048576;

                nextByte = (abs >>> 13) & VLQ_MASK_N;
                if (cont) nextByte |= VLQ_CONTINUE;
                buffer.put((byte)nextByte);

                if (cont)
                {
                    cont = abs >= 134217728;

                    nextByte = (abs >>> 20) & VLQ_MASK_N;
                    if (cont) nextByte |= VLQ_CONTINUE;
                    buffer.put((byte)nextByte);

                    if (cont) buffer.put((byte)((abs >>> 27) & VLQ_MASK_N));
                }
            }
        }
    }

    /**
     * Reads a UTF-8 string from the given buffer, assuming it was encoded by writeUTF8().
     */
    public static String readUTF8(ByteBuffer buffer)
    {
        int length = readVLQ(buffer);
        if (length < 0) return null;

        byte[] bytes = new byte[length];
        buffer.get(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    /**
     * Writes a UTF-8 string from the given buffer such that it could be decoded by readUTF8().
     */
    public static void writeUTF8(ByteBuffer buffer, String string)
    {
        if (string == null)
        {
            writeVLQ(buffer, -1);
            return;
        }

        byte[] bytes = string.getBytes(StandardCharsets.UTF_8);
        writeVLQ(buffer, bytes.length);
        buffer.put(bytes);
    }

    private IOUtil()
    {
    }
}
