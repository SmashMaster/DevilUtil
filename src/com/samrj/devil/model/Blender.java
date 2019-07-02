package com.samrj.devil.model;

import com.samrj.devil.math.Mat3;
import com.samrj.devil.math.Mat4;
import com.samrj.devil.math.Vec3;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.blender.dna.ID;
import org.blender.dna.ListBase;
import org.cakelab.blender.nio.CArrayFacade;
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
    
    static Vec3 vec3(CArrayFacade<Float> facade) throws IOException
    {
        float[] v = facade.toFloatArray(3);
        return new Vec3(v[1], v[2], v[0]);
    }
    
    static Mat3 mat3(CArrayFacade<CArrayFacade<Float>> facade) throws IOException
    {
        float[][] m = new float[3][];
        for (int i=0; i<3; i++) m[i] = facade.get(i).toFloatArray(3);
        
        return new Mat3(m[1][1], m[1][2], m[1][0],
                        m[2][1], m[2][2], m[2][0],
                        m[0][1], m[0][2], m[0][0]);
    }
    
    static Mat4 mat4(CArrayFacade<CArrayFacade<Float>> facade) throws IOException
    {
        float[][] m = new float[4][];
        CArrayFacade<Float>[] arf = facade.toArray(4);
        for (int i=0; i<4; i++) m[i] = arf[i].toFloatArray(4);
        
        return new Mat4(m[1][1], m[1][2], m[1][0], m[1][3],
                        m[2][1], m[2][2], m[2][0], m[2][3],
                        m[0][1], m[0][2], m[0][0], m[0][3],
                        m[3][1], m[3][2], m[3][0], m[3][3]);
    }
    
    private Blender()
    {
    }
}
