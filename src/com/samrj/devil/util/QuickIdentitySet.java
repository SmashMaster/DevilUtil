package com.samrj.devil.util;

import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

public class QuickIdentitySet<T> extends AbstractSet<T>
{
    private static final int DEFAULT_CAPACITY = 16;
    
    private static int hash(Object o)
    {
        return System.identityHashCode(o);
    }
    
    private SparseArray<T> map;
    
    public QuickIdentitySet(int initialCapacity)
    {
        map = new SparseArray<>(initialCapacity);
    }
    
    public QuickIdentitySet(Collection<? extends T> set)
    {
        this(set.size());
        addAll(set);
    }
    
    public QuickIdentitySet(T... array)
    {
        this(Arrays.asList(array));
    }
    
    public QuickIdentitySet()
    {
        this(DEFAULT_CAPACITY);
    }
    
    @Override
    public int size()
    {
        return map.size();
    }

    @Override
    public boolean isEmpty()
    {
        return map.isEmpty();
    }

    @Override
    public boolean contains(Object o)
    {
        if (o == null) return false;
        return map.contains(hash(o));
    }

    @Override
    public Iterator<T> iterator()
    {
        return map.iterator();
    }

    @Override
    public Object[] toArray()
    {
        return map.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a)
    {
        return map.toArray(a);
    }

    @Override
    public boolean add(T e)
    {
        if (e == null) throw new NullPointerException();
        return map.put(hash(e), e);
    }
    
    @Override
    public boolean remove(Object o)
    {
        if (o == null) return false;
        return map.remove(hash(o));
    }
    
    @Override
    public void clear()
    {
        map.clear();
    }
}
