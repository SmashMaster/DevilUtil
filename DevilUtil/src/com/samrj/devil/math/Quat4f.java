package com.samrj.devil.math;

import com.samrj.devil.math.Util.Axis;

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
     * [2] http://en.wikipedia.org/wiki/Rotation_matrix#Quaternion
     */
    
    // <editor-fold defaultstate="collapsed" desc="Factory Methods">
    public static Quat4f axisAngle(Vector3f axis, float angle) //Apapted from [0]
    {
        final float a = angle*.5f;
        final float sin = Util.sin(a);
        final float cos = Util.cos(a);
        
        axis = axis.clone().setLength(sin);
        
        return new Quat4f(cos, axis.x, axis.y, axis.z);
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
        q = q.clone();
        
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
        final float x2 = x*2f, y2 = y*2f, z2 = z*2f;
        
        final float wx = w*x2, wy = w*y2, wz = w*z2;
        final float xx = x*x2, xy = x*y2, xz = x*z2;
        final float            yy = y*y2, yz = y*z2;
        final float                       zz = z*z2;
        
        return new Matrix3f(1f - yy - zz,      xy - wz,      xz + wy,
                            xy + wz,      1f - xx - zz,      yz - wx,
                            xz - wy,           yz + wx, 1f - xx - yy).transpose();
    }
    
    public Matrix4f toMatrix4f() //Source: [2]
    {
        final float x2 = x*2f, y2 = y*2f, z2 = z*2f;
        
        final float wx = w*x2, wy = w*y2, wz = w*z2;
        final float xx = x*x2, xy = x*y2, xz = x*z2;
        final float            yy = y*y2, yz = y*z2;
        final float                       zz = z*z2;
        
        return new Matrix4f(1f - yy - zz,      xy - wz,      xz + wy, 0,
                            xy + wz,      1f - xx - zz,      yz - wx, 0,
                            xz - wy,           yz + wx, 1f - xx - yy, 0,
                                  0,                 0,            0, 1).transpose();
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
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Overriden Object Methods">
    @Override
    public String toString()
    {
        return "("+w+": "+x+", "+y+", "+z+")";
    }
    
    @Override
    public Quat4f clone()
    {
        return new Quat4f(w, x, y, z);
    }
    // </editor-fold>
}