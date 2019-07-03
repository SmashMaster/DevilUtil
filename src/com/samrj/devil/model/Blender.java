package com.samrj.devil.model;

import com.samrj.devil.math.Mat3;
import com.samrj.devil.math.Mat4;
import com.samrj.devil.math.Quat;
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
    static <T extends CFacade> List<T> list(T first) throws IOException
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
    
    static <T extends CFacade> List<T> list(ListBase base, Class<T> cls) throws IOException
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
    
    static String string(CPointer<Byte> pointer) throws IOException
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
    
    static String string(CPointer<Byte> pointer, int bytes) throws IOException
    {
        StringBuilder builder = new StringBuilder(bytes);
        for (byte b : pointer.toByteArray(bytes)) builder.append((char)b);
        return builder.toString();
    }
    
    static Vec3 vec3(CArrayFacade<Float> facade) throws IOException
    {
        float[] v = facade.toFloatArray(3);
        return new Vec3(v[1], v[2], v[0]);
    }
    
    static Quat quat(CArrayFacade<Float> facade) throws IOException
    {
        float[] q = facade.toFloatArray(4);
        return new Quat(q[0], q[2], q[3], q[1]);
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
        for (int i=0; i<4; i++) m[i] = facade.get(i).toFloatArray(4);
        
        return new Mat4(m[1][1], m[1][2], m[1][0], m[1][3],
                        m[2][1], m[2][2], m[2][0], m[2][3],
                        m[0][1], m[0][2], m[0][0], m[0][3],
                        m[3][1], m[3][2], m[3][0], m[3][3]);
    }
    
    /**
     * This lets us project points onto a plane defined by a normal, and ignore the z coordinate.
     */
    static void orthogBasis(Vec3 n, Mat3 result)
    {
        n = Vec3.negate(n);
        float len = n.length();

        if (len != 0)
        {
            //Todo: Optimize this
            Vec3 b = Vec3.cross(n, new Vec3(1.0f, 0.0f, 0.0f));
            if (b.isZero(0.01f)) Vec3.cross(n, new Vec3(0.0f, 1.0f, 0.0f), b);
            b.normalize();
            
            Vec3 t = Vec3.cross(n, b).normalize();
            
            result.set(b.x, b.y, b.z,
                       t.x, t.y, t.z,
                       0.0f, 0.0f, 0.0f);
        }
        else result.setIdentity(); //Cannot create basis from zero vector
    }
    
    static Mat3 orthogBasis(Vec3 n)
    {
        Mat3 result = new Mat3();
        orthogBasis(n, result);
        return result;
    }
    
    private Blender()
    {
    }
}
