/*
 * Copyright (c) 2022 Sam Johnson
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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.util.Objects;

/**
 * 4x4 double precision matrix class.
 * 
 * @author Samuel Johnson (SmashMaster)
 */
public class Mat4d implements DataStreamable<Mat4d>
{
    private static final double SQRT_2 = Math.sqrt(2.0);

    /**
     * Returns the determinant of the given matrix.
     *
     * @param x The matrix to calculate the determinant of.
     * @return The determinant of the given matrix.
     */
    public static final double determinant(Mat4d x)
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
    public static final void copy(Mat2 s, Mat4d r)
    {
        r.a = s.a; r.b = s.b; r.c = 0.0; r.d = 0.0;
        r.e = s.c; r.f = s.d; r.g = 0.0; r.h = 0.0;
        r.i = 0.0; r.j = 0.0; r.k = 1.0; r.l = 0.0;
        r.m = 0.0; r.n = 0.0; r.o = 0.0; r.p = 1.0;
    }

    /**
     * Expands and copies the source matrix into the target matrix.
     *
     * @param s The matrix to copy from.
     * @param r The matrix to copy into.
     */
    public static final void copy(Mat3 s, Mat4d r)
    {
        r.a = s.a; r.b = s.b; r.c = s.c; r.d = 0.0;
        r.e = s.d; r.f = s.e; r.g = s.f; r.h = 0.0;
        r.i = s.g; r.j = s.h; r.k = s.i; r.l = 0.0;
        r.m = 0.0; r.n = 0.0; r.o = 0.0; r.p = 1.0;
    }
    
    public static final void copy(Mat3d s, Mat4d r)
    {
        r.a = s.a; r.b = s.b; r.c = s.c; r.d = 0.0;
        r.e = s.d; r.f = s.e; r.g = s.f; r.h = 0.0;
        r.i = s.g; r.j = s.h; r.k = s.i; r.l = 0.0;
        r.m = 0.0; r.n = 0.0; r.o = 0.0; r.p = 1.0;
    }

    /**
     * Copies the source matrix into the target matrix.
     *
     * @param s The matrix to copy from.
     * @param r The matrix to copy into.
     */
    public static final void copy(Mat4d s, Mat4d r)
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
    public static final void transform(Transform t, Mat4d r)
    {
        translation(new Vec3d(t.pos), r);
        rotate(r, new Quatd(t.rot), r);
        mult(r, new Vec3d(t.sca), r);
    }

    /**
     * Sets the given matrix to the transformation created by the given position, Euler rotation, and scale.
     */
    public static final void transform(Vec3d pos, Euler rot, Vec3d sca, Mat4d r)
    {
        Mat3d rotMat = Euler.toMat3d(rot);
        Mat3d scaMat = Mat3d.scaling(sca);
        Mat3d tempMat = Mat3d.mult(rotMat, scaMat);

        r.set(tempMat);
        r.setEntry(0, 3, pos.x);
        r.setEntry(1, 3, pos.y);
        r.setEntry(2, 3, pos.z);
    }

    /**
     * Sets each entry of the given matrix to zero.
     *
     * @param r The matrix to set to zero.
     */
    public static final void zero(Mat4d r)
    {
        r.a = 0.0; r.b = 0.0; r.c = 0.0; r.d = 0.0;
        r.e = 0.0; r.f = 0.0; r.g = 0.0; r.h = 0.0;
        r.i = 0.0; r.j = 0.0; r.k = 0.0; r.l = 0.0;
        r.m = 0.0; r.n = 0.0; r.o = 0.0; r.p = 0.0;
    }

    /**
     * Sets the given matrix to the identity matrix.
     *
     * @param r The matrix to set to the identity matrix.
     */
    public static final void identity(Mat4d r)
    {
        scaling(1.0, r);
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
    public static final void frustum(double width, double height, double near, double far, Mat4d r)
    {
        double fmn = far - near;
        double n2 = near*2.0;

        r.a = near/width; r.b = 0.0; r.c = 0.0; r.d = 0.0;
        r.e = 0.0; r.f = near/height; r.g = 0.0; r.h = 0.0;
        r.i = 0.0; r.j = 0.0; r.k = -(far + near)/fmn; r.l = (-far*n2)/fmn;
        r.m = 0.0; r.n = 0.0; r.o = -1.0; r.p = 0.0;
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
    public static final void frustum(double left, double right, double bottom, double top, double near, double far, Mat4d r)
    {
        double rml = right - left;
        double tmb = top - bottom;
        double fmn = far - near;
        double n2 = near*2.0;

        r.a = n2/rml; r.b = 0.0; r.c = (right + left)/rml; r.d = 0.0;
        r.e = 0.0; r.f = n2/tmb; r.g = (top + bottom)/tmb; r.h = 0.0;
        r.i = 0.0; r.j = 0.0; r.k = -(far + near)/fmn; r.l = (-far*n2)/fmn;
        r.m = 0.0; r.n = 0.0; r.o = -1.0; r.p = 0.0;
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
    public static final void perspective(double fov, double aspect, double near, double far, Mat4d r)
    {
        double greaterDimension = Math.abs(near)*Math.tan(fov*0.5);

        double w, h;
        if (aspect <= 1.0) //Width is greater or equal to height.
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
    public static final void orthographic(double width, double height, double near, double far, Mat4d r)
    {
        double fmn = far - near;

        r.a = 1.0/width; r.b = 0.0; r.c = 0.0; r.d = 0.0;
        r.e = 0.0; r.f = 1.0/height; r.g = 0.0; r.h = 0.0;
        r.i = 0.0; r.j = 0.0; r.k = -2.0/fmn; r.l = -(far + near)/fmn;
        r.m = 0.0; r.n = 0.0; r.o = 0.0; r.p = 1.0;
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
    public static final void orthographic(double left, double right, double bottom, double top, double near, double far, Mat4d r)
    {
        final double rml = right - left;
        final double tmb = top - bottom;
        final double fmn = far - near;

        r.a = 2.0/rml; r.b = 0.0; r.c = 0.0; r.d = -(right + left)/rml;
        r.e = 0.0; r.f = 2.0/tmb; r.g = 0.0; r.h = -(top + bottom)/tmb;
        r.i = 0.0; r.j = 0.0; r.k = -2.0/fmn; r.l = -(far + near)/fmn;
        r.m = 0.0; r.n = 0.0; r.o = 0.0; r.p = 1.0;
    }

    /**
     * Sets the given matrix to a component-wise scaling matrix using the given
     * vector.
     *
     * @param v The vector to scale by.
     * @param r The matrix in which to store the result.
     */
    public static final void scaling(Vec4 v, Mat4d r)
    {
        r.a = v.x; r.b = 0.0; r.c = 0.0; r.d = 0.0;
        r.e = 0.0; r.f = v.y; r.g = 0.0; r.h = 0.0;
        r.i = 0.0; r.j = 0.0; r.k = v.z; r.l = 0.0;
        r.m = 0.0; r.n = 0.0; r.o = 0.0; r.p = v.w;
    }

    /**
     * Sets the given matrix to a component-wise scaling matrix using the given
     * vector.
     *
     * @param v The vector to scale by.
     * @param r The matrix in which to store the result.
     */
    public static final void scaling(Vec3d v, Mat4d r)
    {
        r.a = v.x; r.b = 0.0; r.c = 0.0; r.d = 0.0;
        r.e = 0.0; r.f = v.y; r.g = 0.0; r.h = 0.0;
        r.i = 0.0; r.j = 0.0; r.k = v.z; r.l = 0.0;
        r.m = 0.0; r.n = 0.0; r.o = 0.0; r.p = 1.0;
    }

    /**
     * Sets the given matrix to a scaling matrix by the given scalar.
     *
     * @param s The scalar to scale by.
     * @param r The matrix in which to store the result.
     */
    public static final void scaling(double s, Mat4d r)
    {
        r.a = s; r.b = 0.0; r.c = 0.0; r.d = 0.0;
        r.e = 0.0; r.f = s; r.g = 0.0; r.h = 0.0;
        r.i = 0.0; r.j = 0.0; r.k = s; r.l = 0.0;
        r.m = 0.0; r.n = 0.0; r.o = 0.0; r.p = s;
    }

    /**
     * Sets the given matrix to the rotation matrix, using the given {@code axis}
     * of rotation, and {@code ang} as the angle. The axis must be normalized.
     *
     * @param axis The axis around which to rotate.
     * @param angle The angle to rotate by.
     * @param r The matrix in which to store the result.
     */
    public static final void rotation(Vec3d axis, double angle, Mat4d r)
    {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        double omcos = 1.0 - cos;

        double xsq = axis.x*axis.x, ysq = axis.y*axis.y, zsq = axis.z*axis.z;

        double xyomcos = axis.x*axis.y*omcos, zsin = axis.z*sin;
        double xzomcos = axis.x*axis.z*omcos, ysin = axis.y*sin;
        double yzomcos = axis.y*axis.z*omcos, xsin = axis.x*sin;

        r.a = xsq + (1.0 - xsq)*cos;
        r.b = xyomcos - zsin;
        r.c = xzomcos + ysin;
        r.d = 0.0;

        r.e = xyomcos + zsin;
        r.f = ysq + (1.0 - ysq)*cos;
        r.g = yzomcos - xsin;
        r.h = 0.0;

        r.i = xzomcos - ysin;
        r.j = yzomcos + xsin;
        r.k = zsq + (1.0 - zsq)*cos;
        r.l = 0.0;

        r.m = 0.0; r.n = 0.0; r.o = 0.0; r.p = 1.0;
    }

    /**
     * Sets the given matrix to a rotation matrix representation of the given
     * quaternion.
     *
     * @param q The quaternion to represent as a matrix.
     * @param r The matrix in which to store the result.
     */
    public static final void rotation(Quatd q, Mat4d r)
    {
        double q0 = SQRT_2*q.w, q1 = SQRT_2*q.x, q2 = SQRT_2*q.y, q3 = SQRT_2*q.z;

	double qda = q0*q1, qdb = q0*q2, qdc = q0*q3;
	double qaa = q1*q1, qab = q1*q2, qac = q1*q3;
	double qbb = q2*q2, qbc = q2*q3, qcc = q3*q3;

        r.a = 1.0 - qbb - qcc; r.b = -qdc + qab; r.c = qdb + qac; r.d = 0.0;
        r.e = qdc + qab; r.f = 1.0 - qaa - qcc; r.g = -qda + qbc; r.h = 0.0;
        r.i = -qdb + qac; r.j = qda + qbc; r.k = 1.0 - qaa - qbb; r.l = 0.0;
        r.m = 0.0; r.n = 0.0; r.o = 0.0; r.p = 1.0;
    }

    /**
     * Sets the given matrix to the translation matrix by the given vector.
     *
     * @param v The vector to translate by.
     * @param r The matrix in which to store the result.
     */
    public static final void translation(Vec3d v, Mat4d r)
    {
        r.a = 1.0; r.b = 0.0; r.c = 0.0; r.d = v.x;
        r.e = 0.0; r.f = 1.0; r.g = 0.0; r.h = v.y;
        r.i = 0.0; r.j = 0.0; r.k = 1.0; r.l = v.z;
        r.m = 0.0; r.n = 0.0; r.o = 0.0; r.p = 1.0;
    }

    /**
     * Sets the given matrix to a reflection matrix about the given plane.
     *
     * @param n The plane's normal vector.
     * @param d The plane constant.
     * @param r The matrix in which to store the result.
     */
    public static final void reflection(Vec3d n, double d, Mat4d r)
    {
        double ab = -2.0*n.x*n.y;
        double ac = -2.0*n.x*n.z;
        double bc = -2.0*n.y*n.z;

        r.a = 1.0 - 2.0*n.x*n.x; r.b = ab; r.c = ac; r.d = -2.0*n.x*d;
        r.e = ab; r.f = 1.0 - 2.0*n.y*n.y; r.g = bc; r.h = -2.0*n.y*d;
        r.i = ac; r.j = bc; r.k = 1.0 - 2.0*n.z*n.z; r.l = -2.0*n.z*d;
        r.m = 0.0; r.n = 0.0; r.o = 0.0; r.p = 1.0;
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
    public static final void rotate(Mat4d x, Vec3d axis, double angle, Mat4d r)
    {
        Mat4d temp = rotation(axis, angle); //Could probably be improved.
        mult(x, temp, r);
    }

    /**
     * Rotates {@code m} by the given quaternion and stores the result in {@code r}.
     *
     * @param x The matrix to rotate.
     * @param q The quaternion to rotate by.
     * @param r The matrix in which to store the result.
     */
    public static final void rotate(Mat4d x, Quatd q, Mat4d r)
    {
        Mat4d temp = rotation(q); //Could probably be improved, as above
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
    public static final void translate(Mat4d x, Vec3d v, Mat4d r)
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
    public static final void mult(Mat4d m0, Mat4d m1, Mat4d r)
    {
        double a = m0.a*m1.a + m0.b*m1.e + m0.c*m1.i + m0.d*m1.m;
        double b = m0.a*m1.b + m0.b*m1.f + m0.c*m1.j + m0.d*m1.n;
        double c = m0.a*m1.c + m0.b*m1.g + m0.c*m1.k + m0.d*m1.o;
        double d = m0.a*m1.d + m0.b*m1.h + m0.c*m1.l + m0.d*m1.p;

        double e = m0.e*m1.a + m0.f*m1.e + m0.g*m1.i + m0.h*m1.m;
        double f = m0.e*m1.b + m0.f*m1.f + m0.g*m1.j + m0.h*m1.n;
        double g = m0.e*m1.c + m0.f*m1.g + m0.g*m1.k + m0.h*m1.o;
        double h = m0.e*m1.d + m0.f*m1.h + m0.g*m1.l + m0.h*m1.p;

        double i = m0.i*m1.a + m0.j*m1.e + m0.k*m1.i + m0.l*m1.m;
        double j = m0.i*m1.b + m0.j*m1.f + m0.k*m1.j + m0.l*m1.n;
        double k = m0.i*m1.c + m0.j*m1.g + m0.k*m1.k + m0.l*m1.o;
        double l = m0.i*m1.d + m0.j*m1.h + m0.k*m1.l + m0.l*m1.p;

        double m = m0.m*m1.a + m0.n*m1.e + m0.o*m1.i + m0.p*m1.m;
        double n = m0.m*m1.b + m0.n*m1.f + m0.o*m1.j + m0.p*m1.n;
        double o = m0.m*m1.c + m0.n*m1.g + m0.o*m1.k + m0.p*m1.o;
        double p = m0.m*m1.d + m0.n*m1.h + m0.o*m1.l + m0.p*m1.p;

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
    public static final void mult(Mat4d m, Transform t, Mat4d r)
    {
        translate(m, new Vec3d(t.pos), r);
        rotate(r, new Quatd(t.rot), r);
        mult(r, new Vec3d(t.sca), r);
    }

    /**
     * Multiplies the given matrix by the given vector.
     *
     * @param x The matrix to multiply.
     * @param v The vector to multiply by.
     * @param r The matrix in which to store the result.
     */
    public static final void mult(Mat4d x, Vec4 v, Mat4d r)
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
    public static final void mult(Mat4d x, Vec3d v, Mat4d r)
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
    public static final void mult(Mat4d x, double s, Mat4d r)
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
    public static final void div(Mat4d x, double s, Mat4d r)
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
    public static final void transpose(Mat4d x, Mat4d r)
    {
        double tb = x.b, tc = x.c, td = x.d;
        double tg = x.g, th = x.h;
        double tl = x.l;
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
     * @throws SingularMatrixException If {@code m} is
     *         a singular matrix. (Its determinant is zero.)
     */
    public static final void invert(Mat4d x, Mat4d r)
    {
        double a = x.f*x.k*x.p + x.g*x.l*x.n + x.h*x.j*x.o - x.f*x.l*x.o - x.g*x.j*x.p - x.h*x.k*x.n;
        double e = x.e*x.l*x.o + x.g*x.i*x.p + x.h*x.k*x.m - x.e*x.k*x.p - x.g*x.l*x.m - x.h*x.i*x.o;
        double i = x.e*x.j*x.p + x.f*x.l*x.m + x.h*x.i*x.n - x.e*x.l*x.n - x.f*x.i*x.p - x.h*x.j*x.m;
        double m = x.e*x.k*x.n + x.f*x.i*x.o + x.g*x.j*x.m - x.e*x.j*x.o - x.f*x.k*x.m - x.g*x.i*x.n;

        double det = x.a*a + x.b*e + x.c*i + x.d*m;
        if (det == 0.0) throw new SingularMatrixException();

        double b = x.b*x.l*x.o + x.c*x.j*x.p + x.d*x.k*x.n - x.b*x.k*x.p - x.c*x.l*x.n - x.d*x.j*x.o;
        double c = x.b*x.g*x.p + x.c*x.h*x.n + x.d*x.f*x.o - x.b*x.h*x.o - x.c*x.f*x.p - x.d*x.g*x.n;
        double d = x.b*x.h*x.k + x.c*x.f*x.l + x.d*x.g*x.j - x.b*x.g*x.l - x.c*x.h*x.j - x.d*x.f*x.k;
        double f = x.a*x.k*x.p + x.c*x.l*x.m + x.d*x.i*x.o - x.a*x.l*x.o - x.c*x.i*x.p - x.d*x.k*x.m;
        double g = x.a*x.h*x.o + x.c*x.e*x.p + x.d*x.g*x.m - x.a*x.g*x.p - x.c*x.h*x.m - x.d*x.e*x.o;
        double h = x.a*x.g*x.l + x.c*x.h*x.i + x.d*x.e*x.k - x.a*x.h*x.k - x.c*x.e*x.l - x.d*x.g*x.i;
        double j = x.a*x.l*x.n + x.b*x.i*x.p + x.d*x.j*x.m - x.a*x.j*x.p - x.b*x.l*x.m - x.d*x.i*x.n;
        double k = x.a*x.f*x.p + x.b*x.h*x.m + x.d*x.e*x.n - x.a*x.h*x.n - x.b*x.e*x.p - x.d*x.f*x.m;
        double l = x.a*x.h*x.j + x.b*x.e*x.l + x.d*x.f*x.i - x.a*x.f*x.l - x.b*x.h*x.i - x.d*x.e*x.j;
        double n = x.a*x.j*x.o + x.b*x.k*x.m + x.c*x.i*x.n - x.a*x.k*x.n - x.b*x.i*x.o - x.c*x.j*x.m;
        double o = x.a*x.g*x.n + x.b*x.e*x.o + x.c*x.f*x.m - x.a*x.f*x.o - x.b*x.g*x.m - x.c*x.e*x.n;
        double p = x.a*x.f*x.k + x.b*x.g*x.i + x.c*x.e*x.j - x.a*x.g*x.j - x.b*x.e*x.k - x.c*x.f*x.i;

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
    public static final Mat4d identity()
    {
        return scaling(1.0);
    }

    /**
     * Returns a new 4x4 transformation matrix equal to the given transform.
     *
     * @param transform The transform to convert.
     * @return A new matrix containing the result.
     */
    public static final Mat4d transform(Transform transform)
    {
        Mat4d m = new Mat4d();
        transform(transform, m);
        return m;
    }

    /**
     * Returns a new
     */
    public static final Mat4d transform(Vec3d pos, Euler rot, Vec3d sca)
    {
        Mat4d m = new Mat4d();
        transform(pos, rot, sca, m);
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
    public static final Mat4d frustum(double width, double height, double near, double far)
    {
        Mat4d m = new Mat4d();
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
    public static final Mat4d frustum(double left, double right, double bottom, double top, double near, double far)
    {
        Mat4d m = new Mat4d();
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
    public static final Mat4d perspective(double fov, double aspect, double near, double far)
    {
        Mat4d m = new Mat4d();
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
    public static final Mat4d orthographic(double width, double height, double near, double far)
    {
        Mat4d m = new Mat4d();
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
    public static final Mat4d orthographic(double left, double right, double bottom, double top, double near, double far)
    {
        Mat4d m = new Mat4d();
        orthographic(left, right, bottom, top, near, far, m); //Could be optimized.
        return m;
    }

    /**
     * Returns a new component-wise scaling matrix using the given vector.
     *
     * @param v The vector to scale by.
     * @return A new scaling matrix.
     */
    public static final Mat4d scaling(Vec4 v)
    {
        Mat4d m = new Mat4d();
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
    public static final Mat4d scaling(Vec3d v)
    {
        Mat4d m = new Mat4d();
        m.a = v.x;
        m.f = v.y;
        m.k = v.z;
        m.p = 1.0;
        return m;
    }

    /**
     * Returns a new scaling matrix, where {@code s} is the scaling factor.
     *
     * @param s The scaling factor.
     * @return A new scaling matrix.
     */
    public static final Mat4d scaling(double s)
    {
        Mat4d m = new Mat4d();
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
    public static final Mat4d rotation(Vec3d axis, double angle)
    {
        Mat4d m = new Mat4d();
        rotation(axis, angle, m);
        return m;
    }

    /**
     * Returns a new rotation matrix representing the given quaternion.
     *
     * @param q The quaternion to represent as a matrix.
     * @return A new rotation matrix.
     */
    public static final Mat4d rotation(Quatd q)
    {
        Mat4d m = new Mat4d();
        rotation(q, m);
        return m;
    }

    /**
     * Returns a new translation matrix using the given vector.
     *
     * @param v The vector to translate by.
     * @return A new translation matrix.
     */
    public static final Mat4d translation(Vec3d v)
    {
        Mat4d m = identity();
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
    public static final Mat4d reflection(Vec3d normal, double constant)
    {
        Mat4d m = new Mat4d();
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
    public static final Mat4d mult(Mat4d m0, Mat4d m1)
    {
        Mat4d result = new Mat4d();
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
    public static final Mat4d mult(Mat4d m, Transform t)
    {
        Mat4d result = new Mat4d();
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
    public static final Mat4d mult(Mat4d m, Vec4 v)
    {
        Mat4d result = new Mat4d();
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
    public static final Mat4d mult(Mat4d m, Vec3d v)
    {
        Mat4d result = new Mat4d();
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
    public static final Mat4d mult(Mat4d m, double s)
    {
        Mat4d result = new Mat4d();
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
    public static final Mat4d div(Mat4d m, double s)
    {
        Mat4d result = new Mat4d();
        div(m, s, result);
        return result;
    }

    /**
     * Returns the transpose of {@code m} as a new matrix.
     *
     * @param m The matrix to compute the transpose of.
     * @return A new matrix containing the result.
     */
    public static final Mat4d transpose(Mat4d m)
    {
        Mat4d result = new Mat4d();
        transpose(m, result);
        return result;
    }

    /**
     * Calculates the inverse of {@code m} and returns the result as a new matrix.
     *
     * @param m The matrix to compute the inverse of.
     * @return A new matrix containing the result.
     * @throws SingularMatrixException If {@code m} is
     *         a singular matrix. (Its determinant is zero.)
     */
    public static final Mat4d invert(Mat4d m)
    {
        Mat4d result = new Mat4d();
        invert(m, result);
        return result;
    }
    // </editor-fold>

    public double a, b, c, d,
                  e, f, g, h,
                  i, j, k, l,
                  m, n, o, p;

    // <editor-fold defaultstate="collapsed" desc="Constructors">
    /**
     * Creates a new 4x4 zero matrix, NOT an identity matrix. Use identity() to
     * create an identity matrix.
     */
    public Mat4d()
    {
    }

    /**
     * Creates a new 4x4 matrix with the given values.
     */
    public Mat4d(double a, double b, double c, double d,
                 double e, double f, double g, double h,
                 double i, double j, double k, double l,
                 double m, double n, double o, double p)
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
    public Mat4d(Mat2 x)
    {
        a = x.a; b = x.b;
        e = x.c; f = x.d;
        k = 1.0; p = 1.0;
    }

    /**
     * Expands and copies the given 3x3 matrix.
     *
     * @param x The matrix to copy.
     */
    public Mat4d(Mat3d x)
    {
        a = x.a; b = x.b; c = x.c;
        e = x.d; f = x.e; g = x.f;
        i = x.g; j = x.h; k = x.i;
        p = 1.0;
    }

    public Mat4d(Mat3 x)
    {
        a = x.a; b = x.b; c = x.c;
        e = x.d; f = x.e; g = x.f;
        i = x.g; j = x.h; k = x.i;
        p = 1.0;
    }

    /**
     * Copies the given 4x4 matrix.
     *
     * @param x The matrix to copy.
     */
    public Mat4d(Mat4d x)
    {
        a = x.a; b = x.b; c = x.c; d = x.d;
        e = x.e; f = x.f; g = x.g; h = x.h;
        i = x.i; j = x.j; k = x.k; l = x.l;
        m = x.m; n = x.n; o = x.o; p = x.p;
    }

    /**
     * Copies the given 4x4 matrix.
     *
     * @param x The matrix to copy.
     */
    public Mat4d(Mat4 x)
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
    public Mat4d(ByteBuffer buffer)
    {
        Mat4d.this.read(buffer);
    }

    /**
     * Loads a new matrix from the given input stream.
     *
     * @param in The input stream to read from.
     * @throws IOException If an io error occurred.
     */
    public Mat4d(DataInputStream in) throws IOException
    {
        Mat4d.this.read(in);
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
    public double getEntry(int row, int column)
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
    public Mat4d set(Mat2 mat)
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
    public Mat4d set(Mat3 mat)
    {
        copy(mat, this);
        return this;
    }

    public Mat4d set(Mat3d mat)
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
    public Mat4d set(Mat4d mat)
    {
        copy(mat, this);
        return this;
    }
    
    /**
     * Sets the entries of this matrix.
     * 
     * @return This matrix.
     */
    public Mat4d set(double a, double b, double c, double d,
                     double e, double f, double g, double h,
                     double i, double j, double k, double l,
                     double m, double n, double o, double p)
    {
        this.a = a; this.b = b; this.c = c; this.d = d;
        this.e = e; this.f = f; this.g = g; this.h = h;
        this.i = i; this.j = j; this.k = k; this.l = l;
        this.m = m; this.n = n; this.o = o; this.p = p;
        return this;
    }
    
    /**
     * Sets the entry at the given position in this matrix to the given double.
     * 
     * @param row A row of this matrix.
     * @param column A column of this matrix.
     * @param v The value to set the entry to.
     * @return This matrix.
     */
    public Mat4d setEntry(int row, int column, double v)
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
    public Mat4d setRow(Vec4 v, int row)
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
    public Mat4d setColumn(Vec4 v, int column)
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
    public Mat4d setZero()
    {
        zero(this);
        return this;
    }
    
    /**
     * Sets this to the identity matrix.
     * 
     * @return This matrix.
     */
    public Mat4d setIdentity()
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
    public Mat4d setTransform(Transform transform)
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
    public Mat4d setFrustum(double width, double height, double near, double far)
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
    public Mat4d setFrustum(double left, double right, double bottom, double top, double near, double far)
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
    public Mat4d setPerspective(double fov, double aspect, double near, double far)
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
    public Mat4d setOrthographic(double width, double height, double near, double far)
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
    public Mat4d setOrthographic(double left, double right, double bottom, double top, double near, double far)
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
    public Mat4d setScaling(Vec4 vec)
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
    public Mat4d setScaling(Vec3d vec)
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
    public Mat4d setScaling(double sca)
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
    public Mat4d setRotation(Vec3d axis, double angle)
    {
        rotation(axis, angle, this);
        return this;
    }
    
    /**
     * Sets this to a rotation matrix representation of the given quaternion.
     * 
     * @return This matrix.
     */
    public Mat4d setRotation(Quatd quat)
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
    public Mat4d setTranslation(Vec3d vec)
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
    public Mat4d setReflection(Vec3d normal, double constant)
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
    public Mat4d rotate(Vec3d axis, double angle)
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
    public Mat4d rotate(Quatd quat)
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
    public Mat4d translate(Vec3d vec)
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
    public Mat4d mult(Mat4d mat)
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
    public Mat4d mult(Transform transform)
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
    public Mat4d mult(Vec4 vec)
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
    public Mat4d mult(Vec3d vec)
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
    public Mat4d mult(double sca)
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
    public Mat4d div(double sca)
    {
        div(this, sca, this);
        return this;
    }
    
    /**
     * Transposes this matrix.
     * 
     * @return This matrix.
     */
    public Mat4d transpose()
    {
        transpose(this, this); //Could be optimized.
        return this;
    }
    
    /**
     * Inverts this matrix.
     * 
     * @throws SingularMatrixException If this matrix is
     *         singular. (Its determinant is zero.)
     * @return This matrix.
     */
    public Mat4d invert()
    {
        invert(this, this);
        return this;
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Overriden/implemented methods">
    /**
     * WARNING: Buffered in column-major format, as per OpenGL.
     */
    public void read(ByteBuffer buffer)
    {
        a = buffer.getDouble(); e = buffer.getDouble(); i = buffer.getDouble(); m = buffer.getDouble();
        b = buffer.getDouble(); f = buffer.getDouble(); j = buffer.getDouble(); n = buffer.getDouble();
        c = buffer.getDouble(); g = buffer.getDouble(); k = buffer.getDouble(); o = buffer.getDouble();
        d = buffer.getDouble(); h = buffer.getDouble(); l = buffer.getDouble(); p = buffer.getDouble();
    }

    public void write(ByteBuffer buffer)
    {
        buffer.putDouble(a); buffer.putDouble(e); buffer.putDouble(i); buffer.putDouble(m);
        buffer.putDouble(b); buffer.putDouble(f); buffer.putDouble(j); buffer.putDouble(n);
        buffer.putDouble(c); buffer.putDouble(g); buffer.putDouble(k); buffer.putDouble(o);
        buffer.putDouble(d); buffer.putDouble(h); buffer.putDouble(l); buffer.putDouble(p);
    }

    public void read(DoubleBuffer buffer)
    {
        a = buffer.get(); e = buffer.get(); i = buffer.get(); m = buffer.get();
        b = buffer.get(); f = buffer.get(); j = buffer.get(); n = buffer.get();
        c = buffer.get(); g = buffer.get(); k = buffer.get(); o = buffer.get();
        d = buffer.get(); h = buffer.get(); l = buffer.get(); p = buffer.get();
    }

    public void write(DoubleBuffer buffer)
    {
        buffer.put(a); buffer.put(e); buffer.put(i); buffer.put(m);
        buffer.put(b); buffer.put(f); buffer.put(j); buffer.put(n);
        buffer.put(c); buffer.put(g); buffer.put(k); buffer.put(o);
        buffer.put(d); buffer.put(h); buffer.put(l); buffer.put(p);
    }

    public int bufferSize()
    {
        return 16*8;
    }
    
    /**
     * Written to/read from stream in row-major format.
     */
    @Override
    public Mat4d read(DataInputStream in) throws IOException
    {
        a = in.readDouble(); b = in.readDouble(); c = in.readDouble(); d = in.readDouble();
        e = in.readDouble(); f = in.readDouble(); g = in.readDouble(); h = in.readDouble();
        i = in.readDouble(); j = in.readDouble(); k = in.readDouble(); l = in.readDouble();
        m = in.readDouble(); n = in.readDouble(); o = in.readDouble(); p = in.readDouble();
        return this;
    }

    @Override
    public Mat4d write(DataOutputStream out) throws IOException
    {
        out.writeDouble(a); out.writeDouble(b); out.writeDouble(c); out.writeDouble(d);
        out.writeDouble(e); out.writeDouble(f); out.writeDouble(g); out.writeDouble(h);
        out.writeDouble(i); out.writeDouble(j); out.writeDouble(k); out.writeDouble(l);
        out.writeDouble(m); out.writeDouble(n); out.writeDouble(o); out.writeDouble(p);
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
    
    public boolean equals(Mat4d mat)
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
        final Mat4d mat = (Mat4d)o;
        return equals(mat);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p);
    }
    // </editor-fold>
}
