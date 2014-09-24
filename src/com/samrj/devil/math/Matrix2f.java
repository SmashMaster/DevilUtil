package com.samrj.devil.math;

import com.samrj.devil.buffer.Bufferable;
import com.samrj.devil.buffer.FloatBuffer;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class Matrix2f implements Bufferable<FloatBuffer>, Matrix<Matrix2f>
{
    // <editor-fold defaultstate="collapsed" desc="Factory Methods">
    public static Matrix2f identity()
    {
        return new Matrix2f();
    }
    
    public static Matrix2f scale(float x, float y)
    {
        return new Matrix2f(x, 0,
                            0, y);
    }
    
    public static Matrix2f rotate(float a)
    {
        float sin = Util.sin(a);
        float nsn = -sin;
        float cos = Util.cos(a);
        
        return new Matrix2f(cos, nsn,
                            sin, cos);
    }
    // </editor-fold>
    
    public float a, b,
                 c, d;
    
    // <editor-fold defaultstate="collapsed" desc="Constructors">
    public Matrix2f(float a, float b,
                    float c, float d)
    {
        set(a, b,
            c, d);
    }
    
    public Matrix2f(Matrix2f z)
    {
        set(z);
    }
    
    public Matrix2f()
    {
        set();
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Mutator Methods">
    public Matrix2f set(float a, float b,
                        float c, float d)
    {
        this.a = a; this.b = b;
        this.c = c; this.d = d;
        return this;
    }
    
    @Override
    public Matrix2f set(Matrix2f z)
    {
        return set(z.a, z.b,
                   z.c, z.d);
    }
    
    @Override
    public Matrix2f set()
    {
        return set(1, 0,
                   0, 1);
    }
    
    @Override
    public Matrix2f mult(Matrix2f z)
    {
        return set(a*z.a + b*z.c,
                   a*z.b + b*z.d,
                   
                   c*z.a + d*z.c,
                   c*z.b + d*z.d);
    }
    
    public Matrix2f multScale(float x, float y)
    {
        return set(a*x, b*y,
                   c*x, d*y);
    }
    
    public Matrix2f multRotate(float angle)
    {
        float sin = Util.sin(a);
        float nsn = -sin;
        float cos = Util.cos(a);
        
        return set(a*cos + b*sin, a*nsn + b*cos,
                   c*cos + d*sin, c*nsn + d*cos);
    }
    
    @Override
    public Matrix2f mult(float s)
    {
        return set(a*s, b*s,
                   c*s, d*s);
    }
    
    @Override
    public Matrix2f div(float s)
    {
        return set(a/s, b/s,
                   c/s, d/s);
    }
    
    @Override
    public Matrix2f invert()
    {
        final float det = determinant();
        
        if (det == 0f) throw new SingularMatrixException();
        
        return set(d, -b,
                  -c,  a).div(det);
    }
    
    @Override
    public Matrix2f transpose()
    {
        return set(a, c,
                   b, d);
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Accessor Methods">
    @Override
    public float determinant()
    {
        return a*d - b*c;
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Overriden Object Methods">
    @Override
    public Matrix2f clone()
    {
        return new Matrix2f(this);
    }
    
    @Override
    public String toString()
    {
        return "[" + a + ", " + b + "]\n" +
               "[" + c + ", " + d + "]";
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Overriden Bufferable Methods">
    @Override
    public void putIn(FloatBuffer buf)
    {
        buf.put(a, b,
                c, d);
    }
    
    @Override
    public int size()
    {
        return 4;
    }
    // </editor-fold>
}
