package com.samrj.devil.io;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Utility methods for data munging.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
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
    
    public static <T> void filter(Collection<T> collection, Predicate<T> predicate)
    {
        Iterator<T> it = collection.iterator();
        while (it.hasNext())
        {
            T value = it.next();
            if (!predicate.test(value)) it.remove();
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
