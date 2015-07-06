
package com.samrj.devil.math;

public class Mat3
{
    /**
     * Returns the determinant of the given matrix.
     * 
     * @param m The matrix to calculate the determinant of.
     * @return The determinant of the given matrix.
     */
    public static final float determinant(Mat3 m)
    {
        return m.a*(m.e*m.i - m.f*m.h) +
               m.b*(m.f*m.g - m.d*m.i) +
               m.c*(m.d*m.h - m.e*m.g);
    }
    //INCOMPLETE
    
    public float a, b, c,
                 d, e, f,
                 g, h, i;
    
    /**
     * Creates a new 3x3 zero matrix, NOT an identity matrix. Use identity() to
     * create an identity matrix.
     */
    public Mat3()
    {
    }
    
    /**
     * Creates a new 3x3 matrix with the given values.
     */
    public Mat3(float a, float b, float c,
                float d, float e, float f,
                float g, float h, float i)
    {
        this.a = a; this.b = b; this.c = c;
        this.d = d; this.e = e; this.f = f;
        this.g = g; this.h = h; this.i = i;
    }
    
    /**
     * Copies the given 3x3 matrix.
     * 
     * @param mat The matrix to copy.
     */
    public Mat3(Mat3 mat)
    {
        a = mat.a; b = mat.b; c = mat.c;
        d = mat.d; e = mat.e; f = mat.f;
        g = mat.g; h = mat.h; i = mat.i;
    }
}
