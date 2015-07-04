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
