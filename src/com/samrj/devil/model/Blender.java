package com.samrj.devil.model;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.blender.dna.ID;
import org.blender.dna.ListBase;
import org.cakelab.blender.nio.CFacade;
import org.cakelab.blender.nio.CPointer;

class Blender
{
    static <T extends CFacade> List<T> blendList(T first) throws IOException
    {
        ArrayList<T> result = new ArrayList<>();
        if (first == null) return new ArrayList<>();
        
        Class<T> cls = (Class<T>)first.getClass();
        T next = first;

        try
        {
            Method idMethod = cls.getMethod("getId");
            
            while (next != null)
            {
                result.add(next);
                ID id = (ID)idMethod.invoke(next);
                next = id.getNext().cast(cls).get();
            }
        }
        catch (ReflectiveOperationException e)
        {
            throw new RuntimeException(e);
        }
        
        return result;
    }
    
    static <T extends CFacade> List<T> blendList(ListBase base, Class<T> cls) throws IOException
    {
        ArrayList<T> result = new ArrayList<>();
        T next = base.getFirst().cast(cls).get();
        if (next == null) return result;
        
        try
        {
            Method nextMethod = cls.getMethod("getNext");
        
            while (next != null)
            {
                CPointer<?> nextPtr = (CPointer)nextMethod.invoke(next);
                result.add(next);
                next = nextPtr.cast(cls).get();
            }
        }
        catch (ReflectiveOperationException e)
        {
            throw new RuntimeException(e);
        }
        
        return result;
    }
    
    static String blendString(CPointer<Byte> pointer) throws IOException
    {
        StringBuilder builder = new StringBuilder();
        while (true)
        {
            char c = (char)pointer.get().byteValue();
            if (c == '\0') break;
            builder.append(c);
            pointer = pointer.plus(1);
        }
        return builder.toString();
    }
    
    private Blender()
    {
    }
}
