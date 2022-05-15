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
 * 4x4 matrix class.
 * 
 * @author Samuel Johnson (SmashMaster)
 */
public class Mat4 implements FloatBufferable, DataStreamable<Mat4>
{
    private static final float SQRT_2 = (float)Math.sqrt(2.0);
    
    /**
     * Returns the determinant of the given matrix.
     * 
     * @param x The matrix to calculate the determinant of.
     * @return The determinant of the given matrix.
     */
    public static final float determinant(Mat4 x)
    {
        return x.a*(x.f*x.k*x.p + x.g*x.l*x.n + x.h*x.j*x.o - x.f*x.l*x.o - x.g*x.j*x.p - x.h*x.k*x.n) +
               x.b*(x.e*x.l*x.o + x.g*x.i*x.p + x.h*x.k*x.m - x.e*x.k*x.p - x.g*x.l*x.m - x.h*x.i*x.o) +
               x.c*(x.e*x.j*x.p + x.f*x.l*x.m + x.h*x.i*x.n - x.e*x.l*x.n - x.f*x.i*x.p - x.h*x.j*x.m) +
               x.d*(x.e*x.k*x.n + x.f*x.i*x.o + x.g*x.j*x.m - x.e*x.j*x.o - x.f*x.k*x.m - x.g*x.i*x.n);
    }
    
    // <editor-fold defaultstate="collapsed" desc="Static mutator methods">
    /**
     * Expands and copies the source matrix into the target matrix. 
     * 
     * @param s The matrix to copy from.
     * @param r The matrix to copy into.
     */
    public static final void copy(Mat2 s, Mat4 r)
    {
        r.a = s.a; r.b = s.b; r.c = 0.0f; r.d = 0.0f;
        r.e = s.c; r.f = s.d; r.g = 0.0f; r.h = 0.0f;
        r.i = 0.0f; r.j = 0.0f; r.k = 1.0f; r.l = 0.0f;
        r.m = 0.0f; r.n = 0.0f; r.o = 0.0f; r.p = 1.0f;
    }
    
    /**
     * Expands and copies the source matrix into the target matrix. 
     * 
     * @param s The matrix to copy from.
     * @param r The matrix to copy into.
     */
    public static final void copy(Mat3 s, Mat4 r)
    {
        r.a = s.a; r.b = s.b; r.c = s.c; r.d = 0.0f;
        r.e = s.d; r.f = s.e; r.g = s.f; r.h = 0.0f;
        r.i = s.g; r.j = s.h; r.k = s.i; r.l = 0.0f;
        r.m = 0.0f; r.n = 0.0f; r.o = 0.0f; r.p = 1.0f;
    }
    
    /**
     * Copies the source matrix into the target matrix. 
     * 
     * @param s The matrix to copy from.
     * @param r The matrix to copy into.
     */
    public static final void copy(Mat4 s, Mat4 r)
    {
        r.a = s.a; r.b = s.b; r.c = s.c; r.d = s.d;
        r.e = s.e; r.f = s.f; r.g = s.g; r.h = s.h;
        r.i = s.i; r.j = s.j; r.k = s.k; r.l = s.l;
        r.m = s.m; r.n = s.n; r.o = s.o; r.p = s.p;
    }
    
    /**
     * Sets the given matrix to the transformation matrix equal to the given
     * transform.
     * 
     * @param t The transform to convert.
     * @param r The matrix in which to store the result.
     */
    public static final void transform(Transform t, Mat4 r)
    {
        translation(t.pos, r);
        rotate(r, t.rot, r);
        mult(r, t.sca, r);
    }
    
    /**
     * Sets each entry of the given matrix to zero.
     * 
     * @param r The matrix to set to zero.
     */
    public static final void zero(Mat4 r)
    {
        r.a = 0.0f; r.b = 0.0f; r.c = 0.0f; r.d = 0.0f;
        r.e = 0.0f; r.f = 0.0f; r.g = 0.0f; r.h = 0.0f;
        r.i = 0.0f; r.j = 0.0f; r.k = 0.0f; r.l = 0.0f;
        r.m = 0.0f; r.n = 0.0f; r.o = 0.0f; r.p = 0.0f;
    }
    
    /**
     * Sets the given matrix to the identity matrix.
     * 
     * @param r The matrix to set to the identity matrix.
     */
    public static final void identity(Mat4 r)
    {
        scaling(1.0f, r);
    }
    
    /**
     * Sets the given matrix to a symmetric frustum projection matrix with the
     * given dimensions. The coordinate system of the frustum is right-handed,
     * with +Z being backwards--towards the camera. +X is right and +Y is up.
     * 
     * @param width The half-width of the near plane.
     * @param height The half-height of the near plane.
     * @param near The near clipping distance of the frustum.
     * @param far The far clipping distance of the frustum.
     * @param r The matrix in which to store the result.
     */
    public static final void frustum(float width, float height, float near, float far, Mat4 r)
    {
        float fmn = far - near;
        float n2 = near*2.0f;
        
        r.a = near/width; r.b = 0.0f; r.c = 0.0f; r.d = 0.0f;
        r.e = 0.0f; r.f = near/height; r.g = 0.0f; r.h = 0.0f;
        r.i = 0.0f; r.j = 0.0f; r.k = -(far + near)/fmn; r.l = (-far*n2)/fmn;
        r.m = 0.0f; r.n = 0.0f; r.o = -1.0f; r.p = 0.0f;
    }
    
    /**
     * Sets the given matrix to a frustum projection matrix with the given
     * bounds. The coordinate system of the frustum is right-handed, with +Z
     * being backwards--towards the camera. +X is right and +Y is up.
     * 
     * @param left The left bound of the near plane.
     * @param right The right bound of the near plane.
     * @param bottom The lower bound of the near plane.
     * @param top The upper bound of the near plane.
     * @param near The near clipping distance of the frustum.
     * @param far The far clipping distance of the frustum.
     * @param r The matrix in which to store the result.
     */
    public static final void frustum(float left, float right, float bottom, float top, float near, float far, Mat4 r)
    {
        float rml = right - left;
        float tmb = top - bottom;
        float fmn = far - near;
        float n2 = near*2.0f;
        
        r.a = n2/rml; r.b = 0.0f; r.c = (right + left)/rml; r.d = 0.0f;
        r.e = 0.0f; r.f = n2/tmb; r.g = (top + bottom)/tmb; r.h = 0.0f;
        r.i = 0.0f; r.j = 0.0f; r.k = -(far + near)/fmn; r.l = (-far*n2)/fmn;
        r.m = 0.0f; r.n = 0.0f; r.o = -1.0f; r.p = 0.0f;
    }
    
    /**
     * Sets the given matrix to a perspective projection matrix with the given
     * field of view, aspect ratio, and bounds. The coordinate system of the
     * frustum is right-handed, with +Z being backwards--towards the camera.
     * +X is right and +Y is up.
     * 
     * @param fov The full field of view of the frustum along its larger
     *            dimension, in radians.
     * @param aspect The aspect ratio of the frustum, height/width.
     * @param near The near clipping distance of the frustum.
     * @param far The far clipping distance of the frustum.
     * @param r The matrix in which to store the result.
     */
    public static final void perspective(float fov, float aspect, float near, float far, Mat4 r)
    {
        float greaterDimension = Math.abs(near)*(float)Math.tan(fov*0.5f);
        
        float w, h;
        if (aspect <= 1.0f) //Width is greater or equal to height.
        {
            w = greaterDimension;
            h = w*aspect;
        }
        else //Widgth is smaller than height.
        {
            h = greaterDimension;
            w = h/aspect;
        }
        
        frustum(w, h, near, far, r);
    }
    
    /**
     * Sets the given matrix to a symmetric orthographic projection matrix with
     * the given dimensions. The coordinate system is right-handed, with +Z
     * being backwards--towards the camera. +X is right and +Y is up.
     * 
     * @param width The half-width of the prism.
     * @param height The half-height of the prism.
     * @param near The near clipping distance of the prism.
     * @param far The far clipping distance of the prism.
     * @param r The matrix in which to store the result.
     */
    public static final void orthographic(float width, float height, float near, float far, Mat4 r)
    {
        float fmn = far - near;
        
        r.a = 1.0f/width; r.b = 0.0f; r.c = 0.0f; r.d = 0.0f;
        r.e = 0.0f; r.f = 1.0f/height; r.g = 0.0f; r.h = 0.0f;
        r.i = 0.0f; r.j = 0.0f; r.k = -2.0f/fmn; r.l = -(far + near)/fmn;
        r.m = 0.0f; r.n = 0.0f; r.o = 0.0f; r.p = 1.0f;
    }
    
    /**
     * Sets the given matrix to an orthographic projection matrix with the given
     * bounds. The coordinate system is right-handed, with +Z being backwards--
     * towards the camera. +X is right and +Y is up.
     * 
     * @param left The left bound of the prism.
     * @param right The right bound of the prism.
     * @param bottom The lower bound of the prism.
     * @param top The upper bound of the prism.
     * @param near The near clipping distance of the prism.
     * @param far The far clipping distance of the prism.
     * @param r The matrix in which to store the result.
     */
    public static final void orthographic(float left, float right, float bottom, float top, float near, float far, Mat4 r)
    {
        final float rml = right - left;
        final float tmb = top - bottom;
        final float fmn = far - near;
        
        r.a = 2.0f/rml; r.b = 0.0f; r.c = 0.0f; r.d = -(right + left)/rml;
        r.e = 0.0f; r.f = 2.0f/tmb; r.g = 0.0f; r.h = -(top + bottom)/tmb;
        r.i = 0.0f; r.j = 0.0f; r.k = -2.0f/fmn; r.l = -(far + near)/fmn;
        r.m = 0.0f; r.n = 0.0f; r.o = 0.0f; r.p = 1.0f;
    }
    
    /**
     * Sets the given matrix to a component-wise scaling matrix using the given
     * vector.
     * 
     * @param v The vector to scale by.
     * @param r The matrix in which to store the result.
     */
    public static final void scaling(Vec4 v, Mat4 r)
    {
        r.a = v.x; r.b = 0.0f; r.c = 0.0f; r.d = 0.0f;
        r.e = 0.0f; r.f = v.y; r.g = 0.0f; r.h = 0.0f;
        r.i = 0.0f; r.j = 0.0f; r.k = v.z; r.l = 0.0f;
        r.m = 0.0f; r.n = 0.0f; r.o = 0.0f; r.p = v.w;
    }
    
    /**
     * Sets the given matrix to a component-wise scaling matrix using the given
     * vector.
     * 
     * @param v The vector to scale by.
     * @param r The matrix in which to store the result.
     */
    public static final void scaling(Vec3 v, Mat4 r)
    {
        r.a = v.x; r.b = 0.0f; r.c = 0.0f; r.d = 0.0f;
        r.e = 0.0f; r.f = v.y; r.g = 0.0f; r.h = 0.0f;
        r.i = 0.0f; r.j = 0.0f; r.k = v.z; r.l = 0.0f;
        r.m = 0.0f; r.n = 0.0f; r.o = 0.0f; r.p = 1.0f;
    }
    
    /**
     * Sets the given matrix to a scaling matrix by the given scalar.
     * 
     * @param s The scalar to scale by.
     * @param r The matrix in which to store the result.
     */
    public static final void scaling(float s, Mat4 r)
    {
        r.a = s; r.b = 0.0f; r.c = 0.0f; r.d = 0.0f;
        r.e = 0.0f; r.f = s; r.g = 0.0f; r.h = 0.0f;
        r.i = 0.0f; r.j = 0.0f; r.k = s; r.l = 0.0f;
        r.m = 0.0f; r.n = 0.0f; r.o = 0.0f; r.p = s;
    }
    
    /**
     * Sets the given matrix to the rotation matrix, using the given {@code axis}
     * of rotation, and {@code ang} as the angle. The axis must be normalized.
     * 
     * @param axis The axis around which to rotate.
     * @param angle The angle to rotate by.
     * @param r The matrix in which to store the result.
     */
    public static final void rotation(Vec3 axis, float angle, Mat4 r)
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
        r.d = 0.0f;
        
        r.e = xyomcos + zsin;
        r.f = ysq + (1.0f - ysq)*cos;
        r.g = yzomcos - xsin;
        r.h = 0.0f;
        
        r.i = xzomcos - ysin;
        r.j = yzomcos + xsin;
        r.k = zsq + (1.0f - zsq)*cos;
        r.l = 0.0f;
        
        r.m = 0.0f; r.n = 0.0f; r.o = 0.0f; r.p = 1.0f;
    }
    
    /**
     * Sets the given matrix to a rotation matrix representation of the given
     * quaternion.
     * 
     * @param q The quaternion to represent as a matrix.
     * @param r The matrix in which to store the result.
     */
    public static final void rotation(Quat q, Mat4 r)
    {
        float q0 = SQRT_2*q.w, q1 = SQRT_2*q.x, q2 = SQRT_2*q.y, q3 = SQRT_2*q.z;

	float qda = q0*q1, qdb = q0*q2, qdc = q0*q3;
	float qaa = q1*q1, qab = q1*q2, qac = q1*q3;
	float qbb = q2*q2, qbc = q2*q3, qcc = q3*q3;
        
        r.a = 1.0f - qbb - qcc; r.b = -qdc + qab; r.c = qdb + qac; r.d = 0.0f;
        r.e = qdc + qab; r.f = 1.0f - qaa - qcc; r.g = -qda + qbc; r.h = 0.0f;
        r.i = -qdb + qac; r.j = qda + qbc; r.k = 1.0f - qaa - qbb; r.l = 0.0f;
        r.m = 0.0f; r.n = 0.0f; r.o = 0.0f; r.p = 1.0f;
    }
    
    /**
     * Sets the given matrix to the translation matrix by the given vector.
     * 
     * @param v The vector to translate by.
     * @param r The matrix in which to store the result.
     */
    public static final void translation(Vec3 v, Mat4 r)
    {
        r.a = 1.0f; r.b = 0.0f; r.c = 0.0f; r.d = v.x;
        r.e = 0.0f; r.f = 1.0f; r.g = 0.0f; r.h = v.y;
        r.i = 0.0f; r.j = 0.0f; r.k = 1.0f; r.l = v.z;
        r.m = 0.0f; r.n = 0.0f; r.o = 0.0f; r.p = 1.0f;
    }
    
    /**
     * Sets the given matrix to a reflection matrix about the given plane.
     * 
     * @param n The plane's normal vector.
     * @param d The plane constant.
     * @param r The matrix in which to store the result.
     */
    public static final void reflection(Vec3 n, float d, Mat4 r)
    {
        float ab = -2.0f*n.x*n.y;
        float ac = -2.0f*n.x*n.z;
        float bc = -2.0f*n.y*n.z;
        
        r.a = 1.0f - 2.0f*n.x*n.x; r.b = ab; r.c = ac; r.d = -2.0f*n.x*d;
        r.e = ab; r.f = 1.0f - 2.0f*n.y*n.y; r.g = bc; r.h = -2.0f*n.y*d;
        r.i = ac; r.j = bc; r.k = 1.0f - 2.0f*n.z*n.z; r.l = -2.0f*n.z*d;
        r.m = 0.0f; r.n = 0.0f; r.o = 0.0f; r.p = 1.0f;
    }
    
    /**
     * Rotates {@code m} about the given {@code axis} by the given angle
     * {@code ang} and stores the result in {@code r}. The axis must be
     * normalized.
     * 
     * @param x The matrix to rotate.
     * @param axis The axis to rotate around.
     * @param angle The angle to rotate by.
     * @param r The matrix in which to store the result.
     */
    public static final void rotate(Mat4 x, Vec3 axis, float angle, Mat4 r)
    {
        Mat4 temp = rotation(axis, angle); //Could probably be improved.
        mult(x, temp, r);
    }
    
    /**
     * Rotates {@code m} by the given quaternion and stores the result in {@code r}.
     * 
     * @param x The matrix to rotate.
     * @param q The quaternion to rotate by.
     * @param r The matrix in which to store the result.
     */
    public static final void rotate(Mat4 x, Quat q, Mat4 r)
    {
        Mat4 temp = rotation(q); //Could probably be improved, as above
        mult(x, temp, r);
    }
    
    /**
     * Translates {@code m} by the given vector {@code v}, and stores the result
     * in {@code r}.
     * 
     * @param x The matrix to translate.
     * @param v The vector to translate by.
     * @param r The matrix in which to store the result.
     */
    public static final void translate(Mat4 x, Vec3 v, Mat4 r)
    {
        r.a = x.a; r.b = x.b; r.c = x.c; r.d = x.a*v.x + x.b*v.y + x.c*v.z + x.d;
        r.e = x.e; r.f = x.f; r.g = x.g; r.h = x.e*v.x + x.f*v.y + x.g*v.z + x.h;
        r.i = x.i; r.j = x.j; r.k = x.k; r.l = x.i*v.x + x.j*v.y + x.k*v.z + x.l;
        r.m = x.m; r.n = x.n; r.o = x.o; r.p = x.m*v.x + x.n*v.y + x.o*v.z + x.p;
    }
    
    /**
     * Performs a matrix multiplication on {@code m0} and {@code m1}, and stores
     * the result in {@code r}. 
     * 
     * @param m0 The left-hand matrix to multiply.
     * @param m1 The right-hand matrix to multiply by.
     * @param r The matrix in which to store the result.
     */
    public static final void mult(Mat4 m0, Mat4 m1, Mat4 r)
    {
        float a = m0.a*m1.a + m0.b*m1.e + m0.c*m1.i + m0.d*m1.m;
        float b = m0.a*m1.b + m0.b*m1.f + m0.c*m1.j + m0.d*m1.n;
        float c = m0.a*m1.c + m0.b*m1.g + m0.c*m1.k + m0.d*m1.o;
        float d = m0.a*m1.d + m0.b*m1.h + m0.c*m1.l + m0.d*m1.p;
        
        float e = m0.e*m1.a + m0.f*m1.e + m0.g*m1.i + m0.h*m1.m;
        float f = m0.e*m1.b + m0.f*m1.f + m0.g*m1.j + m0.h*m1.n;
        float g = m0.e*m1.c + m0.f*m1.g + m0.g*m1.k + m0.h*m1.o;
        float h = m0.e*m1.d + m0.f*m1.h + m0.g*m1.l + m0.h*m1.p;
        
        float i = m0.i*m1.a + m0.j*m1.e + m0.k*m1.i + m0.l*m1.m;
        float j = m0.i*m1.b + m0.j*m1.f + m0.k*m1.j + m0.l*m1.n;
        float k = m0.i*m1.c + m0.j*m1.g + m0.k*m1.k + m0.l*m1.o;
        float l = m0.i*m1.d + m0.j*m1.h + m0.k*m1.l + m0.l*m1.p;
        
        float m = m0.m*m1.a + m0.n*m1.e + m0.o*m1.i + m0.p*m1.m;
        float n = m0.m*m1.b + m0.n*m1.f + m0.o*m1.j + m0.p*m1.n;
        float o = m0.m*m1.c + m0.n*m1.g + m0.o*m1.k + m0.p*m1.o;
        float p = m0.m*m1.d + m0.n*m1.h + m0.o*m1.l + m0.p*m1.p;
        
        r.a = a; r.b = b; r.c = c; r.d = d;
        r.e = e; r.f = f; r.g = g; r.h = h;
        r.i = i; r.j = j; r.k = k; r.l = l;
        r.m = m; r.n = n; r.o = o; r.p = p;
    }
    
    /**
     * Multiplies the given matrix by the given transform, and stores the result
     * in {@code r}.
     * 
     * @param m The left-hand matrix to multiply.
     * @param t The right-hand transform to multiply by.
     * @param r The matrix in which to store the result.
     */
    public static final void mult(Mat4 m, Transform t, Mat4 r)
    {
        translate(m, t.pos, r);
        rotate(r, t.rot, r);
        mult(r, t.sca, r);
    }
    
    /**
     * Multiplies the given matrix by the given vector.
     * 
     * @param x The matrix to multiply.
     * @param v The vector to multiply by.
     * @param r The matrix in which to store the result.
     */
    public static final void mult(Mat4 x, Vec4 v, Mat4 r)
    {
        r.a = x.a*v.x; r.b = x.b*v.y; r.c = x.c*v.z; r.d = x.d*v.w;
        r.e = x.e*v.x; r.f = x.f*v.y; r.g = x.g*v.z; r.h = x.h*v.w;
        r.i = x.i*v.x; r.j = x.j*v.y; r.k = x.k*v.z; r.l = x.l*v.w;
        r.m = x.m*v.x; r.n = x.n*v.y; r.o = x.o*v.z; r.p = x.p*v.w;
    }
    
    /**
     * Multiplies the given matrix by the given vector.
     * 
     * @param x The matrix to multiply.
     * @param v The vector to multiply by.
     * @param r The matrix in which to store the result.
     */
    public static final void mult(Mat4 x, Vec3 v, Mat4 r)
    {
        r.a = x.a*v.x; r.b = x.b*v.y; r.c = x.c*v.z; r.d = x.d;
        r.e = x.e*v.x; r.f = x.f*v.y; r.g = x.g*v.z; r.h = x.h;
        r.i = x.i*v.x; r.j = x.j*v.y; r.k = x.k*v.z; r.l = x.l;
        r.m = x.m*v.x; r.n = x.n*v.y; r.o = x.o*v.z; r.p = x.p;
    }
    
    /**
     * Multiplies each entry in the given matrix by the given scalar.
     * 
     * @param x The matrix to multiply.
     * @param s The scalar to multiply by.
     * @param r The matrix in which to store the result.
     */
    public static final void mult(Mat4 x, float s, Mat4 r)
    {
        r.a = x.a*s; r.b = x.b*s; r.c = x.c*s; r.d = x.d*s;
        r.e = x.e*s; r.f = x.f*s; r.g = x.g*s; r.h = x.h*s;
        r.i = x.i*s; r.j = x.j*s; r.k = x.k*s; r.l = x.l*s;
        r.m = x.m*s; r.n = x.n*s; r.o = x.o*s; r.p = x.p*s;
    }
    
    /**
     * Divides the given matrix by the given scalar.
     * 
     * @param x The matrix to divide.
     * @param s The scalar to divide by.
     * @param r The matrix in which to store the result.
     */
    public static final void div(Mat4 x, float s, Mat4 r)
    {
        r.a = x.a/s; r.b = x.b/s; r.c = x.c/s; r.d = x.d/s;
        r.e = x.e/s; r.f = x.f/s; r.g = x.g/s; r.h = x.h/s;
        r.i = x.i/s; r.j = x.j/s; r.k = x.k/s; r.l = x.l/s;
        r.m = x.m/s; r.n = x.n/s; r.o = x.o/s; r.p = x.p/s;
    }
    
    /**
     * Sets {@code r} to the transpose of {@code m}.
     * 
     * @param x The matrix to compute the transpose of.
     * @param r The matrix in which to store the result.
     */
    public static final void transpose(Mat4 x, Mat4 r)
    {
        float tb = x.b, tc = x.c, td = x.d;
        float tg = x.g, th = x.h;
        float tl = x.l;
        r.a = x.a; r.b = x.e; r.c = x.i; r.d = x.m;
        r.e = tb;  r.f = x.f; r.g = x.j; r.h = x.n;
        r.i = tc;  r.j = tg;  r.k = x.k; r.l = x.o;
        r.m = td;  r.n = th;  r.o = tl;  r.p = x.p;
    }
    
    /**
     * Calculates the inverse of {@code m} and stores the result in {@code r}.
     * 
     * @param x The matrix to compute the inverse of.
     * @param r The matrix in which to store the result.
     * @throws com.samrj.devil.math.SingularMatrixException If {@code m} is
     *         a singular matrix. (Its determinant is zero.)
     */
    public static final void invert(Mat4 x, Mat4 r)
    {
        float a = x.f*x.k*x.p + x.g*x.l*x.n + x.h*x.j*x.o - x.f*x.l*x.o - x.g*x.j*x.p - x.h*x.k*x.n;
        float e = x.e*x.l*x.o + x.g*x.i*x.p + x.h*x.k*x.m - x.e*x.k*x.p - x.g*x.l*x.m - x.h*x.i*x.o;
        float i = x.e*x.j*x.p + x.f*x.l*x.m + x.h*x.i*x.n - x.e*x.l*x.n - x.f*x.i*x.p - x.h*x.j*x.m;
        float m = x.e*x.k*x.n + x.f*x.i*x.o + x.g*x.j*x.m - x.e*x.j*x.o - x.f*x.k*x.m - x.g*x.i*x.n;
        
        float det = x.a*a + x.b*e + x.c*i + x.d*m;
        if (det == 0.0f) throw new SingularMatrixException();
        
        float b = x.b*x.l*x.o + x.c*x.j*x.p + x.d*x.k*x.n - x.b*x.k*x.p - x.c*x.l*x.n - x.d*x.j*x.o;
        float c = x.b*x.g*x.p + x.c*x.h*x.n + x.d*x.f*x.o - x.b*x.h*x.o - x.c*x.f*x.p - x.d*x.g*x.n;
        float d = x.b*x.h*x.k + x.c*x.f*x.l + x.d*x.g*x.j - x.b*x.g*x.l - x.c*x.h*x.j - x.d*x.f*x.k;
        float f = x.a*x.k*x.p + x.c*x.l*x.m + x.d*x.i*x.o - x.a*x.l*x.o - x.c*x.i*x.p - x.d*x.k*x.m;
        float g = x.a*x.h*x.o + x.c*x.e*x.p + x.d*x.g*x.m - x.a*x.g*x.p - x.c*x.h*x.m - x.d*x.e*x.o;
        float h = x.a*x.g*x.l + x.c*x.h*x.i + x.d*x.e*x.k - x.a*x.h*x.k - x.c*x.e*x.l - x.d*x.g*x.i;
        float j = x.a*x.l*x.n + x.b*x.i*x.p + x.d*x.j*x.m - x.a*x.j*x.p - x.b*x.l*x.m - x.d*x.i*x.n;
        float k = x.a*x.f*x.p + x.b*x.h*x.m + x.d*x.e*x.n - x.a*x.h*x.n - x.b*x.e*x.p - x.d*x.f*x.m;
        float l = x.a*x.h*x.j + x.b*x.e*x.l + x.d*x.f*x.i - x.a*x.f*x.l - x.b*x.h*x.i - x.d*x.e*x.j;
        float n = x.a*x.j*x.o + x.b*x.k*x.m + x.c*x.i*x.n - x.a*x.k*x.n - x.b*x.i*x.o - x.c*x.j*x.m;
        float o = x.a*x.g*x.n + x.b*x.e*x.o + x.c*x.f*x.m - x.a*x.f*x.o - x.b*x.g*x.m - x.c*x.e*x.n;
        float p = x.a*x.f*x.k + x.b*x.g*x.i + x.c*x.e*x.j - x.a*x.g*x.j - x.b*x.e*x.k - x.c*x.f*x.i;
        
        r.a = a; r.b = b; r.c = c; r.d = d;
        r.e = e; r.f = f; r.g = g; r.h = h;
        r.i = i; r.j = j; r.k = k; r.l = l;
        r.m = m; r.n = n; r.o = o; r.p = p;
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Static factory methods">
    /**
     * Returns a new 4x4 identity matrix.
     * 
     * @return A new 4x4 identity matrix.
     */
    public static final Mat4 identity()
    {
        return scaling(1.0f);
    }
    
    /**
     * Returns a new 4x4 transformation matrix equal to the given transform.
     * 
     * @param transform The transform to convert.
     * @return A new matrix containing the result.
     */
    public static final Mat4 transform(Transform transform)
    {
        Mat4 m = new Mat4();
        transform(transform, m);
        return m;
    }
    
    /**
     * Creates a new symmetric frustum projection matrix with the given
     * dimensions. The coordinate system of the frustum is right-handed, with
     * +Z being backwards--towards the camera. +X is right and +Y is up.
     * 
     * @param width The half-width of the near plane.
     * @param height The half-height of the near plane.
     * @param near The near clipping distance of the frustum.
     * @param far The far clipping distance of the frustum.
     * @return A new matrix containing the result.
     */
    public static final Mat4 frustum(float width, float height, float near, float far)
    {
        Mat4 m = new Mat4();
        frustum(width, height, near, far, m); //Could be optimized.
        return m;
    }
    
    /**
     * Creates a new frustum projection matrix with the given bounds. The
     * coordinate system of the frustum is right-handed, with +Z being
     * backwards--towards the camera. +X is right and +Y is up.
     * 
     * @param left The left bound of the near plane.
     * @param right The right bound of the near plane.
     * @param bottom The lower bound of the near plane.
     * @param top The upper bound of the near plane.
     * @param near The near clipping distance of the frustum.
     * @param far The far clipping distance of the frustum.
     * @return A new matrix containing the result.
     */
    public static final Mat4 frustum(float left, float right, float bottom, float top, float near, float far)
    {
        Mat4 m = new Mat4();
        frustum(left, right, bottom, top, near, far, m); //Could be optimized.
        return m;
    }
    
    /**
     * Creates a new perspective projection matrix with the given field of view,
     * aspect ratio, and bounds. The coordinate system of the frustum is right-
     * handed, with +Z being backwards--towards the camera. +X is right and +Y
     * is up.
     * 
     * @param fov The full field of view of the frustum along its larger
     *            dimension, in radians.
     * @param aspect The aspect ratio of the frustum, height/width.
     * @param near The near clipping distance of the frustum.
     * @param far The far clipping distance of the frustum.
     * @return A new matrix containing the result.
     */
    public static final Mat4 perspective(float fov, float aspect, float near, float far)
    {
        Mat4 m = new Mat4();
        perspective(fov, aspect, near, far, m); //Could be optimized.
        return m;
    }
    
    /**
     * Creates a new symmetric orthographic projection matrix with the given
     * dimensions. The coordinate system is right-handed, with +Z being
     * backwards--towards the camera. +X is right and +Y is up.
     * 
     * @param width The half-width of the prism.
     * @param height The half-height of the prism.
     * @param near The near clipping distance of the prism.
     * @param far The far clipping distance of the prism.
     * @return A new matrix containing the result.
     */
    public static final Mat4 orthographic(float width, float height, float near, float far)
    {
        Mat4 m = new Mat4();
        orthographic(width, height, near, far, m); //Could be optimized.
        return m;
    }
    
    /**
     * Creates a new orthographic projection matrix with the given bounds. The
     * coordinate system is right-handed, with +Z being backwards--towards the
     * camera. +X is right and +Y is up.
     * 
     * @param left The left bound of the prism.
     * @param right The right bound of the prism.
     * @param bottom The lower bound of the prism.
     * @param top The upper bound of the prism.
     * @param near The near clipping distance of the prism.
     * @param far The far clipping distance of the prism.
     * @return A new matrix containing the result.
     */
    public static final Mat4 orthographic(float left, float right, float bottom, float top, float near, float far)
    {
        Mat4 m = new Mat4();
        orthographic(left, right, bottom, top, near, far, m); //Could be optimized.
        return m;
    }
    
    /**
     * Returns a new component-wise scaling matrix using the given vector.
     * 
     * @param v The vector to scale by.
     * @return A new scaling matrix.
     */
    public static final Mat4 scaling(Vec4 v)
    {
        Mat4 m = new Mat4();
        m.a = v.x;
        m.f = v.y;
        m.k = v.z;
        m.p = v.w;
        return m;
    }
    
    /**
     * Returns a new component-wise scaling matrix using the given vector.
     * 
     * @param v The vector to scale by.
     * @return A new scaling matrix.
     */
    public static final Mat4 scaling(Vec3 v)
    {
        Mat4 m = new Mat4();
        m.a = v.x;
        m.f = v.y;
        m.k = v.z;
        m.p = 1.0f;
        return m;
    }
    
    /**
     * Returns a new scaling matrix, where {@code s} is the scaling factor.
     * 
     * @param s The scaling factor.
     * @return A new scaling matrix.
     */
    public static final Mat4 scaling(float s)
    {
        Mat4 m = new Mat4();
        m.a = s;
        m.f = s;
        m.k = s;
        m.p = s;
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
    public static final Mat4 rotation(Vec3 axis, float angle)
    {
        Mat4 m = new Mat4();
        rotation(axis, angle, m);
        return m;
    }
    
    /**
     * Returns a new rotation matrix representing the given quaternion.
     * 
     * @param q The quaternion to represent as a matrix.
     * @return A new rotation matrix.
     */
    public static final Mat4 rotation(Quat q)
    {
        Mat4 m = new Mat4();
        rotation(q, m);
        return m;
    }
    
    /**
     * Returns a new translation matrix using the given vector.
     * 
     * @param v The vector to translate by.
     * @return A new translation matrix.
     */
    public static final Mat4 translation(Vec3 v)
    {
        Mat4 m = identity();
        m.d = v.x;
        m.h = v.y;
        m.l = v.z;
        return m;
    }
    
    /**
     * Returns a new reflection matrix about the given plane.
     * @param normal The plane normal.
     * @param constant The plane constant.
     * @return A new reflection matrix.
     */
    public static final Mat4 reflection(Vec3 normal, float constant)
    {
        Mat4 m = new Mat4();
        reflection(normal, constant, m);
        return m;
    }
    
    /**
     * Multiplies {@code m0} by {@code m1} and returns the result as a new matrix.
     * 
     * @param m0 The left-hand matrix to multiply.
     * @param m1 The right-hand matrix to multiply by.
     * @return A new matrix containing the result.
     */
    public static final Mat4 mult(Mat4 m0, Mat4 m1)
    {
        Mat4 result = new Mat4();
        mult(m0, m1, result);
        return result;
    }
    
    /**
     * Multiplies {@code m} by {@code t} and returns the result as a new matrix.
     * 
     * @param m The left-hand matrix to multiply.
     * @param t The right-hand transform to multiply by.
     * @return A new matrix containing the result.
     */
    public static final Mat4 mult(Mat4 m, Transform t)
    {
        Mat4 result = new Mat4();
        mult(m, t, result);
        return result;
    }
    
    /**
     * Multiplies {@code m} by {@code v} and returns the result as a new matrix.
     * 
     * @param m The matrix to multiply.
     * @param v The vector to multiply by.
     * @return A new matrix containing the result.
     */
    public static final Mat4 mult(Mat4 m, Vec4 v)
    {
        Mat4 result = new Mat4();
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
    public static final Mat4 mult(Mat4 m, Vec3 v)
    {
        Mat4 result = new Mat4();
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
    public static final Mat4 mult(Mat4 m, float s)
    {
        Mat4 result = new Mat4();
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
    public static final Mat4 div(Mat4 m, float s)
    {
        Mat4 result = new Mat4();
        div(m, s, result);
        return result;
    }
    
    /**
     * Returns the transpose of {@code m} as a new matrix.
     * 
     * @param m The matrix to compute the transpose of.
     * @return A new matrix containing the result.
     */
    public static final Mat4 transpose(Mat4 m)
    {
        Mat4 result = new Mat4();
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
    public static final Mat4 invert(Mat4 m)
    {
        Mat4 result = new Mat4();
        invert(m, result);
        return result;
    }
    // </editor-fold>
    
    public float a, b, c, d,
                 e, f, g, h,
                 i, j, k, l,
                 m, n, o, p;
    
    // <editor-fold defaultstate="collapsed" desc="Constructors">
    /**
     * Creates a new 4x4 zero matrix, NOT an identity matrix. Use identity() to
     * create an identity matrix.
     */
    public Mat4()
    {
    }
    
    /**
     * Creates a new 4x4 matrix with the given values.
     */
    public Mat4(float a, float b, float c, float d,
                float e, float f, float g, float h,
                float i, float j, float k, float l,
                float m, float n, float o, float p)
    {
        this.a = a; this.b = b; this.c = c; this.d = d;
        this.e = e; this.f = f; this.g = g; this.h = h;
        this.i = i; this.j = j; this.k = k; this.l = l;
        this.m = m; this.n = n; this.o = o; this.p = p;
    }
    
    /**
     * Expands and copies the given 2x2 matrix.
     * 
     * @param x The matrix to copy.
     */
    public Mat4(Mat2 x)
    {
        a = x.a; b = x.b;
        e = x.c; f = x.d;
        k = 1.0f; p = 1.0f;
    }
    
    /**
     * Expands and copies the given 3x3 matrix.
     * 
     * @param x The matrix to copy.
     */
    public Mat4(Mat3 x)
    {
        a = x.a; b = x.b; c = x.c;
        e = x.d; f = x.e; g = x.f;
        i = x.g; j = x.h; k = x.i;
        p = 1.0f;
    }
    
    /**
     * Copies the given 4x4 matrix.
     * 
     * @param x The matrix to copy.
     */
    public Mat4(Mat4 x)
    {
        a = x.a; b = x.b; c = x.c; d = x.d;
        e = x.e; f = x.f; g = x.g; h = x.h;
        i = x.i; j = x.j; k = x.k; l = x.l;
        m = x.m; n = x.n; o = x.o; p = x.p;
    }

    /**
     * Loads a new matrix from the given buffer.
     *
     * @param buffer The buffer to read from.
     */
    public Mat4(ByteBuffer buffer)
    {
        Mat4.this.read(buffer);
    }

    /**
     * Loads a new matrix from the given input stream.
     * 
     * @param in The input stream to read from.
     * @throws IOException If an io error occurred.
     */
    public Mat4(DataInputStream in) throws IOException
    {
        Mat4.this.read(in);
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
                case 3: return d;
                default: throw new ArrayIndexOutOfBoundsException();
            }
            case 1: switch (column)
            {
                case 0: return e;
                case 1: return f;
                case 2: return g;
                case 3: return h;
                default: throw new ArrayIndexOutOfBoundsException();
            }
            case 2: switch (column)
            {
                case 0: return i;
                case 1: return j;
                case 2: return k;
                case 3: return l;
                default: throw new ArrayIndexOutOfBoundsException();
            }
            case 3: switch (column)
            {
                case 0: return m;
                case 1: return n;
                case 2: return o;
                case 3: return p;
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
    public Mat4 set(Mat2 mat)
    {
        copy(mat, this);
        return this;
    }
    
    /**
     * Sets this to the given matrix, expanded.
     * 
     * @param mat The matrix to set this to.
     * @return This matrix.
     */
    public Mat4 set(Mat3 mat)
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
    public Mat4 set(Mat4 mat)
    {
        copy(mat, this);
        return this;
    }
    
    /**
     * Sets the entries of this matrix.
     * 
     * @return This matrix.
     */
    public Mat4 set(float a, float b, float c, float d,
                    float e, float f, float g, float h,
                    float i, float j, float k, float l,
                    float m, float n, float o, float p)
    {
        this.a = a; this.b = b; this.c = c; this.d = d;
        this.e = e; this.f = f; this.g = g; this.h = h;
        this.i = i; this.j = j; this.k = k; this.l = l;
        this.m = m; this.n = n; this.o = o; this.p = p;
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
    public Mat4 setEntry(int row, int column, float v)
    {
        switch (row)
        {
            case 0: switch (column)
            {
                case 0: a = v; return this;
                case 1: b = v; return this;
                case 2: c = v; return this;
                case 3: d = v; return this;
                default: throw new ArrayIndexOutOfBoundsException();
            }
            case 1: switch (column)
            {
                case 0: e = v; return this;
                case 1: f = v; return this;
                case 2: g = v; return this;
                case 3: h = v; return this;
                default: throw new ArrayIndexOutOfBoundsException();
            }
            case 2: switch (column)
            {
                case 0: i = v; return this;
                case 1: j = v; return this;
                case 2: k = v; return this;
                case 3: l = v; return this;
                default: throw new ArrayIndexOutOfBoundsException();
            }
            case 3: switch (column)
            {
                case 0: m = v; return this;
                case 1: n = v; return this;
                case 2: o = v; return this;
                case 3: p = v; return this;
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
    public Mat4 setRow(Vec4 v, int row)
    {
        switch (row)
        {
            case 0: a = v.x;
                    b = v.y;
                    c = v.z;
                    d = v.w; return this;
            case 1: e = v.x;
                    f = v.y;
                    g = v.z;
                    h = v.w; return this;
            case 2: i = v.x;
                    j = v.y;
                    k = v.z;
                    l = v.w; return this;
            case 3: m = v.x;
                    n = v.y;
                    o = v.z;
                    p = v.w; return this;
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
    public Mat4 setColumn(Vec4 v, int column)
    {
        switch (column)
        {
            case 0: a = v.x;
                    e = v.y;
                    i = v.z;
                    m = v.w; return this;
            case 1: b = v.x;
                    f = v.y;
                    j = v.z;
                    n = v.w; return this;
            case 2: c = v.x;
                    g = v.y;
                    k = v.z;
                    o = v.w; return this;
            case 3: d = v.x;
                    h = v.y;
                    l = v.z;
                    p = v.w; return this;
            default: throw new ArrayIndexOutOfBoundsException();
        }
    }
    
    /**
     * Sets this to the identity matrix.
     * 
     * @return This matrix.
     */
    public Mat4 setZero()
    {
        zero(this);
        return this;
    }
    
    /**
     * Sets this to the identity matrix.
     * 
     * @return This matrix.
     */
    public Mat4 setIdentity()
    {
        identity(this);
        return this;
    }
    
    /**
     * Sets this to the transformation matrix equal to the given transform.
     * 
     * @param transform The transform to convert.
     * @return This matrix. 
     */
    public Mat4 setTransform(Transform transform)
    {
        transform(transform, this);
        return this;
    }
    
    /**
     * Sets this to a symmetric frustum projection matrix with the given
     * dimensions. The coordinate system of the frustum is right-handed, with
     * +Z being backwards--towards the camera. +X is right and +Y is up.
     * 
     * @param width The half-width of the near plane.
     * @param height The half-height of the near plane.
     * @param near The near clipping distance of the frustum.
     * @param far The far clipping distance of the frustum.
     * @return This matrix.
     */
    public Mat4 setFrustum(float width, float height, float near, float far)
    {
        frustum(width, height, near, far, this);
        return this;
    }
    
    /**
     * Sets this to a frustum projection matrix with the given bounds. The
     * coordinate system of the frustum is right-handed, with +Z being
     * backwards--towards the camera. +X is right and +Y is up.
     * 
     * @param left The left bound of the near plane.
     * @param right The right bound of the near plane.
     * @param bottom The lower bound of the near plane.
     * @param top The upper bound of the near plane.
     * @param near The near clipping distance of the frustum.
     * @param far The far clipping distance of the frustum.
     * @return This matrix.
     */
    public Mat4 setFrustum(float left, float right, float bottom, float top, float near, float far)
    {
        frustum(left, right, bottom, top, near, far, this);
        return this;
    }
    
    /**
     * Sets this to a perspective projection matrix with the given field of
     * view, aspect ratio, and bounds. The coordinate system of the frustum is
     * right- handed, with +Z being backwards--towards the camera. +X is right
     * and +Y is up.
     * 
     * @param fov The full field of view of the frustum along its larger
     *            dimension, in radians.
     * @param aspect The aspect ratio of the frustum, height/width.
     * @param near The near clipping distance of the frustum.
     * @param far The far clipping distance of the frustum.
     * @return This matrix.
     */
    public Mat4 setPerspective(float fov, float aspect, float near, float far)
    {
        perspective(fov, aspect, near, far, this);
        return this;
    }
    
    /**
     * Sets this to a symmetric orthographic projection matrix with the given
     * dimensions. The coordinate system is right-handed, with +Z being
     * backwards--towards the camera. +X is right and +Y is up.
     * 
     * @param width The half-width of the prism.
     * @param height The half-height of the prism.
     * @param near The near clipping distance of the prism.
     * @param far The far clipping distance of the prism.
     * @return This matrix.
     */
    public Mat4 setOrthographic(float width, float height, float near, float far)
    {
        orthographic(width, height, near, far, this);
        return this;
    }
    
    /**
     * Sets this to a orthographic projection matrix with the given bounds. The
     * coordinate system is right-handed, with +Z being backwards--towards the
     * camera. +X is right and +Y is up.
     * 
     * @param left The left bound of the prism.
     * @param right The right bound of the prism.
     * @param bottom The lower bound of the prism.
     * @param top The upper bound of the prism.
     * @param near The near clipping distance of the prism.
     * @param far The far clipping distance of the prism.
     * @return This matrix.
     */
    public Mat4 setOrthographic(float left, float right, float bottom, float top, float near, float far)
    {
        orthographic(left, right, bottom, top, near, far, this);
        return this;
    }
    
    /**
     * Sets this to the component-wise scaling matrix by the given vector.
     * 
     * @param vec The vector to scale by.
     * @return This matrix.
     */
    public Mat4 setScaling(Vec4 vec)
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
    public Mat4 setScaling(Vec3 vec)
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
    public Mat4 setScaling(float sca)
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
    public Mat4 setRotation(Vec3 axis, float angle)
    {
        rotation(axis, angle, this);
        return this;
    }
    
    /**
     * Sets this to a rotation matrix representation of the given quaternion.
     * 
     * @return This matrix.
     */
    public Mat4 setRotation(Quat quat)
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
    public Mat4 setTranslation(Vec3 vec)
    {
        translation(vec, this);
        return this;
    }
    
    /**
     * Sets this to the reflection matrix about the given plane.
     * 
     * @param normal The plane normal.
     * @param constant The plane constant.
     * @return This matrix.
     */
    public Mat4 setReflection(Vec3 normal, float constant)
    {
        reflection(normal, constant, this);
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
    public Mat4 rotate(Vec3 axis, float angle)
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
    public Mat4 rotate(Quat quat)
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
    public Mat4 translate(Vec3 vec)
    {
        d += a*vec.x + b*vec.y + c*vec.z;
        h += e*vec.x + f*vec.y + g*vec.z;
        l += i*vec.x + j*vec.y + k*vec.z;
        p += m*vec.x + n*vec.y + o*vec.z;
        return this;
    }
    
    /**
     * Multiplies this matrix by the given matrix.
     * 
     * @param mat The right-hand matrix to multiply by.
     * @return This matrix.
     */
    public Mat4 mult(Mat4 mat)
    {
        mult(this, mat, this);
        return this;
    }
    
    /**
     * Multiplies this matrix by the given transform.
     * 
     * @param transform The transform to multiply by.
     * @return This matrix.
     */
    public Mat4 mult(Transform transform)
    {
        mult(this, transform, this);
        return this;
    }
    
    /**
     * Multiplies this matrix by the given vector.
     * 
     * @param vec The matrix to multiply by.
     * @return This matrix.
     */
    public Mat4 mult(Vec4 vec)
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
    public Mat4 mult(Vec3 vec)
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
    public Mat4 mult(float sca)
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
    public Mat4 div(float sca)
    {
        div(this, sca, this);
        return this;
    }
    
    /**
     * Transposes this matrix.
     * 
     * @return This matrix.
     */
    public Mat4 transpose()
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
    public Mat4 invert()
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
        a = buffer.getFloat(); e = buffer.getFloat(); i = buffer.getFloat(); m = buffer.getFloat();
        b = buffer.getFloat(); f = buffer.getFloat(); j = buffer.getFloat(); n = buffer.getFloat();
        c = buffer.getFloat(); g = buffer.getFloat(); k = buffer.getFloat(); o = buffer.getFloat();
        d = buffer.getFloat(); h = buffer.getFloat(); l = buffer.getFloat(); p = buffer.getFloat();
    }

    @Override
    public void write(ByteBuffer buffer)
    {
        buffer.putFloat(a); buffer.putFloat(e); buffer.putFloat(i); buffer.putFloat(m);
        buffer.putFloat(b); buffer.putFloat(f); buffer.putFloat(j); buffer.putFloat(n);
        buffer.putFloat(c); buffer.putFloat(g); buffer.putFloat(k); buffer.putFloat(o);
        buffer.putFloat(d); buffer.putFloat(h); buffer.putFloat(l); buffer.putFloat(p);
    }
    
    @Override
    public void read(FloatBuffer buffer)
    {
        a = buffer.get(); e = buffer.get(); i = buffer.get(); m = buffer.get();
        b = buffer.get(); f = buffer.get(); j = buffer.get(); n = buffer.get();
        c = buffer.get(); g = buffer.get(); k = buffer.get(); o = buffer.get();
        d = buffer.get(); h = buffer.get(); l = buffer.get(); p = buffer.get();
    }
    
    @Override
    public void write(FloatBuffer buffer)
    {
        buffer.put(a); buffer.put(e); buffer.put(i); buffer.put(m);
        buffer.put(b); buffer.put(f); buffer.put(j); buffer.put(n);
        buffer.put(c); buffer.put(g); buffer.put(k); buffer.put(o);
        buffer.put(d); buffer.put(h); buffer.put(l); buffer.put(p);
    }
    
    @Override
    public int bufferSize()
    {
        return 16*4;
    }
    
    /**
     * Written to/read from stream in row-major format.
     */
    @Override
    public Mat4 read(DataInputStream in) throws IOException
    {
        a = in.readFloat(); b = in.readFloat(); c = in.readFloat(); d = in.readFloat();
        e = in.readFloat(); f = in.readFloat(); g = in.readFloat(); h = in.readFloat();
        i = in.readFloat(); j = in.readFloat(); k = in.readFloat(); l = in.readFloat();
        m = in.readFloat(); n = in.readFloat(); o = in.readFloat(); p = in.readFloat();
        return this;
    }

    @Override
    public Mat4 write(DataOutputStream out) throws IOException
    {
        out.writeFloat(a); out.writeFloat(b); out.writeFloat(c); out.writeFloat(d);
        out.writeFloat(e); out.writeFloat(f); out.writeFloat(g); out.writeFloat(h);
        out.writeFloat(i); out.writeFloat(j); out.writeFloat(k); out.writeFloat(l);
        out.writeFloat(m); out.writeFloat(n); out.writeFloat(o); out.writeFloat(p);
        return this;
    }
    
    @Override
    public String toString()
    {
        return "[" + a + ", " + b + ", " + c + ", " + d + "]\n" +
               "[" + e + ", " + f + ", " + g + ", " + h + "]\n" +
               "[" + i + ", " + j + ", " + k + ", " + l + "]\n" +
               "[" + m + ", " + n + ", " + o + ", " + p + "]";
    }
    
    public boolean equals(Mat4 mat)
    {
        if (mat == null) return false;
        
        return a == mat.a && b == mat.b && c == mat.c && d == mat.d &&
               e == mat.e && f == mat.f && g == mat.g && h == mat.h &&
               i == mat.i && j == mat.j && k == mat.k && l == mat.l &&
               m == mat.m && n == mat.n && o == mat.o && p == mat.p;
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null) return false;
        if (getClass() != o.getClass()) return false;
        final Mat4 mat = (Mat4)o;
        return equals(mat);
    }
    
    @Override
    public int hashCode()
    {
        int hash = 5;
        hash = 79*hash + Float.floatToIntBits(a);
        hash = 79*hash + Float.floatToIntBits(b);
        hash = 79*hash + Float.floatToIntBits(c);
        hash = 79*hash + Float.floatToIntBits(d);
        hash = 79*hash + Float.floatToIntBits(e);
        hash = 79*hash + Float.floatToIntBits(f);
        hash = 79*hash + Float.floatToIntBits(g);
        hash = 79*hash + Float.floatToIntBits(h);
        hash = 79*hash + Float.floatToIntBits(i);
        hash = 79*hash + Float.floatToIntBits(j);
        hash = 79*hash + Float.floatToIntBits(k);
        hash = 79*hash + Float.floatToIntBits(l);
        hash = 79*hash + Float.floatToIntBits(m);
        hash = 79*hash + Float.floatToIntBits(n);
        hash = 79*hash + Float.floatToIntBits(o);
        hash = 79*hash + Float.floatToIntBits(p);
        return hash;
    }
    // </editor-fold>
}
