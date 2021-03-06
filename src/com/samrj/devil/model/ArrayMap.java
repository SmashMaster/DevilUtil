package com.samrj.devil.model;

import java.util.*;
import java.util.stream.Stream;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2019 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class ArrayMap<T extends DataBlock> extends AbstractList<T>
{
    private final List<T> list = new ArrayList<>();
    private final Map<String, T> map = new HashMap<>();
    
    ArrayMap()
    {
    }
    
    void put(T data)
    {
        list.add(data);
        map.put(data.name, data);
    }
    
    public boolean contains(String name)
    {
        return map.containsKey(name);
    }

    public T get(String name)
    {
        return map.get(name);
    }
    
    public Optional<T> optional(String name)
    {
        return Optional.ofNullable(map.get(name));
    }
    
    public T require(String name)
    {
        T out = map.get(name);
        if (out == null) throw new NoSuchElementException(name);
        return out;
    }

    @Override
    public T get(int i)
    {
        return list.get(i);
    }

    @Override
    public int size()
    {
        return list.size();
    }
    
    @Override
    public Iterator<T> iterator()
    {
        return Collections.unmodifiableList(list).iterator();
    }
    
    @Override
    public Stream<T> stream()
    {
        return list.stream();
    }
    
    void destroy()
    {
        list.forEach(DataBlock::destroy);
        list.clear();
        map.clear();
    }
}
