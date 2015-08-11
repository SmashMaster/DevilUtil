package com.samrj.devil.math;

import com.samrj.devil.io.Bufferable;
import com.samrj.devil.io.Streamable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * 2x2 matrix class.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Mat2 implements Bufferable, Streamable 
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
     * @param s The matrix to copy from.
     * @param r The matrix to copy into.
     */
    public static final void copy(Mat2 s, Mat2 r)
    {
        r.a = s.a; r.b = s.b;
        r.c = s.c; r.d = s.d;
    }
    
    /**
     * Contracts and copies the source matrix into the target matrix. 
     * 
     * @param s The matrix to copy from.
     * @param r The matrix to copy into.
     */
    public static final void copy(Mat3 s, Mat2 r)
    {
        r.a = s.a; r.b = s.b;
        r.c = s.d; r.d = s.e;
    }
    
    /**
     * Contracts and copies the source matrix into the target matrix. 
     * 
     * @param s The matrix to copy from.
     * @param r The matrix to copy into.
     */
    public static final void copy(Mat4 s, Mat2 r)
    {
        r.a = s.a; r.b = s.b;
        r.c = s.e; r.d = s.f;
    }
    
    /**
     * Sets the given matrix to the identity matrix.
     * 
     * @param r The matrix to set to the identity matrix.
     */
    public static final void identity(Mat2 r)
    {
        scaling(1.0f, r);
    }
    
    /**
     * Sets the given matrix to a scaling matrix by the given scalar.
     * 
     * @param s The scalar to scale by.
     * @param r The matrix in which to store the result.
     */
    public static final void scaling(float s, Mat2 r)
    {
        r.a = s; r.b = 0.0f;
        r.c = 0.0f; r.d = s;
    }
    
    /**
     * Sets the given matrix to the rotation matrix by the given angle.
     * 
     * @param angle The angle to rotate by.
     * @param r The matrix in which to store the result.
     */
    public static final void rotation(float angle, Mat2 r)
    {
        r.a = (float)Math.cos(angle); 
        r.c = (float)Math.sin(angle);
        r.b = -r.c;
        r.d = r.a;
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
     * @param angle The angle to rotate by.
     * @return A new rotation matrix.
     */
    public static final Mat2 rotation(float angle)
    {
        Mat2 m = new Mat2();
        rotation(angle, m);
        return m;
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
    
    /**
     * Contracts the given 3x3 matrix.
     * 
     * @param mat The matrix to copy.
     */
    public Mat2(Mat3 mat)
    {
        a = mat.a; b = mat.b;
        c = mat.d; d = mat.e;
    }
    
    /**
     * Contracts the given 4x4 matrix.
     * 
     * @param mat The matrix to copy.
     */
    public Mat2(Mat4 mat)
    {
        a = mat.a; b = mat.b;
        c = mat.e; d = mat.f;
    }
    
    /**
     * Returns the determinant of this matrix.
     * 
     * @return The determinant of this matrix.
     */
    public float determinant()
    {
        return determinant(this);
    }
    
    // <editor-fold defaultstate="collapsed" desc="Instance mutator methods">
    /**
     * Sets this to the given matrix.
     * 
     * @param mat The matrix to set this to.
     * @return This matrix.
     */
    public Mat2 set(Mat2 mat)
    {
        copy(mat, this);
        return this;
    }
    
    /**
     * Sets this to the upper left corner of the given matrix.
     * 
     * @param mat The matrix to set this to.
     * @return This matrix.
     */
    public Mat2 set(Mat3 mat)
    {
        copy(mat, this);
        return this;
    }
    
    /**
     * Sets this to the upper left corner of the given matrix.
     * 
     * @param mat The matrix to set this to.
     * @return This matrix.
     */
    public Mat2 set(Mat4 mat)
    {
        copy(mat, this);
        return this;
    }
    
    /**
     * Sets the entries of this matrix.
     * 
     * @return This matrix.
     */
    public Mat2 set(float a, float b,
                    float c, float d)
    {
        this.a = a; this.b = b;
        this.c = c; this.d = d;
        return this;
    }
    
    /**
     * Sets this to the identity matrix.
     * 
     * @return This matrix.
     */
    public Mat2 setIdentity()
    {
        identity(this);
        return this;
    }
    
    /**
     * Sets this to the scaling matrix by the given scalar.
     * 
     * @param sca The scalar to scale by.
     * @return This matrix.
     */
    public Mat2 setScaling(float sca)
    {
        scaling(sca, this);
        
        return this;
    }
    
    /**
     * Sets this to the rotation matrix by the given angle.
     * 
     * @param ang The angle to rotate by.
     * @return This matrix.
     */
    public Mat2 setRotation(float ang)
    {
        rotation(ang, this);
        return this;
    }
    
    /**
     * Rotates this matrix by the given angle.
     * 
     * @param ang The angle to rotate by.
     * @return This matrix.
     */
    public Mat2 rotate(float ang)
    {
        rotate(this, ang, this);
        return this;
    }
    
    /**
     * Multiplies this matrix by the given matrix.
     * 
     * @param mat The right-hand matrix to multiply by.
     * @return This matrix.
     */
    public Mat2 mult(Mat2 mat)
    {
        mult(this, mat, this);
        return this;
    }
    
    /**
     * Multiplies each entry in this matrix by the given scalar. Equivalent to
     * scaling this matrix by the given scalar.
     * 
     * @param sca The scalar to multiply by.
     */
    public Mat2 mult(float sca)
    {
        mult(this, sca, this);
        return this;
    }
    
    /**
     * Divides each entry in this matrix by the given scalar.
     * 
     * @param sca The scalar to divide by.
     * @return This matrix.
     */
    public Mat2 div(float sca)
    {
        div(this, sca, this);
        
        return this;
    }
    
    /**
     * Transposes this matrix.
     * 
     * @return This matrix.
     */
    public Mat2 transpose()
    {
        transpose(this, this);
        return this;
    }
    
    /**
     * Inverts this matrix.
     * 
     * @return This matrix.
     * @throws com.samrj.devil.math.SingularMatrixException If this matrix is
     *         singular. (Its determinant is zero.)
     */
    public Mat2 invert()
    {
        invert(this, this);
        return this;
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Overriden/implemented methods">
    /**
     * WARNING: Buffered in column-major format, as per OpenGL.
     */
    @Override
    public void read(ByteBuffer buffer)
    {
        a = buffer.getFloat(); c = buffer.getFloat();
        b = buffer.getFloat(); d = buffer.getFloat();
    }

    @Override
    public void write(ByteBuffer buffer)
    {
        buffer.putFloat(a); buffer.putFloat(c);
        buffer.putFloat(b); buffer.putFloat(d);
    }
    
    @Override
    public int bufferSize()
    {
        return 4*4;
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
