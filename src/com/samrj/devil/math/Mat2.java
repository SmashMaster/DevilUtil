package com.samrj.devil.math;

import com.samrj.devil.io.Bufferable;
import com.samrj.devil.io.Streamable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.FloatBuffer;

/**
 * Optimized 2x2 matrix class.
 * 
 * @author SmashMaster
 * @copyright 2015 Samuel Johnson
 */
public class Mat2 implements Bufferable<FloatBuffer>, Streamable 
{
    /**
     * Returns the determinant of the given matrix.
     * 
     * @param m The matrix to calculate the determinant of.
     * @return The determinant of the given matrix.
     */
    public static final float determinant(Mat2 m)
    {
        return m.a*m.d - m.b*m.c;
    }
    
    // <editor-fold defaultstate="collapsed" desc="Static mutator methods">
    /**
     * Copies the source matrix into the target matrix. 
     * 
     * @param m The matrix to copy from.
     * @param r The matrix to copy into.
     */
    public static final void copy(Mat2 m, Mat2 r)
    {
        r.a = m.a; r.b = m.b;
        r.c = m.c; r.d = m.d;
    }
    
    /**
     * Rotates {@code m} by {@code ang} and stores the result in {@code r}.
     * 
     * @param m The matrix to rotate.
     * @param angle The angle to rotate by.
     * @param r The matrix in which to store the result.
     */
    public static final void rotate(Mat2 m, float angle, Mat2 r)
    {
        float sin = (float)Math.sin(angle);
        float nsn = -sin;
        float cos = (float)Math.cos(angle);
        
        float a = m.a*cos + m.b*sin, b = m.a*nsn + m.b*cos;
        float c = m.c*cos + m.d*sin, d = m.c*nsn + m.d*cos;
        
        r.a = a; r.b = b;
        r.c = c; r.d = d;
    }
    
    /**
     * Performs a matrix multiplication on {@code m0} and {@code m1}, and stores
     * the result in {@code r}. 
     * 
     * @param m0 The left-hand matrix to multiply.
     * @param m1 The right-hand matrix to multiply by.
     * @param r The matrix in which to store the result.
     */
    public static final void mult(Mat2 m0, Mat2 m1, Mat2 r)
    {
        float a = m0.a*m1.a + m0.b*m1.c;
        float b = m0.a*m1.b + m0.b*m1.d;
        
        float c = m0.c*m1.a + m0.d*m1.c;
        float d = m0.c*m1.b + m0.d*m1.d;
        
        r.a = a; r.b = b;
        r.c = c; r.d = d;
    }
    
    /**
     * Multiplies each entry in the given matrix by the given scalar.
     * 
     * @param m The matrix to multiply.
     * @param s The scalar to multiply by.
     * @param r The matrix in which to store the result.
     */
    public static final void mult(Mat2 m, float s, Mat2 r)
    {
        r.a = m.a*s; r.b = m.b*s;
        r.c = m.c*s; r.d = m.d*s;
    }
    
    /**
     * Divides the given matrix by the given scalar.
     * 
     * @param m The matrix to divide.
     * @param s The scalar to divide by.
     * @param r The matrix in which to store the result.
     */
    public static final void div(Mat2 m, float s, Mat2 r)
    {
        r.a = m.a/s; r.b = m.b/s;
        r.c = m.c/s; r.d = m.d/s;
    }
    
    /**
     * Sets {@code r} to the transpose of {@code m}.
     * 
     * @param m The matrix to compute the transpose of.
     * @param r The matrix in which to store the result.
     */
    public static final void transpose(Mat2 m, Mat2 r)
    {
        float tb = m.b;
        r.a = m.a; r.b = m.c;
        r.c = tb;  r.d = m.d;
    }
    
    /**
     * Calculates the inverse of {@code m} and stores the result in {@code r}.
     * 
     * @param m The matrix to compute the inverse of.
     * @param r The matrix in which to store the result.
     * @throws com.samrj.devil.math.SingularMatrixException If {@code m} is
     *         a singular matrix. (Its determinant is zero.)
     */
    public static final void invert(Mat2 m, Mat2 r)
    {
        float det = determinant(m);
        if (det == 0.0f) throw new SingularMatrixException();
        
        float a =  m.d/det, b = -m.b/det;
        float c = -m.c/det, d =  m.a/det;
        
        r.a = a; r.b = b;
        r.c = c; r.d = d;
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Static factory methods">
    /**
     * Returns a new 2x2 identity matrix.
     * 
     * @return A new 2x2 identity matrix.
     */
    public static final Mat2 identity()
    {
        return scaling(1.0f);
    }
    
    /**
     * Returns a new scaling matrix, where {@code s} is the scaling factor.
     * 
     * @param s The scaling factor.
     * @return A new scaling matrix.
     */
    public static final Mat2 scaling(float s)
    {
        Mat2 m = new Mat2();
        m.a = s;
        m.d = s;
        return m;
    }
    
    /**
     * Returns a new rotation matrix, where {@code a} is the angle to rotate by.
     * 
     * @param a The angle to rotate by.
     * @return A new rotation matrix.
     */
    public static final Mat2 rotation(float a)
    {
        float sin = (float)Math.sin(a);
        float cos = (float)Math.cos(a);
        
        return new Mat2(cos, -sin,
                        sin,  cos);
    }
    /**
     * Multiplies {@code m0} by {@code m1} and returns the result as a new matrix.
     * 
     * @param m0 The left-hand matrix to multiply.
     * @param m1 The right-hand matrix to multiply by.
     * @return A new matrix containing the result.
     */
    public static final Mat2 mult(Mat2 m0, Mat2 m1)
    {
        Mat2 result = new Mat2();
        mult(m0, m1, result);
        return result;
    }
    
    /**
     * Multiplies {@code m} by {@code s} and returns the result as a new matrix.
     * 
     * @param m The matrix to multiply.
     * @param s The scalar to multiply by.
     * @return A new matrix containing the result.
     */
    public static final Mat2 mult(Mat2 m, float s)
    {
        Mat2 result = new Mat2();
        mult(m, s, result);
        return result;
    }
    
    /**
     * Divides {@code m} by {@code s} and returns the result as a new matrix.
     * 
     * @param m The matrix to divide.
     * @param s The scalar to divide by.
     * @return A new matrix containing the result.
     */
    public static final Mat2 div(Mat2 m, float s)
    {
        Mat2 result = new Mat2();
        div(m, s, result);
        return result;
    }
    
    /**
     * Returns the transpose of {@code m} as a new matrix.
     * 
     * @param m The matrix to compute the transpose of.
     * @return A new matrix containing the result.
     */
    public static final Mat2 transpose(Mat2 m)
    {
        Mat2 result = new Mat2();
        transpose(m, result);
        return result;
    }
    
    /**
     * Calculates the inverse of {@code m} and returns the result as a new matrix.
     * 
     * @param m The matrix to compute the inverse of.
     * @return A new matrix containing the result.
     * @throws com.samrj.devil.math.SingularMatrixException If {@code m} is
     *         a singular matrix. (Its determinant is zero.)
     */
    public static final Mat2 invert(Mat2 m)
    {
        Mat2 result = new Mat2();
        invert(m, result);
        return result;
    }
    // </editor-fold>
    
    public float a, b,
                 c, d;
    
    /**
     * Creates a new 2x2 zero matrix, NOT an identity matrix. Use identity() to
     * create an identity matrix.
     */
    public Mat2()
    {
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
     * @param mat The matrix to copy.
     */
    public Mat2(Mat2 mat)
    {
        a = mat.a; b = mat.b;
        c = mat.c; d = mat.d;
    }
    
    public float determinant()
    {
        return determinant(this);
    }
    
    // <editor-fold defaultstate="collapsed" desc="Instance mutator methods">
    /**
     * Sets this to the given matrix.
     * 
     * @param mat The matrix to set this to.
     */
    public void set(Mat2 mat)
    {
        copy(mat, this);
    }
    
    /**
     * Rotates this matrix by the given angle.
     * 
     * @param ang The angle to rotate by.
     */
    public void rotate(float ang)
    {
        rotate(this, ang, this);
    }
    
    /**
     * Multiplies this matrix by the given matrix.
     * 
     * @param mat The right-hand matrix to multiply by.
     */
    public void mult(Mat2 mat)
    {
        mult(this, mat, this);
    }
    
    /**
     * Multiplies each entry in this matrix by the given scalar.
     * 
     * @param sca The scalar to multiply by.
     */
    public void mult(float sca)
    {
        mult(this, sca, this);
    }
    
    /**
     * Divides each entry in this matrix by the given scalar.
     * 
     * @param sca The scalar to divide by.
     */
    public void div(float sca)
    {
        div(this, sca, this);
    }
    
    /**
     * Transposes this matrix.
     */
    public void transpose()
    {
        transpose(this, this);
    }
    
    /**
     * Inverts this matrix.
     * 
     * @throws com.samrj.devil.math.SingularMatrixException If this matrix is
     *         singular. (Its determinant is zero.)
     */
    public void invert()
    {
        invert(this, this);
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Overriden/implemented methods">
    /**
     * WARNING: Buffered in column-major format, as per OpenGL.
     */
    @Override
    public void read(FloatBuffer buffer)
    {
        a = buffer.get(); c = buffer.get();
        b = buffer.get(); d = buffer.get();
    }

    @Override
    public void write(FloatBuffer buffer)
    {
        buffer.put(a); buffer.put(c);
        buffer.put(b); buffer.put(d);
    }
    
    /**
     * Written to/read from stream in row-major format.
     */
    @Override
    public void read(DataInputStream in) throws IOException
    {
        a = in.readFloat(); b = in.readFloat();
        c = in.readFloat(); d = in.readFloat();
    }

    @Override
    public void write(DataOutputStream out) throws IOException
    {
        out.writeFloat(a); out.writeFloat(b);
        out.writeFloat(c); out.writeFloat(d);
    }
    
    @Override
    public String toString()
    {
        return "[" + a + ", " + b + "]\n" +
               "[" + c + ", " + d + "]";
    }
    // </editor-fold>
}
