package com.samrj.devil.io;

import java.io.*;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import org.lwjgl.system.Pointer;

/**
 * Utility methods for data munging.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2019 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class IOUtil
{
    public static byte[] hexToBytes(String s)
    {
        int len = s.length();
        byte[] data = new byte[len/2];
        for (int i = 0; i < len; i += 2)
            data[i/2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) +
                                 Character.digit(s.charAt(i + 1), 16));
        return data;
    }
    
    public static String readPaddedUTF(DataInputStream in) throws IOException
    {
        if (!in.markSupported()) throw new IOException("Cannot read padded UTF-8 with this stream.");
        in.mark(8);
        int utflen = in.readUnsignedShort() + 2;
        in.reset();
        String out = in.readUTF();
        int padding = (4 - (utflen % 4)) % 4;
        if (in.skip(padding) != padding) throw new IOException("Cannot skip bytes with this stream.");
        return out;
    }
    
    public static <T> T[] arrayFromStream(DataInputStream in, Class<T> type, StreamConstructor<T> constructor) throws IOException
    {
        T[] out = (T[])Array.newInstance(type, in.readInt());
        for (int i=0; i<out.length; i++) out[i] = constructor.construct(in);
        return out;
    }
    
    public static <T> List<T> listFromStream(DataInputStream in, StreamConstructor<T> constructor) throws IOException
    {
        int size = in.readInt();
        if (size == 0) return Collections.emptyList();
        
        List<T> out = new ArrayList<>(size);
        for (int i=0; i<size; i++) out.add(constructor.construct(in));
        return Collections.unmodifiableList(out);
    }
    
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
    
    public static InputStream stringStream(String string)
    {
        return new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8));
    }
    
    public static <T, R> R[] mapArray(T[] array, Class<R> type, Function<T, R> func)
    {
        R[] out = (R[])Array.newInstance(type, array.length);
        for (int i=0; i<out.length; i++) out[i] = func.apply(array[i]);
        return out;
    }
    
    public static <T, R> List<R> mapList(List<T> list, Function<T, R> func)
    {
        List<R> out = new ArrayList<>(list.size());
        for (T value : list) out.add(func.apply(value));
        return out;
    }
    
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
    
    public static <T> void filter(Collection<T> collection, Predicate<T> predicate)
    {
        Iterator<T> it = collection.iterator();
        while (it.hasNext())
        {
            T value = it.next();
            if (!predicate.test(value)) it.remove();
        }
    }
    
    public static String unbufferUTF(ByteBuffer buffer)
    {
        try
        {
            int start = buffer.position();
            while (buffer.get() != 0);
            int length = buffer.position() - start - 1;
            byte[] bytes = new byte[length];
            buffer.get(bytes, start, length);
            return new String(bytes, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException(e);
        }
    }
    
    public static void bufferUTF(String string, ByteBuffer buffer)
    {
        try
        {
            byte[] bytes = string.getBytes("UTF-8");
            buffer.put(bytes);
            buffer.put((byte)0);
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException(e);
        }
    }
    
    public static int getUTFSize(String string)
    {
        int count = 0;
        for (int i = 0, len = string.length(); i < len; i++)
        {
            char c = string.charAt(i);
            if (c <= 0x7F) count++;
            else if (c <= 0x7FF) count += 2;
            else if (Character.isHighSurrogate(c))
            {
                count += 4;
                i++;
            } else count += 3;
        }
        return count;
    }
    
    public static long unbufferPointer(ByteBuffer buffer)
    {
        switch (Pointer.POINTER_SIZE)
        {
            case 4: return buffer.getInt();
            case 8: return buffer.getLong();
            default: throw new Error("Unknown pointer size on this platform.");
        }
    }
    
    public static void bufferPointer(long address, ByteBuffer buffer)
    {
        switch (Pointer.POINTER_SIZE)
        {
            case 4: buffer.putInt((int)address); return;
            case 8: buffer.putLong(address); return;
            default: throw new Error("Unknown pointer size on this platform.");
        }
    }
    
    private IOUtil()
    {
    }
    
    public static final class ArrayIterator<T> implements Iterator<T>
    {
        private final T[] array;
        private int index;
        
        public ArrayIterator(T[] array)
        {
            if (array == null) throw new NullPointerException();
            this.array = array;
        }

        @Override
        public boolean hasNext()
        {
            return index < array.length;
        }

        @Override
        public T next()
        {
            if (hasNext()) return array[index++];
            else throw new NoSuchElementException();
        }
    }
}
