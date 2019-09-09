package com.samrj.devil.util;

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
 * @copyright 2019 Samuel Johnson
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
    
    private IOUtil()
    {
    }
}
