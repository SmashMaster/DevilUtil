package com.samrj.devil.model;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Stream;

public final class ArrayMap<T extends DataBlock> implements Iterable<T>
{
    private final List<T> list;
    private final Map<String, T> map;

    ArrayMap(Model model, DataInputStream in, int magic, ModelConstructor<T> constructor) throws IOException
    {
        int rID = in.readInt();
        if (rID != magic) throw new IOException("Expected id " + magic + ", read " + rID);
        in.skip(4);
        
        int size = in.readInt();
        list = new ArrayList<>(size);
        map = new HashMap<>(size);
        
        for (int i=0; i<size; i++)
        {
            T data = constructor.construct(model, in);
            list.add(data);
            map.put(data.name, data);
        }
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

    public T get(int i)
    {
        return list.get(i);
    }

    public int size()
    {
        return list.size();
    }

    @Override
    public Iterator<T> iterator()
    {
        return list.iterator();
    }
    
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
