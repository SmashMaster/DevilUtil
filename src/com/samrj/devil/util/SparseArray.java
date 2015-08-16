package com.samrj.devil.util;

import com.samrj.devil.math.Util;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A map between integers and objects based on a hash table with linked list
 * separate chaining. All integers are allowed as keys. Null is the default
 * value for all keys.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class SparseArray<T> implements Iterable<T>
{
    private static final int triNumbers[] = {1, 3, 6, 10, 15, 21, 28, 36,
                                             45, 55, 66, 78, 91, 105, 120, 136};
    
    /**
     * This sentinel object indicates that there may be a key collision at the
     * given index.
     */
    private static final Object SENTINEL = new Object();
    
    private static int triNumber(int n)
    {
        if (n < triNumbers.length) return triNumbers[n];
        n++;
        return n*(n+1) >>> 2;
    }
    
    private final float loadFactor;
    private int[] keys;
    private Object[] entries;
    private int mask;
    private int size = 0;
    
    public SparseArray(int capacity, float loadFactor)
    {
        if (capacity <= 0 || loadFactor <= 0f || loadFactor >= 1f)
            throw new IllegalArgumentException();
        if (!Util.isPower2(capacity)) capacity = Util.nextPower2(capacity);
        entries = new Object[capacity];
        keys = new int[capacity];
        mask = capacity - 1;
        this.loadFactor = loadFactor;
    }
    
    public SparseArray(int capacity)
    {
        this(capacity, 0.75f);
    }
    
    public SparseArray()
    {
        this(16);
    }
    
    public int size()
    {
        return size;
    }
    
    public boolean isEmpty()
    {
        return size == 0;
    }
    
    public int capacity()
    {
        return entries.length;
    }
    
    public float load()
    {
        return (float)size/entries.length;
    }
    
    private boolean isEmpty(int i)
    {
        Object entry = entries[i];
        return entry == null || entry == SENTINEL;
    }
    
    private int search(int key)
    {
        int i = key;
        int numSearched = 0;
        while (numSearched < entries.length)
        {
            i &= mask;
            Object entry = entries[i];
            if (entry == null || keys[i] == key) return i;
            i += triNumber(numSearched++);
        }
        throw new RuntimeException("Sparse array full?");
    }
    
    private void grow()
    {
        Object[] oldEntries = entries;
        int[] oldKeys = keys;
        entries = new Object[oldEntries.length << 1];
        keys = new int[entries.length];
        mask = entries.length - 1;
        
        for (int i=0; i<oldEntries.length; i++)
        {
            Object entry = oldEntries[i];
            if (entry != null && entry != SENTINEL)
            {
                int key = oldKeys[i];
                int index = search(key);
                keys[index] = key;
                entries[index] = entry;
            }
        }
    }
    
    public boolean contains(int key)
    {
        int i = search(key);
        return keys[i] == key && !isEmpty(i);
    }
    
    public T get(int key)
    {
        int i = search(key);
        if (keys[i] != key || isEmpty(i)) return null;
        return (T)entries[i];
    }
    
    public boolean put(int key, T value)
    {
        if (value == null) throw new NullPointerException();
        
        int i = search(key);
        if (isEmpty(i)) size++;
        keys[i] = key;
        Object oldEntry = entries[i];
        entries[i] = value;
        
        if (load() >= loadFactor) grow();
        
        return oldEntry != value;
    }
    
    public boolean remove(int key)
    {
        int i = search(key);
        boolean out;
        if (isEmpty(i)) out = false;
        else
        {
            size--;
            out = true;
        }
        entries[i] = SENTINEL;
        return out;
    }
    
    public void clear()
    {
        Arrays.fill(entries, null);
        size = 0;
    }
    
//    public void debugPrint(PrintStream stream)
//    {
//        Object firstEntry = entries[0];
//        if (firstEntry == SENTINEL) stream.print("[sentinel");
//        else stream.print("[" + entries[0]);
//        
//        for (int i=1; i<entries.length; i++)
//        {
//            Object entry = entries[i];
//            if (entry == SENTINEL) stream.print(", sentinel");
//            else stream.print(", " + entry);
//        }
//        stream.println("]");
//    }
    
    public Object[] toArray()
    {
        Object[] out = new Object[size];
        int i=0;
        for (Object o : entries) if (o != null && o != SENTINEL) out[i++] = o;
        return out;
    }
    
    public <T> T[] toArray(T[] a)
    {
        Object[] out;
        if (a.length >= size) out = a;
        else out = new Object[size];
        
        int i=0;
        for (Object o : entries) if (o != null && o != SENTINEL) out[i++] = o;
        return (T[])out;
    }

    @Override
    public Iterator<T> iterator()
    {
        return new SparseArrayIterator();
    }
    
    private class SparseArrayIterator implements Iterator<T>
    {
        private int i;
        
        private SparseArrayIterator()
        {
            findNext();
        }
        
        private void findNext()
        {
            while (true)
            {
                if (i >= entries.length) break;
                Object entry = entries[i];
                if (entry != null && entry != SENTINEL) break;
                i++;
            }
        }

        @Override
        public boolean hasNext()
        {
            return i < entries.length;
        }

        @Override
        public T next()
        {
            if (!hasNext()) throw new NoSuchElementException();
            T entry = (T)entries[i++];
            findNext();
            return entry;
        }
    }
}
