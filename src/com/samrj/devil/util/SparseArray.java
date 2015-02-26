package com.samrj.devil.util;

import java.util.Arrays;

/**
 * A map between integers and objects based on a hash table with linked list
 * separate chaining. All integers are allowed as keys. Null is not allowed as
 * a value.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class SparseArray<T>
{
    private Entry[] entries;
    private int size = 0;
    private float loadFactor;
    
    public SparseArray(int capacity, float loadFactor)
    {
        if (capacity <= 0 || loadFactor <= 0f || loadFactor >= 1f)
            throw new IllegalArgumentException();
        entries = new Entry[capacity];
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
    
    public int capacity()
    {
        return entries.length;
    }
    
    public float load()
    {
        return (float)size/entries.length;
    }
    
    private int index(int key)
    {
        int t = key % entries.length;
        return t<0 ? t+entries.length : t;
    }
    
    public void put(int key, T value)
    {
        if (value == null) throw new NullPointerException();
        
        int index = index(key);
        if (entries[index] == null)
        {
            entries[index] = new Entry(key, value);
            size++;
        }
        else if (entries[index].put(key, value)) size++;
        
        if (load() >= loadFactor) grow();
    }
    
    private void put(Entry entry)
    {
        int index = index(entry.key);
        if (entries[index]  == null) entries[index] = entry;
        else entries[index].put(entry);
    }
    
    public void remove(int key)
    {
        int index = index(key);
        Entry entry = entries[index];
        if (entry != null)
        {
            if (entry.key == key)
            {
                entries[index] = entry.next;
                size--;
            }
            else if (entry.remove(key)) size--;
        }
    }
    
    public void clear()
    {
        Arrays.fill(entries, null);
        size = 0;
    }
    
    public T get(int key)
    {
        Entry entry = entries[index(key)];
        if (entry == null) return null;
        return (T)entry.get(key);
    }
    
    private void grow()
    {
        Entry[] oldEntries = entries;
        entries = new Entry[oldEntries.length << 1];
        
        for (Entry entry : oldEntries) while (entry != null)
        {
            entry.prepareForGrow();
            put(entry);
            entry = entry.oldNext;
        }
    }
    
    private static class Entry
    {
        private int key;
        private Object value;
        private Entry oldNext;
        private Entry next;
        
        private Entry(int key, Object value)
        {
            this.key = key;
            this.value = value;
        }
        
        private boolean put(int key, Object value)
        {
            if (this.key == key)
            {
                this.value = value;
                return false;
            }
            else if (next == null)
            {
                next = new Entry(key, value);
                return true;
            }
            else return next.put(key, value);
        }
        
        private void put(Entry entry)
        {
            if (next == null) next = entry;
            else next.put(entry);
        }
        
        private Object get(int key)
        {
            if (this.key == key) return value;
            else if (next == null) return null;
            else return next.get(key);
        }
        
        private boolean remove(int key)
        {
            if (next == null) return false;
            if (next.key == key)
            {
                next = next.next;
                return true;
            }
            return next.remove(key);
        }
        
        private void prepareForGrow()
        {
            oldNext = next;
            next = null;
            if (oldNext != null) oldNext.prepareForGrow();
        }
    }
}
