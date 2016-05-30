package com.samrj.devil.util;

import java.util.*;

/**
 * Deprecated: Use Collections.newSetFromMap(new IdentityHashMap<>()) instead.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
@Deprecated
public class IdentitySet<T> extends AbstractSet<T>
{
    private static final int DEFAULT_CAPACITY = 16;
    
    private final IdentityHashMap<T, T> map;

    @Deprecated
    public IdentitySet(int expectedMaxSize)
    {
        map = new IdentityHashMap<>(expectedMaxSize);
    }
    
    @Deprecated
    public IdentitySet(Collection<? extends T> set)
    {
        this(set.size());
        addAll(set);
    }
    
    @Deprecated
    public IdentitySet(T... array)
    {
        this(Arrays.asList(array));
    }
    
    @Deprecated
    public IdentitySet()
    {
        this(DEFAULT_CAPACITY);
    }
    
    @Override public int size()
    {
        return map.size();
    }

    @Override public boolean isEmpty()
    {
        return map.isEmpty();
    }

    @Override public boolean contains(Object o)
    {
        if (o == null) return false;
        return map.containsKey(o);
    }

    @Override public Iterator<T> iterator()
    {
        return map.keySet().iterator();
    }

    @Override public Object[] toArray()
    {
        return map.keySet().toArray();
    }

    @Override public <T> T[] toArray(T[] a)
    {
        return map.keySet().toArray(a);
    }

    @Override public boolean add(T e)
    {
        if (e == null) throw new NullPointerException();
        return map.put(e, e) != e;
    }
    
    @Override public boolean remove(Object o)
    {
        if (o == null) return false;
        return map.remove(o) != null;
    }
    
    @Override public void clear()
    {
        map.clear();
    }
    
    @Override
    public int hashCode()
    {
        return map.hashCode();
    }
}
