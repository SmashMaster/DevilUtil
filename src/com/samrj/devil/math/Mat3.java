/*
 * Copyright (c) 2015 Sam Johnson
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

import com.samrj.devil.io.Bufferable;
import com.samrj.devil.io.Streamable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * 3x3 matrix class.
 * 
 * @author Samuel Johnson (SmashMaster)
 */
public class Mat3 implements Bufferable, Streamable 
{
    private static final float SQRT_2 = (float)Math.sqrt(2.0);
    
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
    
    // <editor-fold defaultstate="collapsed" desc="Static mutator methods">
    /**
     * Expands and copies the source matrix into the target matrix. 
     * 
     * @param s The matrix to copy from.
     * @param r The matrix to copy into.
     */
    public static final void copy(Mat2 s, Mat3 r)
    {
        r.a = s.a; r.b = s.b; r.c = 0.0f;
        r.d = s.c; r.e = s.d; r.f = 0.0f;
        r.g = 0.0f; r.h = 0.0f; r.i = 1.0f;
    }
    
    /**
     * Copies the source matrix into the target matrix. 
     * 
     * @param s The matrix to copy from.
     * @param r The matrix to copy into.
     */
    public static final void copy(Mat3 s, Mat3 r)
    {
        r.a = s.a; r.b = s.b; r.c = s.c;
        r.d = s.d; r.e = s.e; r.f = s.f;
        r.g = s.g; r.h = s.h; r.i = s.i;
    }
    
    /**
     * Contracts and copies the source matrix into the target matrix. 
     * 
     * @param s The matrix to copy from.
     * @param r The matrix to copy into.
     */
    public static final void copy(Mat4 s, Mat3 r)
    {
        r.a = s.a; r.b = s.b; r.c = s.c;
        r.d = s.e; r.e = s.f; r.f = s.g;
        r.g = s.i; r.h = s.j; r.i = s.k;
    }
    
    /**
     * Sets each entry of the given matrix to zero.
     * 
     * @param r The matrix to set to zero.
     */
    public static final void zero(Mat3 r)
    {
        r.a = 0.0f; r.b = 0.0f; r.c = 0.0f;
        r.d = 0.0f; r.e = 0.0f; r.f = 0.0f;
        r.g = 0.0f; r.h = 0.0f; r.i = 0.0f;
    }
    
    /**
     * Sets the given matrix to the identity matrix.
     * 
     * @param r The matrix to set to the identity matrix.
     */
    public static final void identity(Mat3 r)
    {
        scaling(1.0f, r);
    }
    
    /**
     * Sets the given matrix to a symmetric orthographic projection matrix with
     * the given dimensions.
     * 
     * @param width The half-width of the prism.
     * @param height The half-height of the prism.
     * @param r The matrix in which to store the result.
     */
    public static final void orthographic(float width, float height, Mat3 r)
    {
        r.a = 1.0f/width; r.b = 0.0f; r.c = 0.0f;
        r.d = 0.0f; r.e = 1.0f/height; r.f = 0.0f;
        r.g = 0.0f; r.h = 0.0f; r.i = 1.0f;
    }
    
    /**
     * Sets the given matrix to an orthographic projection matrix with the given
     * bounds.
     * 
     * @param left The left bound of the prism.
     * @param right The right bound of the prism.
     * @param bottom The lower bound of the prism.
     * @param top The upper bound of the prism.
     * @param r The matrix in which to store the result.
     */
    public static final void orthographic(float left, float right, float bottom, float top, Mat3 r)
    {
        final float rml = right - left;
        final float tmb = top - bottom;
        
        r.a = 2.0f/rml; r.b = 0.0f; r.c = -(right + left)/rml;
        r.d = 0.0f; r.e = 2.0f/tmb; r.f = -(top + bottom)/tmb;
        r.g = 0.0f; r.h = 0.0f; r.i = 1.0f;
    }
    
    /**
     * Sets the given matrix to a component-wise scaling matrix using the given
     * vector.
     * 
     * @param v The vector to scale by.
     * @param r The matrix in which to store the result.
     */
    public static final void scaling(Vec3 v, Mat3 r)
    {
        r.a = v.x; r.b = 0.0f; r.c = 0.0f;
        r.d = 0.0f; r.e = v.y; r.f = 0.0f;
        r.g = 0.0f; r.h = 0.0f; r.i = v.z;
    }
    
    /**
     * Sets the given matrix to a component-wise scaling matrix using the given
     * vector.
     * 
     * @param v The vector to scale by.
     * @param r The matrix in which to store the result.
     */
    public static final void scaling(Vec2 v, Mat3 r)
    {
        r.a = v.x; r.b = 0.0f; r.c = 0.0f;
        r.d = 0.0f; r.e = v.y; r.f = 0.0f;
        r.g = 0.0f; r.h = 0.0f; r.i = 1.0f;
    }
    
    /**
     * Sets the given matrix to a scaling matrix by the given scalar.
     * 
     * @param s The scalar to scale by.
     * @param r The matrix in which to store the result.
     */
    public static final void scaling(float s, Mat3 r)
    {
        r.a = s; r.b = 0.0f; r.c = 0.0f;
        r.d = 0.0f; r.e = s; r.f = 0.0f;
        r.g = 0.0f; r.h = 0.0f; r.i = s;
    }
    
    /**
     * Sets the given matrix to the rotation matrix, using the given {@code axis}
     * of rotation, and {@code ang} as the angle. The axis must be normalized.
     * 
     * @param axis The axis around which to rotate.
     * @param angle The angle to rotate by.
     * @param r The matrix in which to store the result.
     */
    public static final void rotation(Vec3 axis, float angle, Mat3 r)
    {
        float cos = (float)Math.cos(angle);
        float sin = (float)Math.sin(angle);
        float omcos = 1.0f - cos;
        
        float xsq = axis.x*axis.x, ysq = axis.y*axis.y, zsq = axis.z*axis.z;
        
        float xyomcos = axis.x*axis.y*omcos, zsin = axis.z*sin;
        float xzomcos = axis.x*axis.z*omcos, ysin = axis.y*sin;
        float yzomcos = axis.y*axis.z*omcos, xsin = axis.x*sin;
        
        r.a = xsq + (1.0f - xsq)*cos;
        r.b = xyomcos - zsin;
        r.c = xzomcos + ysin;
        
        r.d = xyomcos + zsin;
        r.e = ysq + (1.0f - ysq)*cos;
        r.f = yzomcos - xsin;
        
        r.g = xzomcos - ysin;
        r.h = yzomcos + xsin;
        r.i = zsq + (1.0f - zsq)*cos;
    }
    
    /**
     * Sets the given matrix to a rotation matrix representation of the given
     * quaternion.
     * 
     * @param q The quaternion to represent as a matrix.
     * @param r The matrix in which to store the result.
     */
    public static final void rotation(Quat q, Mat3 r)
    {
        float q0 = SQRT_2*q.w, q1 = SQRT_2*q.x, q2 = SQRT_2*q.y, q3 = SQRT_2*q.z;

	float qda = q0*q1, qdb = q0*q2, qdc = q0*q3;
	float qaa = q1*q1, qab = q1*q2, qac = q1*q3;
	float qbb = q2*q2, qbc = q2*q3, qcc = q3*q3;
        
        r.a = 1.0f - qbb - qcc; r.b = -qdc + qab; r.c = qdb + qac;
        r.d = qdc + qab; r.e = 1.0f - qaa - qcc; r.f = -qda + qbc;
        r.g = -qdb + qac; r.h = qda + qbc; r.i = 1.0f - qaa - qbb;
    }
    
    /**
     * Sets the given matrix to the translation matrix by the given vector.
     * 
     * @param v The vector to translate by.
     * @param r The matrix in which to store the result.
     */
    public static final void translation(Vec2 v, Mat3 r)
    {
        r.a = 1.0f; r.b = 0.0f; r.c = v.x;
        r.d = 0.0f; r.e = 1.0f; r.f = v.y;
        r.g = 0.0f; r.h = 0.0f; r.i = 1.0f;
    }
    
    /**
     * Rotates {@code m} about the given {@code axis} by the given angle
     * {@code ang} and stores the result in {@code r}. The axis must be
     * normalized.
     * 
     * @param m The matrix to rotate.
     * @param axis The axis to rotate around.
     * @param angle The angle to rotate by.
     * @param r The matrix in which to store the result.
     */
    public static final void rotate(Mat3 m, Vec3 axis, float angle, Mat3 r)
    {
        Mat3 temp = rotation(axis, angle); //Could probably be improved.
        mult(m, temp, r);
    }
    
    /**
     * Rotates {@code m} by the given quaternion and stores the result in {@code r}.
     * 
     * @param m The matrix to rotate.
     * @param q The quaternion to rotate by.
     * @param r The matrix in which to store the result.
     */
    public static final void rotate(Mat3 m, Quat q, Mat3 r)
    {
        Mat3 temp = rotation(q); //Could probably be improved, as above
        mult(m, temp, r);
    }
    
    /**
     * Translates {@code m} by the given vector {@code v}, and stores the result
     * in {@code r}.
     * 
     * @param m The matrix to translate.
     * @param v The vector to translate by.
     * @param r The matrix in which to store the result.
     */
    public static final void translate(Mat3 m, Vec2 v, Mat3 r)
    {
        r.a = m.a; r.b = m.b; r.c = m.a*v.x + m.b*v.y + m.c;
        r.d = m.d; r.e = m.e; r.f = m.d*v.x + m.e*v.y + m.f;
        r.g = m.g; r.h = m.h; r.i = m.g*v.x + m.h*v.y + m.i;
    }
    
    /**
     * Performs a matrix multiplication on {@code m0} and {@code m1}, and stores
     * the result in {@code r}. 
     * 
     * @param m0 The left-hand matrix to multiply.
     * @param m1 The right-hand matrix to multiply by.
     * @param r The matrix in which to store the result.
     */
    public static final void mult(Mat3 m0, Mat3 m1, Mat3 r)
    {
        float a = m0.a*m1.a + m0.b*m1.d + m0.c*m1.g;
        float b = m0.a*m1.b + m0.b*m1.e + m0.c*m1.h;
        float c = m0.a*m1.c + m0.b*m1.f + m0.c*m1.i;
        
        float d = m0.d*m1.a + m0.e*m1.d + m0.f*m1.g;
        float e = m0.d*m1.b + m0.e*m1.e + m0.f*m1.h;
        float f = m0.d*m1.c + m0.e*m1.f + m0.f*m1.i;
        
        float g = m0.g*m1.a + m0.h*m1.d + m0.i*m1.g;
        float h = m0.g*m1.b + m0.h*m1.e + m0.i*m1.h;
        float i = m0.g*m1.c + m0.h*m1.f + m0.i*m1.i;
        
        r.a = a; r.b = b; r.c = c;
        r.d = d; r.e = e; r.f = f;
        r.g = g; r.h = h; r.i = i;
    }
    
    /**
     * Multiplies the given matrix by the given vector.
     * 
     * @param m The matrix to multiply.
     * @param v The vector to multiply by.
     * @param r The matrix in which to store the result.
     */
    public static final void mult(Mat3 m, Vec3 v, Mat3 r)
    {
        r.a = m.a*v.x; r.b = m.b*v.y; r.c = m.c*v.z;
        r.d = m.d*v.x; r.e = m.e*v.y; r.f = m.f*v.z;
        r.g = m.g*v.x; r.h = m.h*v.y; r.i = m.i*v.z;
    }
    
    /**
     * Multiplies the given matrix by the given vector.
     * 
     * @param m The matrix to multiply.
     * @param v The vector to multiply by.
     * @param r The matrix in which to store the result.
     */
    public static final void mult(Mat3 m, Vec2 v, Mat3 r)
    {
        r.a = m.a*v.x; r.b = m.b*v.y; r.c = m.c;
        r.d = m.d*v.x; r.e = m.e*v.y; r.f = m.f;
        r.g = m.g*v.x; r.h = m.h*v.y; r.i = m.i;
    }
    
    /**
     * Multiplies each entry in the given matrix by the given scalar.
     * 
     * @param m The matrix to multiply.
     * @param s The scalar to multiply by.
     * @param r The matrix in which to store the result.
     */
    public static final void mult(Mat3 m, float s, Mat3 r)
    {
        r.a = m.a*s; r.b = m.b*s; r.c = m.c*s;
        r.d = m.d*s; r.e = m.e*s; r.f = m.f*s;
        r.g = m.g*s; r.h = m.h*s; r.i = m.i*s;
    }
    
    /**
     * Divides the given matrix by the given scalar.
     * 
     * @param m The matrix to divide.
     * @param s The scalar to divide by.
     * @param r The matrix in which to store the result.
     */
    public static final void div(Mat3 m, float s, Mat3 r)
    {
        r.a = m.a/s; r.b = m.b/s; r.c = m.c/s;
        r.d = m.d/s; r.e = m.e/s; r.f = m.f/s;
        r.g = m.g/s; r.h = m.h/s; r.i = m.i/s;
    }
    
    /**
     * Sets {@code r} to the transpose of {@code m}.
     * 
     * @param m The matrix to compute the transpose of.
     * @param r The matrix in which to store the result.
     */
    public static final void transpose(Mat3 m, Mat3 r)
    {
        float tb = m.b, tc = m.c, tf = m.f;
        r.a = m.a; r.b = m.d; r.c = m.g;
        r.d = tb;  r.e = m.e; r.f = m.h;
        r.g = tc;  r.h = tf;  r.i = m.i;
    }
    
    /**
     * Calculates the inverse of {@code m} and stores the result in {@code r}.
     * 
     * @param m The matrix to compute the inverse of.
     * @param r The matrix in which to store the result.
     * @throws com.samrj.devil.math.SingularMatrixException If {@code m} is
     *         a singular matrix. (Its determinant is zero.)
     */
    public static final void invert(Mat3 m, Mat3 r)
    {
        float a = m.e*m.i - m.f*m.h;
        float d = m.f*m.g - m.d*m.i;
        float g = m.d*m.h - m.e*m.g;
        
        float det = m.a*a + m.b*d + m.c*g;
        if (det == 0.0f) throw new SingularMatrixException();
        
        float b = m.c*m.h - m.b*m.i, c = m.b*m.f - m.c*m.e;
        float e = m.a*m.i - m.c*m.g, f = m.c*m.d - m.a*m.f;
        float h = m.g*m.b - m.a*m.h, i = m.a*m.e - m.b*m.d;
        
        r.a = a/det; r.b = b/det; r.c = c/det;
        r.d = d/det; r.e = e/det; r.f = f/det;
        r.g = g/det; r.h = h/det; r.i = i/det;
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Static factory methods">
    /**
     * Returns a new 3x3 identity matrix.
     * 
     * @return A new 3x3 identity matrix.
     */
    public static final Mat3 identity()
    {
        return scaling(1.0f);
    }
    
    /**
     * Creates a new symmetric orthographic projection matrix with the given
     * dimensions.
     * 
     * @param width The half-width of the prism.
     * @param height The half-height of the prism.
     * @return A new matrix containing the result.
     */
    public static final Mat3 orthographic(float width, float height)
    {
        Mat3 m = new Mat3();
        orthographic(width, height, m);
        return m;
    }
    
    /**
     * Creates a new orthographic projection matrix with the given bounds.
     * 
     * @param left The left bound of the prism.
     * @param right The right bound of the prism.
     * @param bottom The lower bound of the prism.
     * @param top The upper bound of the prism.
     * @return A new matrix containing the result.
     */
    public static final Mat3 orthographic(float left, float right, float bottom, float top)
    {
        Mat3 m = new Mat3();
        orthographic(left, right, bottom, top, m);
        return m;
    }
    
    /**
     * Returns a new component-wise scaling matrix using the given vector.
     * 
     * @param v The vector to scale by.
     * @return A new scaling matrix.
     */
    public static final Mat3 scaling(Vec3 v)
    {
        Mat3 m = new Mat3();
        m.a = v.x;
        m.e = v.y;
        m.i = v.z;
        return m;
    }
    
    /**
     * Returns a new component-wise scaling matrix using the given vector.
     * 
     * @param v The vector to scale by.
     * @return A new scaling matrix.
     */
    public static final Mat3 scaling(Vec2 v)
    {
        Mat3 m = new Mat3();
        m.a = v.x;
        m.e = v.y;
        return m;
    }
    
    /**
     * Returns a new scaling matrix, where {@code s} is the scaling factor.
     * 
     * @param s The scaling factor.
     * @return A new scaling matrix.
     */
    public static final Mat3 scaling(float s)
    {
        Mat3 m = new Mat3();
        m.a = s;
        m.e = s;
        m.i = s;
        return m;
    }
    
    /**
     * Returns a new rotation matrix using the given {@code axis} of rotation,
     * and {@code ang} as the angle. The axis must be normalized.
     * 
     * @param axis The axis around which to rotate.
     * @param angle The angle to rotate by.
     * @return A new rotation matrix.
     */
    public static final Mat3 rotation(Vec3 axis, float angle)
    {
        Mat3 m = new Mat3();
        rotation(axis, angle, m);
        return m;
    }
    
    /**
     * Returns a new rotation matrix representing the given quaternion.
     * 
     * @param q The quaternion to represent as a matrix.
     * @return A new rotation matrix.
     */
    public static final Mat3 rotation(Quat q)
    {
        Mat3 m = new Mat3();
        rotation(q, m);
        return m;
    }
    
    /**
     * Returns a new translation matrix using the given vector.
     * 
     * @param v The vector to translate by.
     * @return A new translation matrix.
     */
    public static final Mat3 translation(Vec2 v)
    {
        Mat3 m = identity();
        m.c = v.x;
        m.f = v.y;
        return m;
    }
    
    /**
     * Multiplies {@code m0} by {@code m1} and returns the result as a new matrix.
     * 
     * @param m0 The left-hand matrix to multiply.
     * @param m1 The right-hand matrix to multiply by.
     * @return A new matrix containing the result.
     */
    public static final Mat3 mult(Mat3 m0, Mat3 m1)
    {
        Mat3 result = new Mat3();
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
    public static final Mat3 mult(Mat3 m, Vec3 v)
    {
        Mat3 result = new Mat3();
        mult(m, v, result);
        return result;
    }
    
    /**
     * Multiplies {@code m} by {@code v} and returns the result as a new matrix.
     * 
     * @param m The matrix to multiply.
     * @param v The vector to multiply by.
     * @return A new matrix containing the result.
     */
    public static final Mat3 mult(Mat3 m, Vec2 v)
    {
        Mat3 result = new Mat3();
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
    public static final Mat3 mult(Mat3 m, float s)
    {
        Mat3 result = new Mat3();
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
    public static final Mat3 div(Mat3 m, float s)
    {
        Mat3 result = new Mat3();
        div(m, s, result);
        return result;
    }
    
    /**
     * Returns the transpose of {@code m} as a new matrix.
     * 
     * @param m The matrix to compute the transpose of.
     * @return A new matrix containing the result.
     */
    public static final Mat3 transpose(Mat3 m)
    {
        Mat3 result = new Mat3();
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
    public static final Mat3 invert(Mat3 m)
    {
        Mat3 result = new Mat3();
        invert(m, result);
        return result;
    }
    // </editor-fold>
    
    public float a, b, c,
                 d, e, f,
                 g, h, i;
    
    // <editor-fold defaultstate="collapsed" desc="Constructors">
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
     * Expands and copies the given 2x2 matrix.
     * 
     * @param mat The matrix to copy.
     */
    public Mat3(Mat2 mat)
    {
        a = mat.a; b = mat.b;
        d = mat.c; e = mat.d;
        i = 1.0f;
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
    
    /**
     * Contracts and copies the given 4x4 matrix.
     * 
     * @param mat The matrix to copy.
     */
    public Mat3(Mat4 mat)
    {
        a = mat.a; b = mat.b; c = mat.c;
        d = mat.e; e = mat.f; f = mat.g;
        g = mat.i; h = mat.j; i = mat.k;
    }
    
    /**
     * Loads a new matrix from the given input stream.
     * 
     * @param in The input stream to read from.
     * @throws IOException If an io error occurred.
     */
    public Mat3(DataInputStream in) throws IOException
    {
        Mat3.this.read(in);
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
                case 2: return c;
                default: throw new ArrayIndexOutOfBoundsException();
            }
            case 1: switch (column)
            {
                case 0: return d;
                case 1: return e;
                case 2: return f;
                default: throw new ArrayIndexOutOfBoundsException();
            }
            case 2: switch (column)
            {
                case 0: return g;
                case 1: return h;
                case 2: return i;
                default: throw new ArrayIndexOutOfBoundsException();
            }
            default: throw new ArrayIndexOutOfBoundsException();
        }
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Instance mutator methods">
    /**
     * Sets this to the given matrix, expanded.
     * 
     * @param mat The matrix to set this to.
     * @return This matrix.
     */
    public Mat3 set(Mat2 mat)
    {
        copy(mat, this);
        return this;
    }
    
    /**
     * Sets this to the given matrix.
     * 
     * @param mat The matrix to set this to.
     * @return This matrix.
     */
    public Mat3 set(Mat3 mat)
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
    public Mat3 set(Mat4 mat)
    {
        copy(mat, this);
        return this;
    }
    
    /**
     * Sets the entries of this matrix.
     * 
     * @return This matrix.
     */
    public Mat3 set(float a, float b, float c,
                    float d, float e, float f,
                    float g, float h, float i)
    {
        this.a = a; this.b = b; this.c = c;
        this.d = d; this.e = e; this.f = f;
        this.g = g; this.h = h; this.i = i;
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
    public Mat3 setEntry(int row, int column, float v)
    {
        switch (row)
        {
            case 0: switch (column)
            {
                case 0: a = v; return this;
                case 1: b = v; return this;
                case 2: c = v; return this;
                default: throw new ArrayIndexOutOfBoundsException();
            }
            case 1: switch (column)
            {
                case 0: d = v; return this;
                case 1: e = v; return this;
                case 2: f = v; return this;
                default: throw new ArrayIndexOutOfBoundsException();
            }
            case 2: switch (column)
            {
                case 0: g = v; return this;
                case 1: h = v; return this;
                case 2: i = v; return this;
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
    public Mat3 setRow(Vec3 v, int row)
    {
        switch (row)
        {
            case 0: a = v.x;
                    b = v.y;
                    c = v.z; return this;
            case 1: d = v.x;
                    e = v.y;
                    f = v.z; return this;
            case 2: g = v.x;
                    h = v.y;
                    i = v.z; return this;
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
    public Mat3 setColumn(Vec3 v, int column)
    {
        switch (column)
        {
            case 0: a = v.x;
                    d = v.y;
                    g = v.z; return this;
            case 1: b = v.x;
                    e = v.y;
                    h = v.z; return this;
            case 2: c = v.x;
                    f = v.y;
                    i = v.z; return this;
            default: throw new ArrayIndexOutOfBoundsException();
        }
    }
    
    /**
     * Sets this to the identity matrix.
     * 
     * @return This matrix.
     */
    public Mat3 setZero()
    {
        zero(this);
        return this;
    }
    
    /**
     * Sets this to the identity matrix.
     * 
     * @return This matrix.
     */
    public Mat3 setIdentity()
    {
        identity(this);
        return this;
    }
    
    /**
     * Sets this to a symmetric orthographic projection matrix with the given
     * dimensions.
     * 
     * @param width The half-width of the prism.
     * @param height The half-height of the prism.
     * @return This matrix.
     */
    public Mat3 setOrthographic(float width, float height)
    {
        orthographic(width, height, this);
        return this;
    }
    
    /**
     * Sets this to a orthographic projection matrix with the given bounds.
     * 
     * @param left The left bound of the prism.
     * @param right The right bound of the prism.
     * @param bottom The lower bound of the prism.
     * @param top The upper bound of the prism.
     * @return This matrix.
     */
    public Mat3 setOrthographic(float left, float right, float bottom, float top)
    {
        orthographic(left, right, bottom, top, this);
        return this;
    }
    
    /**
     * Sets this to the component-wise scaling matrix by the given vector.
     * 
     * @param vec The vector to scale by.
     * @return This matrix.
     */
    public Mat3 setScaling(Vec3 vec)
    {
        scaling(vec, this);
        return this;
    }
    
    /**
     * Sets this to the component-wise scaling matrix by the given vector.
     * 
     * @param vec The vector to scale by.
     * @return This matrix.
     */
    public Mat3 setScaling(Vec2 vec)
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
    public Mat3 setScaling(float sca)
    {
        scaling(sca, this);
        return this;
    }
    
    /**
     * Sets this to the rotation matrix by the given angle, around the given
     * axis. The axis must be normalized.
     * 
     * @param axis The axis to rotate around.
     * @param angle The angle to rotate by.
     * @return This matrix.
     */
    public Mat3 setRotation(Vec3 axis, float angle)
    {
        rotation(axis, angle, this);
        return this;
    }
    
    /**
     * Sets this to a rotation matrix representation of the given quaternion.
     * 
     * @return This matrix.
     */
    public Mat3 setRotation(Quat quat)
    {
        rotation(quat, this);
        return this;
    }
    
    /**
     * Sets this to the translation matrix by the given vector.
     * 
     * @param vec The vector to translate by.
     * @return This matrix.
     */
    public Mat3 setTranslation(Vec2 vec)
    {
        translation(vec, this);
        return this;
    }
    
    /**
     * Rotates this matrix by the given angle, around the given axis. Assumes
     * that the given axis is normalized.
     * 
     * @param axis The axis around which to rotate.
     * @param angle The angle to rotate by.
     * @return This matrix.
     */
    public Mat3 rotate(Vec3 axis, float angle)
    {
        rotate(this, axis, angle, this);
        return this;
    }
    
    /**
     * Rotates this matrix by the given quaternion.
     * 
     * @param quat The quaternion to rotate by.
     * @return This matrix.
     */
    public Mat3 rotate(Quat quat)
    {
        rotate(this, quat, this);
        return this;
    }
    
    /**
     * Translates this matrix by the given vector.
     * 
     * @param vec The vector to translate by.
     * @return This matrix.
     */
    public Mat3 translate(Vec2 vec)
    {
        c += a*vec.x + b*vec.y;
        f += d*vec.x + e*vec.y;
        i += g*vec.x + h*vec.y;
        return this;
    }
    
    /**
     * Multiplies this matrix by the given matrix.
     * 
     * @param mat The right-hand matrix to multiply by.
     * @return This matrix.
     */
    public Mat3 mult(Mat3 mat)
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
    public Mat3 mult(Vec3 vec)
    {
        mult(this, vec, this);
        return this;
    }
    
    /**
     * Multiplies this matrix by the given vector.
     * 
     * @param vec The matrix to multiply by.
     * @return This matrix.
     */
    public Mat3 mult(Vec2 vec)
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
    public Mat3 mult(float sca)
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
    public Mat3 div(float sca)
    {
        div(this, sca, this);
        return this;
    }
    
    /**
     * Transposes this matrix.
     * 
     * @return This matrix.
     */
    public Mat3 transpose()
    {
        transpose(this, this); //Could be optimized.
        return this;
    }
    
    /**
     * Inverts this matrix.
     * 
     * @throws com.samrj.devil.math.SingularMatrixException If this matrix is
     *         singular. (Its determinant is zero.)
     * @return This matrix.
     */
    public Mat3 invert()
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
        a = buffer.getFloat(); d = buffer.getFloat(); g = buffer.getFloat();
        b = buffer.getFloat(); e = buffer.getFloat(); h = buffer.getFloat();
        c = buffer.getFloat(); f = buffer.getFloat(); i = buffer.getFloat();
    }

    @Override
    public void write(ByteBuffer buffer)
    {
        buffer.putFloat(a); buffer.putFloat(d); buffer.putFloat(g);
        buffer.putFloat(b); buffer.putFloat(e); buffer.putFloat(h);
        buffer.putFloat(c); buffer.putFloat(f); buffer.putFloat(i);
    }
    
    @Override
    public int bufferSize()
    {
        return 9*4;
    }
    
    /**
     * Written to/read from stream in row-major format.
     */
    @Override
    public void read(DataInputStream in) throws IOException
    {
        a = in.readFloat(); b = in.readFloat(); c = in.readFloat();
        d = in.readFloat(); e = in.readFloat(); f = in.readFloat();
        g = in.readFloat(); h = in.readFloat(); i = in.readFloat();
    }

    @Override
    public void write(DataOutputStream out) throws IOException
    {
        out.writeFloat(a); out.writeFloat(b); out.writeFloat(c);
        out.writeFloat(d); out.writeFloat(e); out.writeFloat(f);
        out.writeFloat(g); out.writeFloat(h); out.writeFloat(i);
    }
    
    @Override
    public String toString()
    {
        return "[" + a + ", " + b + ", " + c + "]\n" +
               "[" + d + ", " + e + ", " + f + "]\n" +
               "[" + g + ", " + h + ", " + i + "]";
    }
    // </editor-fold>
}
