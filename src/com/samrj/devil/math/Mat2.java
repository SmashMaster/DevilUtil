/*
 * Copyright (c) 2020 Sam Johnson
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.samrj.devil.math;

import com.samrj.devil.util.DataStreamable;
import com.samrj.devil.util.FloatBufferable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

/**
 * 2x2 matrix class.
 * 
 * @author Samuel Johnson (SmashMaster)
 */
public class Mat2 implements FloatBufferable, DataStreamable<Mat2>
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
     * Sets each entry of the given matrix to zero.
     * 
     * @param r The matrix to set to zero.
     */
    public static final void zero(Mat2 r)
    {
        r.a = 0.0f; r.b = 0.0f;
        r.c = 0.0f; r.d = 0.0f;
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
     * Sets the given matrix to a component-wise scaling matrix using the given
     * vector.
     * 
     * @param v The vector to scale by.
     * @param r The matrix in which to store the result.
     */
    public static final void scaling(Vec2 v, Mat2 r)
    {
        r.a = v.x; r.b = 0.0f;
        r.c = 0.0f; r.d = v.y;
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
     * Multiplies the given matrix by the given vector.
     * 
     * @param m The matrix to multiply.
     * @param v The vector to multiply by.
     * @param r The matrix in which to store the result.
     */
    public static final void mult(Mat2 m, Vec2 v, Mat2 r)
    {
        r.a = m.a*v.x; r.b = m.b*v.y;
        r.c = m.c*v.x; r.d = m.d*v.y;
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
     * Returns a new component-wise scaling matrix using the given vector.
     * 
     * @param v The vector to scale by.
     * @return A new scaling matrix.
     */
    public static final Mat2 scaling(Vec2 v)
    {
        Mat2 m = new Mat2();
        m.a = v.x;
        m.d = v.y;
        return m;
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
     * Multiplies {@code m} by {@code v} and returns the result as a new matrix.
     * 
     * @param m The matrix to multiply.
     * @param v The vector to multiply by.
     * @return A new matrix containing the result.
     */
    public static final Mat2 mult(Mat2 m, Vec2 v)
    {
        Mat2 result = new Mat2();
        mult(m, v, result);
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
    
    // <editor-fold defaultstate="collapsed" desc="Constructors">
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
     * Loads a new matrix from the given buffer.
     *
     * @param buffer The buffer to read from.
     */
    public Mat2(ByteBuffer buffer)
    {
        Mat2.this.read(buffer);
    }

    /**
     * Loads a new matrix from the given input stream.
     * 
     * @param in The input stream to read from.
     * @throws IOException If an io error occurred.
     */
    public Mat2(DataInputStream in) throws IOException
    {
        Mat2.this.read(in);
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Instance accessor methods">
    /**
     * Returns the entry at the specified position in this matrix.
     * 
     * @param row A row of this matrix.
     * @param column A column of this matrix.
     * @return The entry at the given row and column.
     */
    public float getEntry(int row, int column)
    {
        switch (row)
        {
            case 0: switch (column)
            {
                case 0: return a;
                case 1: return b;
                default: throw new ArrayIndexOutOfBoundsException();
            }
            case 1: switch (column)
            {
                case 0: return c;
                case 1: return d;
                default: throw new ArrayIndexOutOfBoundsException();
            }
            default: throw new ArrayIndexOutOfBoundsException();
        }
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
    // </editor-fold>
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
     * Sets the entry at the given position in this matrix to the given float.
     * 
     * @param row A row of this matrix.
     * @param column A column of this matrix.
     * @param v The value to set the entry to.
     * @return This matrix.
     */
    public Mat2 setEntry(int row, int column, float v)
    {
        switch (row)
        {
            case 0: switch (column)
            {
                case 0: a = v; return this;
                case 1: b = v; return this;
                default: throw new ArrayIndexOutOfBoundsException();
            }
            case 1: switch (column)
            {
                case 0: c = v; return this;
                case 1: d = v; return this;
                default: throw new ArrayIndexOutOfBoundsException();
            }
            default: throw new ArrayIndexOutOfBoundsException();
        }
    }
    
    /**
     * Sets the specified row of this matrix to the given vector.
     * 
     * @param v The vector to copy.
     * @param row The row of the matrix to copy to.
     * @return This matrix.
     */
    public Mat2 setRow(Vec2 v, int row)
    {
        switch (row)
        {
            case 0: a = v.x;
                    b = v.y; return this;
            case 1: c = v.x;
                    d = v.y; return this;
            default: throw new ArrayIndexOutOfBoundsException();
        }
    }
    
    /**
     * Sets the specified column of this matrix to the given vector.
     * 
     * @param v The vector to copy.
     * @param column The column of the matrix to copy to.
     * @return This matrix.
     */
    public Mat2 setColumn(Vec2 v, int column)
    {
        switch (column)
        {
            case 0: a = v.x;
                    c = v.y; return this;
            case 1: b = v.x;
                    d = v.y; return this;
            default: throw new ArrayIndexOutOfBoundsException();
        }
    }
    
    /**
     * Sets this to the identity matrix.
     * 
     * @return This matrix.
     */
    public Mat2 setZero()
    {
        zero(this);
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
     * Sets this to the component-wise scaling matrix by the given vector.
     * 
     * @param vec The vector to scale by.
     * @return This matrix.
     */
    public Mat2 setScaling(Vec2 vec)
    {
        scaling(vec, this);
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
     * Multiplies this matrix by the given vector.
     * 
     * @param vec The matrix to multiply by.
     * @return This matrix.
     */
    public Mat2 mult(Vec2 vec)
    {
        mult(this, vec, this);
        return this;
    }
    
    /**
     * Multiplies each entry in this matrix by the given scalar. Equivalent to
     * scaling this matrix by the given scalar.
     * 
     * @param sca The scalar to multiply by.
     * @return This matrix.
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
    
    @Override
    public int bufferSize()
    {
        return 4*4;
    }
    
    /**
     * Written to/read from stream in row-major format.
     */
    @Override
    public Mat2 read(DataInputStream in) throws IOException
    {
        a = in.readFloat(); b = in.readFloat();
        c = in.readFloat(); d = in.readFloat();
        return this;
    }

    @Override
    public Mat2 write(DataOutputStream out) throws IOException
    {
        out.writeFloat(a); out.writeFloat(b);
        out.writeFloat(c); out.writeFloat(d);
        return this;
    }
    
    @Override
    public String toString()
    {
        return "[" + a + ", " + b + "]\n" +
               "[" + c + ", " + d + "]";
    }
    
    public boolean equals(Mat2 mat)
    {
        if (mat == null) return false;
        
        return a == mat.a && b == mat.b &&
               c == mat.c && d == mat.d;
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null) return false;
        if (getClass() != o.getClass()) return false;
        final Mat2 mat = (Mat2)o;
        return equals(mat);
    }
    
    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 61*hash + Float.floatToIntBits(a);
        hash = 61*hash + Float.floatToIntBits(b);
        hash = 61*hash + Float.floatToIntBits(c);
        hash = 61*hash + Float.floatToIntBits(d);
        return hash;
    }
    // </editor-fold>
}
