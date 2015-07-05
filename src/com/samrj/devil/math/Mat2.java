package com.samrj.devil.math;

public class Mat2
{
    ~~INCOMPLETE~~
    
    public static final Mat2 scale(float s)
    {
        return new Mat2(s, 0.0f,
                        0.0f, s);
    }
    
    public static final Mat2 rotate(float a)
    {
        final float sin = (float)Math.sin(a);
        final float cos = (float)Math.cos(a);
        
        return new Mat2(cos, -sin,
                        sin,  cos);
    }
    
    public static final void mult(Mat2 m0, Mat2 m1, Mat2 result)
    {
        final float a = m0.a*m1.a + m0.b*m1.c;
        final float b = m0.a*m1.b + m0.b*m1.d;
        
        final float c = m0.c*m1.a + m0.d*m1.c;
        final float d = m0.c*m1.b + m0.d*m1.d;
        
        result.a = a; result.b = b;
        result.c = c; result.d = d;
    }
    
    public static final void mult(Mat2 m, float s, Mat2 result)
    {
        result.a = m.a*s; result.b = m.b*s;
        result.c = m.c*s; result.d = m.d*s;
    }
    
    public static final void div(Mat2 m, float s, Mat2 result)
    {
        result.a = m.a/s; result.b = m.b/s;
        result.c = m.c/s; result.d = m.d/s;
    }
    
    public float a, b,
                 c, d;
    
    /**
     * Creates a new 2x2 identity matrix.
     */
    public Mat2()
    {
        a = 1.0f; d = 1.0f;
    }
    
    /**
     * Creates a new 2x2 matrix with the given values.
     */
    public Mat2(float a, float b,
                float c, float d)
    {
        this.a = a; this.b = b;
        this.c = c; this.d = d;
    }
    
    public float determinant()
    {
        return a*d - b*c;
    }
    
    /**
     * Copies the given 2x2 matrix.
     * 
     * @param m The matrix to copy.
     */
    public Mat2(Mat2 m)
    {
        a = m.a; b = m.b;
        c = m.c; d = m.d;
    }
    
    public void set(Mat2 m)
    {
        a = m.a; b = m.b;
        c = m.c; d = m.d;
    }
}
