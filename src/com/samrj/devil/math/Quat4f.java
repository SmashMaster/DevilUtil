package com.samrj.devil.math;

import com.samrj.devil.math.Util.Axis;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class Quat4f
{
    /* 
     * This quaternion class follows the same conventions as the Matrix classes.
     * This includes operand order. If you initialize a Matrix3f m and a Quat4f
     * q and perform equivalent operations on both, in the same order, then
     * q.toMatrix3f() should approximate m within a few floating point epsila.
     * 
     * Sources of information, code, or algorithms:
     * [0] http://www.cprogramming.com/tutorial/3d/quaternions.html
     * [1] OpenGL Mathematics (glm.g-truc.net)
     * [2] https://svn.blender.org/svnroot/bf-blender/trunk/blender/source/blender/blenlib/intern/math_rotation.c
     */
    
    private static final float SQRT_2 = Util.sqrt(2.0f);
    
    // <editor-fold defaultstate="collapsed" desc="Factory Methods">
    public static Quat4f identity()
    {
        return new Quat4f();
    }
    
    public static Quat4f axisAngle(Vector3f axis, float angle) //Apapted from [0]
    {
        final float a = angle*.5f;
        final float sin = Util.sin(a);
        final float cos = Util.cos(a);
        
        axis = axis.copy().setLength(sin);
        
        return new Quat4f(cos, axis.x, axis.y, axis.z);
    }
    
    public static Quat4f axisAngle(Vector3f axisAngle)
    {
        float angle = axisAngle.length();
        if (angle == 0f) return new Quat4f();
        
        Vector3f axis = axisAngle.cdiv(angle);
        return axisAngle(axis, angle);
    }
    
    public static Quat4f axisAngle(Axis axis, float angle)
    {
        return axisAngle(axis.versor(), angle);
    }
    
    public static Quat4f fromDir(Vector3f v)
    {
        return Axis.X.versor().rotationTo(v);
    }
    // </editor-fold>
    
    public float w, x, y, z;
    
    // <editor-fold defaultstate="collapsed" desc="Constructors">
    public Quat4f(float w, float x, float y, float z)
    {
        set(w, x, y, z);
    }
    
    public Quat4f(Quat4f q)
    {
        set(q);
    }
    
    public Quat4f()
    {
        set(1f, 0f, 0f, 0f);
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Mutator Methods">
    public Quat4f set(float w, float x, float y, float z)
    {
        this.w = w; this.x = x; this.y = y; this.z = z;
        return this;
    }
    
    public Quat4f set(Quat4f q)
    {
        return set(q.w, q.x, q.y, q.z);
    }

    public Quat4f mult(Quat4f q) //Source: [0]
    {
        return set(w*q.w - x*q.x - y*q.y - z*q.z,
                   w*q.x + x*q.w + y*q.z - z*q.y,
                   w*q.y - x*q.z + y*q.w + z*q.x,
                   w*q.z + x*q.y - y*q.x + z*q.w);
    }
    
    public Quat4f add(Quat4f q)
    {
        return set(w+q.w, x+q.x, y+q.y, z+q.z);
    }
    
    public Quat4f sub(Quat4f q)
    {
        return set(w-q.w, x-q.x, y-q.y, z-q.z);
    }
    
    public Quat4f mult(float s)
    {
        return set(w*s, x*s, y*s, z*s);
    }
    
    public Quat4f div(float s)
    {
        return set(w/s, x/s, y/s, z/s);
    }
    
    public Quat4f lerp(Quat4f q, float t)
    {
        return set(Util.lerp(w, q.w, t),
                   Util.lerp(x, q.x, t),
                   Util.lerp(y, q.y, t),
                   Util.lerp(z, q.z, t));
    }
    
    public Quat4f slerp(Quat4f q, float t) //Source: [1]
    {
        q = q.copy();
        
        float cos = dot(q);
        
        if (cos < 0f)
        {
            q.negate();
            cos = -cos;
        }
        
        if (Util.epsEqual(cos, 1f, 1<<6)) return lerp(q, t);
        
        final float angle = (float)Math.acos(cos); //Util.acos has redundant degrees conversion.
        
        return mult( (float)Math.sin((1f - t)*angle) )
               .add( q.mult((float)Math.sin(t*angle)) )
               .div( (float)Math.sin(angle) );
    }
    
    public Quat4f normalize()
    {
        return div(length());
    }
    
    public Quat4f negate()
    {
        return set(-w, -x, -y, -z);
    }
    
    public Quat4f conjugate()
    {
        return set(w, -x, -y, -z);
    }
    
    public Quat4f invert()
    {
        return conjugate().div(squareLength());
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Accessor Methods">
    public float dot(Quat4f q)
    {
        return w*q.w + x*q.x + y*q.y + z*q.z;
    }
    
    public Matrix3f toMatrix3f() //Source: [2]
    {
        float q0 = SQRT_2*w, q1 = SQRT_2*x, q2 = SQRT_2*y, q3 = SQRT_2*z;

	float qda = q0*q1, qdb = q0*q2, qdc = q0*q3;
	float qaa = q1*q1, qab = q1*q2, qac = q1*q3;
	float qbb = q2*q2, qbc = q2*q3, qcc = q3*q3;
        
        return new Matrix3f(
                1.0f - qbb - qcc, -qdc + qab, qdb + qac,
                qdc + qab, 1.0f - qaa - qcc, -qda + qbc,
                -qdb + qac, qda + qbc, 1.0f - qaa - qbb);
    }
    
    public Matrix4f toMatrix4f()
    {
        float q0 = SQRT_2*w, q1 = SQRT_2*x, q2 = SQRT_2*y, q3 = SQRT_2*z;

	float qda = q0*q1, qdb = q0*q2, qdc = q0*q3;
	float qaa = q1*q1, qab = q1*q2, qac = q1*q3;
	float qbb = q2*q2, qbc = q2*q3, qcc = q3*q3;
        
        return new Matrix4f(
                1.0f - qbb - qcc, -qdc + qab, qdb + qac, 0.0f,
                qdc + qab, 1.0f - qaa - qcc, -qda + qbc, 0.0f,
                -qdb + qac, qda + qbc, 1.0f - qaa - qbb, 0.0f,
                0.0f, 0.0f, 0.0f, 1.0f);
    }
    
    public float squareLength()
    {
        return w*w + x*x + y*y + z*z;
    }

    public float length()
    {
        return Util.sqrt(squareLength());
    }
    
    public boolean equals(Quat4f q)
    {
        return w == q.w &&
               x == q.x &&
               y == q.y &&
               z == q.z;
    }
    
    public boolean epsEqual(Quat4f q, int tolerance)
    {
        return Util.epsEqual(q.w, w, tolerance) &&
               Util.epsEqual(q.w, x, tolerance) &&
               Util.epsEqual(q.x, y, tolerance) &&
               Util.epsEqual(q.y, z, tolerance);
    }
    
    public Quat4f copy()
    {
        return new Quat4f(w, x, y, z);
    }
    // </editor-fold>
    @Override
    public String toString()
    {
        return "("+w+": "+x+", "+y+", "+z+")";
    }
}
