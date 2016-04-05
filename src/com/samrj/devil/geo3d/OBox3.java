package com.samrj.devil.geo3d;

import com.samrj.devil.math.Mat3;
import com.samrj.devil.math.Quat;
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
    
    /**
     * Copies the first given box into the second.
     * 
     * @param source The box to copy from.
     * @param target The box to copy into.
     */
    public static void copy(OBox3 source, OBox3 target)
    {
        Vec3.copy(source.pos, target.pos);
        Quat.copy(source.rot, target.rot);
        Vec3.copy(source.sca, target.sca);
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
        Vec3.lerp(b0.pos, b1.pos, t, result.pos);
        Quat.slerp(b0.rot, b1.rot, t, result.rot);
        Vec3.lerp(b0.sca, b1.sca, t, result.sca);
    }
    
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
        Mat3 rotMat0 = Mat3.rotation(b0.rot);
        Mat3 rotMat1 = Mat3.rotation(b1.rot);
        
        Mat3 m = new Mat3();
        for (int i=0; i<3; i++) for (int j=0; j<3; j++)
            m.setEntry(i, j, temp.setAsColumn(rotMat0, i).dot(temp2.setAsColumn(rotMat1, j)));
        
        Vec3 t = Vec3.sub(b1.pos, b0.pos).mult(rotMat0.transpose());
        
        Mat3 absM = new Mat3();
        for (int i=0; i<3; i++) for (int j=0; j<3; j++)
            absM.setEntry(i, j, Math.abs(m.getEntry(i, j)) + EPSILON);
        
        float r0, r1;
        for (int i=0; i<3; i++)
        {
            r0 = b0.sca.getComponent(i);
            r1 = b1.sca.dot(temp.setAsRow(absM, i));
            if (Math.abs(t.getComponent(i)) > r0 + r1) return false;
        }
        
        for (int i=0; i<3; i++)
        {
            r0 = b0.sca.dot(temp.setAsColumn(absM, i));
            r1 = b1.sca.getComponent(i);
            if (Math.abs(t.dot(temp.setAsColumn(m, i))) > r0 + r1) return false;
        }
        
        r0 = b0.sca.y*absM.g + b0.sca.z*absM.d;
        r1 = b1.sca.y*absM.c + b1.sca.z*absM.b;
        if (Math.abs(t.z*m.d - t.y*m.g) > r0 + r1) return false;
        
        r0 = b0.sca.y*absM.h + b0.sca.z*absM.e;
        r1 = b1.sca.x*absM.c + b1.sca.z*absM.a;
        if (Math.abs(t.z*m.e - t.y*m.h) > r0 + r1) return false;
        
        r0 = b0.sca.y*absM.i + b0.sca.z*absM.f;
        r1 = b1.sca.x*absM.b + b1.sca.y*absM.a;
        if (Math.abs(t.z*m.f - t.y*m.i) > r0 + r1) return false;
        
        r0 = b0.sca.x*absM.g + b0.sca.z*absM.a;
        r1 = b1.sca.y*absM.f + b1.sca.z*absM.e;
        if (Math.abs(t.x*m.g - t.z*m.a) > r0 + r1) return false;
        
        r0 = b0.sca.x*absM.h + b0.sca.z*absM.b;
        r1 = b1.sca.x*absM.f + b1.sca.z*absM.d;
        if (Math.abs(t.x*m.h - t.z*m.b) > r0 + r1) return false;
        
        r0 = b0.sca.x*absM.i + b0.sca.z*absM.c;
        r1 = b1.sca.x*absM.e + b1.sca.y*absM.d;
        if (Math.abs(t.x*m.i - t.z*m.c) > r0 + r1) return false;
        
        r0 = b0.sca.x*absM.d + b0.sca.y*absM.a;
        r1 = b1.sca.y*absM.i + b1.sca.z*absM.h;
        if (Math.abs(t.y*m.a - t.x*m.d) > r0 + r1) return false;
        
        r0 = b0.sca.x*absM.e + b0.sca.y*absM.b;
        r1 = b1.sca.x*absM.i + b1.sca.z*absM.g;
        if (Math.abs(t.y*m.b - t.x*m.e) > r0 + r1) return false;
        
        r0 = b0.sca.x*absM.f + b0.sca.y*absM.c;
        r1 = b1.sca.x*absM.h + b1.sca.y*absM.g;
        if (Math.abs(t.y*m.c - t.x*m.f) > r0 + r1) return false;
        
        return true;
    }
    
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
    
    public final Vec3 pos = new Vec3();
    public final Quat rot = new Quat();
    public final Vec3 sca = new Vec3();
    
    public OBox3()
    {
    }
    
    public OBox3(OBox3 b)
    {
        Vec3.copy(b.pos, pos);
        Quat.copy(b.rot, rot);
        Vec3.copy(b.sca, sca);
    }
    
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
