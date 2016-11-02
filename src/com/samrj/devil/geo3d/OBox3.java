package com.samrj.devil.geo3d;

import com.samrj.devil.math.Mat3;
import com.samrj.devil.math.Quat;
import com.samrj.devil.math.Transform;
import com.samrj.devil.math.Util;
import com.samrj.devil.math.Vec3;

/**
 * Oriented bounding box class.
 *
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2016 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class OBox3
{
    private static final float EPSILON = 1.0f/65536.0f;
    
    // <editor-fold defaultstate="collapsed" desc="Static accessor methods">
    /**
     * Returns whether the two given oriented boxes are touching.
     * 
     * @param b0 The first box to test.
     * @param b1 The second box to test.
     * @return Whether the two boxes are touching.
     */
    public static boolean touching(OBox3 b0, OBox3 b1)
    {
        Vec3 temp = new Vec3(), temp2 = new Vec3();
        Mat3 rotMat0 = Mat3.rotation(b0.transform.rot);
        Mat3 rotMat1 = Mat3.rotation(b1.transform.rot);
        
        Mat3 m = new Mat3();
        for (int i=0; i<3; i++) for (int j=0; j<3; j++)
            m.setEntry(i, j, temp.setAsColumn(rotMat0, i).dot(temp2.setAsColumn(rotMat1, j)));
        
        Vec3 t = Vec3.sub(b1.transform.pos, b0.transform.pos).mult(rotMat0.transpose());
        
        Mat3 absM = new Mat3();
        for (int i=0; i<3; i++) for (int j=0; j<3; j++)
            absM.setEntry(i, j, Math.abs(m.getEntry(i, j)) + EPSILON);
        
        float r0, r1;
        for (int i=0; i<3; i++)
        {
            r0 = b0.transform.sca.getComponent(i);
            r1 = b1.transform.sca.dot(temp.setAsRow(absM, i));
            if (Math.abs(t.getComponent(i)) > r0 + r1) return false;
        }
        
        for (int i=0; i<3; i++)
        {
            r0 = b0.transform.sca.dot(temp.setAsColumn(absM, i));
            r1 = b1.transform.sca.getComponent(i);
            if (Math.abs(t.dot(temp.setAsColumn(m, i))) > r0 + r1) return false;
        }
        
        r0 = b0.transform.sca.y*absM.g + b0.transform.sca.z*absM.d;
        r1 = b1.transform.sca.y*absM.c + b1.transform.sca.z*absM.b;
        if (Math.abs(t.z*m.d - t.y*m.g) > r0 + r1) return false;
        
        r0 = b0.transform.sca.y*absM.h + b0.transform.sca.z*absM.e;
        r1 = b1.transform.sca.x*absM.c + b1.transform.sca.z*absM.a;
        if (Math.abs(t.z*m.e - t.y*m.h) > r0 + r1) return false;
        
        r0 = b0.transform.sca.y*absM.i + b0.transform.sca.z*absM.f;
        r1 = b1.transform.sca.x*absM.b + b1.transform.sca.y*absM.a;
        if (Math.abs(t.z*m.f - t.y*m.i) > r0 + r1) return false;
        
        r0 = b0.transform.sca.x*absM.g + b0.transform.sca.z*absM.a;
        r1 = b1.transform.sca.y*absM.f + b1.transform.sca.z*absM.e;
        if (Math.abs(t.x*m.g - t.z*m.a) > r0 + r1) return false;
        
        r0 = b0.transform.sca.x*absM.h + b0.transform.sca.z*absM.b;
        r1 = b1.transform.sca.x*absM.f + b1.transform.sca.z*absM.d;
        if (Math.abs(t.x*m.h - t.z*m.b) > r0 + r1) return false;
        
        r0 = b0.transform.sca.x*absM.i + b0.transform.sca.z*absM.c;
        r1 = b1.transform.sca.x*absM.e + b1.transform.sca.y*absM.d;
        if (Math.abs(t.x*m.i - t.z*m.c) > r0 + r1) return false;
        
        r0 = b0.transform.sca.x*absM.d + b0.transform.sca.y*absM.a;
        r1 = b1.transform.sca.y*absM.i + b1.transform.sca.z*absM.h;
        if (Math.abs(t.y*m.a - t.x*m.d) > r0 + r1) return false;
        
        r0 = b0.transform.sca.x*absM.e + b0.transform.sca.y*absM.b;
        r1 = b1.transform.sca.x*absM.i + b1.transform.sca.z*absM.g;
        if (Math.abs(t.y*m.b - t.x*m.e) > r0 + r1) return false;
        
        r0 = b0.transform.sca.x*absM.f + b0.transform.sca.y*absM.c;
        r1 = b1.transform.sca.x*absM.h + b1.transform.sca.y*absM.g;
        if (Math.abs(t.y*m.c - t.x*m.f) > r0 + r1) return false;
        
        return true;
    }
    
    /**
     * Returns whether the given oriented box is touching the given triangle.
     * 
     * @param box An oriented box.
     * @param triangle A triangle.
     * @return Whether the box and triangle are touching.
     */
    public static boolean touching(OBox3 box, Triangle3 triangle)
    {
        Vec3 a = toLocal(box, triangle.a().p());
        Vec3 b = toLocal(box, triangle.b().p());
        Vec3 c = toLocal(box, triangle.c().p());
        Triangle3 local = Triangle3.from(a, b, c);
        return Box3.touchingUnitBox(local);
    }
    
    /**
     * Returns whether the given box contains the given vertex.
     * 
     * @param b The box to test.
     * @param v The vertex to test.
     * @return Whether the given box contains the given vertex.
     */
    public static boolean contains(OBox3 b, Vec3 v)
    {
        Vec3 lv = toLocal(b, v);
        return lv.x >= -1.0f && lv.x <= 1.0f &&
               lv.y >= -1.0f && lv.y <= 1.0f &&
               lv.z >= -1.0f && lv.z <= 1.0f;
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Static mutator methods">
    /**
     * Copies the first given box into the second.
     * 
     * @param source The box to copy from.
     * @param target The box to copy into.
     */
    public static void copy(OBox3 source, OBox3 target)
    {
        Transform.copy(source.transform, target.transform);
    }
    
    /**
     * Interpolates between the two given boxes using the given scalar, and
     * stores the result in {@code result}.
     * 
     * @param b0 The 'start' box to interpolate from.
     * @param b1 The 'end' box to interpolate to.
     * @param t The scalar interpolant, between zero and one (inclusive).
     * @param result The box in which to store the result.
     */
    public static void lerp(OBox3 b0, OBox3 b1, float t, OBox3 result)
    {
        Transform.lerp(b0.transform, b1.transform, t, result.transform);
    }
    
    /**
     * Transforms the given vector to the local space of the given box and
     * stores the result in {@code result}.
     * 
     * @param b The box whose space to transform into.
     * @param v The vector to transform.
     * @param result The vector in which to store the result.
     */
    public static void toLocal(OBox3 b, Vec3 v, Vec3 result)
    {
        Vec3.sub(v, b.transform.pos, result);
        Vec3.mult(result, Quat.invert(b.transform.rot), result);
        Vec3.div(result, b.transform.sca, result);
    }
    
    /**
     * Transforms the given vector out of the local space of the given box and
     * stores the result in {@code result}.
     * 
     * @param b The box whose space to transform out of.
     * @param v The vector to transform.
     * @param result The vector in which to store the result.
     */
    public static void toGlobal(OBox3 b, Vec3 v, Vec3 result)
    {
        Vec3.mult(v, b.transform.sca, result);
        Vec3.mult(result, b.transform.rot, result);
        Vec3.add(result, b.transform.pos, result);
    }
    
    /**
     * Clamps the given global vector to the boundary of the given box, if it
     * lies outside that boundary.
     * 
     * @param b The box to clamp to.
     * @param v The vector to clamp.
     * @param result The vector in which to store the result.
     */
    public static void clamp(OBox3 b, Vec3 v, Vec3 result)
    {
        toLocal(b, v, result);
        float chebyLen = Util.max(Math.abs(result.x),
                         Util.max(Math.abs(result.y),
                                  Math.abs(result.z)));
        if (chebyLen > 1.0f) Vec3.div(result, chebyLen, result);
        toGlobal(b, result, result);
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Static factory methods">
    /**
     * Interpolates between the two given boxes using the given scalar, and
     * returns the result as a new box.
     * 
     * @param b0 The 'start' box to interpolate from.
     * @param b1 The 'end' box to interpolate to.
     * @param t The scalar interpolant, between zero and one (inclusive).
     * @return A new box containing the result.
     */
    public static OBox3 lerp(OBox3 b0, OBox3 b1, float t)
    {
        OBox3 result = new OBox3();
        lerp(b0, b1, t, result);
        return result;
    }
    
    public static Vec3 toLocal(OBox3 b, Vec3 v)
    {
        Vec3 result = new Vec3();
        toLocal(b, v, result);
        return result;
    }
    
    public static Vec3 toGlobal(OBox3 b, Vec3 v)
    {
        Vec3 result = new Vec3();
        toGlobal(b, v, result);
        return result;
    }
    
    public static Vec3 clamp(OBox3 b, Vec3 v)
    {
        Vec3 result = new Vec3();
        clamp(b, v, result);
        return result;
    }
    // </editor-fold>
    
    public final Transform transform = new Transform();
    
    public OBox3()
    {
    }
    
    public OBox3(Transform transform)
    {
        Transform.copy(transform, this.transform);
    }
    
    public OBox3(OBox3 b)
    {
        Transform.copy(b.transform, transform);
    }
    
    public OBox3(Box3 b)
    {
        transform.pos.set(b.max).add(b.min).mult(0.5f);
        transform.sca.set(b.max).sub(b.min).mult(0.5f);
        transform.rot.setIdentity();
    }
    
    // <editor-fold defaultstate="collapsed" desc="Instance accessor methods">
    /**
     * Returns whether this is touching the given box.
     * 
     * @param b The box to test against.
     * @return Whether this is touching the given box.
     */
    public boolean touching(OBox3 b)
    {
        return touching(this, b);
    }
    
    public boolean contains(Vec3 v)
    {
        return contains(this, v);
    }
    
    public Vec3 toLocal(Vec3 v)
    {
        return toLocal(this, v);
    }
    
    public Vec3 toGlobal(Vec3 v)
    {
        return toGlobal(this, v);
    }
    
    public Vec3 clamp(Vec3 v)
    {
        return clamp(this, v);
    }
    // </editor-fold>
    
    /**
     * Sets this to the given box.
     * 
     * @param b
     * @return This box.
     */
    public OBox3 set(OBox3 b)
    {
        copy(b, this);
        return this;
    }
    
    /**
     * Interpolates this towards the given box, using the given scalar.
     * 
     * @param b The box to interpolate towards.
     * @param t The scalar interpolant, between zero and one (inclusive).
     * @return This box.
     */
    public OBox3 lerp(OBox3 b, float t)
    {
        lerp(this, b, t, this);
        return this;
    }
}
