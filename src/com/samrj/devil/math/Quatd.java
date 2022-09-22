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

import com.samrj.devil.util.Bufferable;
import com.samrj.devil.util.DataStreamable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * Quaternion class.
 * 
 * @author Samuel Johnson (SmashMaster)
 */
public class Quatd implements DataStreamable<Quatd>, Bufferable
{
    private static final double EPSILON = 1.0/65536.0;
    
    // <editor-fold defaultstate="collapsed" desc="Static accessor methods">
    /**
     * Returns the dot product of the two given quaternions.
     * 
     * @param q0 The first quaternion to multiply.
     * @param q1 The second quaternion to multiply.
     * @return The dot product of the two given quaternions.
     */
    public static final double dot(Quatd q0, Quatd q1)
    {
        return q0.w*q1.w + q0.x*q1.x + q0.y*q1.y + q0.z*q1.z;
    }
    
    /**
     * Returns the square length of the given quaternion. May be alternately
     * defined as the dot product of the quaternion with itself.
     * 
     * @param q A quaternion.
     * @return The dot product of the given quaternion.
     */
    public static final double squareLength(Quatd q)
    {
        return q.w*q.w + q.x*q.x + q.y*q.y + q.z*q.z;
    }
    
    /**
     * Returns the length of the given quaternion.
     * 
     * @param q A quaternion.
     * @return The length of the given quaternion.
     */
    public static final double length(Quatd q)
    {
        return Math.sqrt(squareLength(q));
    }

    /**
     * Returns the specified component of the given quaternion.
     */
    public static final double getComponent(Quatd q, int component)
    {
        return switch(component)
        {
            case 0 -> q.w;
            case 1 -> q.x;
            case 2 -> q.y;
            case 3 -> q.z;
            default -> throw new ArrayIndexOutOfBoundsException();
        };
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Static mutator methods">
    /**
     *  Copies {@code source} into {@code target}.
     * 
     * @param source The quaternion to copy.
     * @param target The quaternion to copy into.
     */
    public static final void copy(Quatd source, Quatd target)
    {
        target.w = source.w;
        target.x = source.x;
        target.y = source.y;
        target.z = source.z;
    }

    /**
     * Sets the specified component of the given quaternion to the given value.
     */
    public static final void setComponent(Quatd q, int component, double value)
    {
        switch(component)
        {
            case 0 -> q.w = value;
            case 1 -> q.x = value;
            case 2 -> q.y = value;
            case 3 -> q.z = value;
            default -> throw new ArrayIndexOutOfBoundsException();
        }
    }
    
    /**
     * Sets the given quaternion to the identity rotation.
     * 
     * @param result The quaternion in which to store the result.
     */
    public static final void identity(Quatd result)
    {
        result.w = 1.0;
        result.x = 0.0;
        result.y = 0.0;
        result.z = 0.0;
    }
    
    /**
     * Sets {@code result} to the rotation around the given axis, by the given
     * angle. The axis must be normalized.
     * 
     * @param axis The axis around which to rotate.
     * @param angle The angle to rotate by.
     * @param result The quaternion in which to store the result.
     */
    public static final void rotation(Vec3d axis, double angle, Quatd result)
    {
        double a = angle*0.5;
        double sin = Math.sin(a);
        
        result.w = Math.cos(a);
        result.x = axis.x*sin;
        result.y = axis.y*sin;
        result.z = axis.z*sin;
    }
    
    /**
     * Sets {@code result} to the shortest rotation from the starting direction
     * vector to the end vector.
     * 
     * @param start The start direction.
     * @param end The end direction.
     * @param result The quaternion in which to store the result.
     */
    public static final void rotation(Vec3d start, Vec3d end, Quatd result)
    {
        if (start.isZero() || end.isZero())
        {
            result.setIdentity();
            return;
        }
        
        Vec3d v0 = Vec3d.normalize(start);
        Vec3d v1 = Vec3d.normalize(end);

        double dot = v0.dot(v1);
        if (Util.equals(dot, 1.0, EPSILON)) //this and v have same direction.
        {
            result.setIdentity();
            return;
        }
        
        //this and v have opposite direction. Rotate 180 degrees about an
        //arbitrary axis normal to this.
        if (Util.equals(dot, -1.0, EPSILON))
        {
            Vec3d axis = new Vec3d(1.0, 0.0, 0.0).cross(v0);
            //this lies along X axis and v is our opposite, so we can optimize.
            if (axis.x == 0.0 && axis.y == 0.0 && axis.z == 0.0)
                result.set(0.0, 0.0, 0.0, -1.0);
            else result.setRotation(axis.normalize(), Util.PI);
            return;
        }
        
        v0.cross(v1);
        double s = Math.sqrt(2.0 + dot*2.0);
        result.set(s*0.5, v0.x/s, v0.y/s, v0.z/s).normalize();
    }
    
    /**
     * Sets {@code result} to the rotation represented by the given matrix.
     * 
     * @param m A rotation matrix.
     * @param result The quaternion in which to store the result.
     */
    public static final void rotation(Mat3d m, Quatd result)
    {
        double[] tr = {m.a + m.e + m.i,
                      m.a - m.e - m.i,
                      m.e - m.a - m.i,
                      m.i - m.a - m.e};
        
        int i = Util.maxdex(tr);
        double s = 2.0*Math.sqrt(1.0 + tr[i]);
        switch(i)
        {
            case 0: result.w = 0.25*s;
                    result.x = (m.h - m.f)/s;
                    result.y = (m.c - m.g)/s;
                    result.z = (m.d - m.b)/s;
                    return;
            case 1: result.w = (m.h - m.f)/s;
                    result.x = 0.25*s;
                    result.y = (m.b + m.d)/s;
                    result.z = (m.c + m.g)/s;
                    return;
            case 2: result.w = (m.c - m.g)/s;
                    result.x = (m.b + m.d)/s;
                    result.y = 0.25*s;
                    result.z = (m.f + m.h)/s;
                    return;
            case 3: result.w = (m.d - m.b)/s;
                    result.x = (m.c + m.g)/s;
                    result.y = (m.f + m.h)/s;
                    result.z = 0.25*s;
        }
    }
    
    /**
     * Rotates {@code q} about the given {@code axis} by the given angle
     * {@code angle} and stores the result in {@code result}. The axis must be
     * normalized.
     * 
     * @param q The quaternion to rotate.
     * @param axis The axis to rotate around.
     * @param angle The angle to rotate by.
     * @param result The matrix in which to store the result.
     */
    public static final void rotate(Quatd q, Vec3d axis, double angle, Quatd result)
    {
        Quatd temp = rotation(axis, angle);
        mult(q, temp, result);
    }
    
    /**
     * Adds the two given quaternions and stores the result in {@code result}.
     * 
     * @param q0 The first quaternion to add.
     * @param q1 The second quaternion to add.
     * @param result The quaternion in which to store the result.
     */
    public static final void add(Quatd q0, Quatd q1, Quatd result)
    {
        result.w = q0.w + q1.w;
        result.x = q0.x + q1.x;
        result.y = q0.y + q1.y;
        result.z = q0.z + q1.z;
    }
    
    /**
     * Subtracts {@code q1} from {@code q0} and stores the result in {@code result}.
     * 
     * @param q0 The quaternion to subtract from.
     * @param q1 The quaternion to subtract by.
     * @param result The quaternion in which to store the result.
     */
    public static final void sub(Quatd q0, Quatd q1, Quatd result)
    {
        result.w = q0.w - q1.w;
        result.x = q0.x - q1.x;
        result.y = q0.y - q1.y;
        result.z = q0.z - q1.z;
    }
    
    /**
     * Multiplies {@code q0} by {@code q1} and stores the result in {@code result}.
     * 
     * @param q0 The left-hand quaternion to multiply.
     * @param q1 The right-hand quaternion to multiply by.
     * @param result The quaternion in which to store the result.
     */
    public static final void mult(Quatd q0, Quatd q1, Quatd result)
    {
        double w = q0.w*q1.w - q0.x*q1.x - q0.y*q1.y - q0.z*q1.z;
        double x = q0.w*q1.x + q0.x*q1.w + q0.y*q1.z - q0.z*q1.y;
        double y = q0.w*q1.y - q0.x*q1.z + q0.y*q1.w + q0.z*q1.x;
        double z = q0.w*q1.z + q0.x*q1.y - q0.y*q1.x + q0.z*q1.w;
        
        result.w = w; result.x = x; result.y = y; result.z = z;
    }
    
    /**
     * Multiplies {@code q1} by {@code s}, adds {@code q0}, and stores the
     * result in {@code result}.
     * 
     * @param v0 The quaternion to add to.
     * @param v1 The quaternion to multiply by {@code s} and then add to {@code q0}.
     * @param s The scalar by which to multiply {@code q1}.
     * @param result The quaternion in which to store the result.
     */
    public static final void madd(Quatd v0, Quatd v1, double s, Quatd result)
    {
        result.w = v0.w + v1.w*s;
        result.x = v0.x + v1.x*s;
        result.y = v0.y + v1.y*s;
        result.z = v0.z + v1.z*s;
    }
    
    /**
     * Multiplies the given quaternion by the given scalar and stores the result
     * in {@code result}.
     * 
     * @param q The quaternion to multiply.
     * @param s The scalar to multiply by.
     * @param result The quaternion in which to store the result.
     */
    public static final void mult(Quatd q, double s, Quatd result)
    {
        result.w = q.w*s;
        result.x = q.x*s;
        result.y = q.y*s;
        result.z = q.z*s;
    }
    
    /**
     * Divides the given quaternion by the given scalar and stores the result
     * in {@code result}.
     * 
     * @param q The quaternion to multiply.
     * @param s The scalar to multiply by.
     * @param result The quaternion in which to store the result.
     */
    public static final void div(Quatd q, double s, Quatd result)
    {
        result.w = q.w/s;
        result.x = q.x/s;
        result.y = q.y/s;
        result.z = q.z/s;
    }
    
    /**
     * Negates the given quaternion and stores the result in {@code result}.
     * 
     * @param q The quaternion to negate.
     * @param result The quaternion in which to store the result.
     */
    public static final void negate(Quatd q, Quatd result)
    {
        result.w = -q.w;
        result.x = -q.x;
        result.y = -q.y;
        result.z = -q.z;
    }
    
    /**
     * Calculates the conjugate of the given quaternion and stores the result
     * in {@code result}.
     * 
     * @param q The quaternion to conjugate.
     * @param result The quaternion in which to store the result.
     */
    public static final void conjugate(Quatd q, Quatd result)
    {
        result.w = q.w;
        result.x = -q.x;
        result.y = -q.y;
        result.z = -q.z;
    }
    
    /**
     * Normalizes the given quaternion and stores the result in {@code result}.
     * 
     * @param q The quaternion to normalize.
     * @param result The quaternion in which to store the result. 
     */
    public static final void normalize(Quatd q, Quatd result)
    {
        div(q, length(q), result);
    }
    
    /**
     * Inverts the given quaternion and stores the result in {@code result}.
     * 
     * @param q The quaternion to invert.
     * @param result The quaternion in which to store the result.
     */
    public static final void invert(Quatd q, Quatd result)
    {
        conjugate(q, result);
        div(result, squareLength(result), result);
    }
    
    /**
     * Linearly interpolates between the two given quaternions using the given
     * scalar and stores the result in {@code result}.
     * 
     * @param q0 The 'start' quaternion to interpolate from.
     * @param q1 The 'end' quaternion to interpolate to.
     * @param t The scalar interpolant, between zero and one (inclusive).
     * @param result The vector in which to store the result.
     */
    public static final void lerp(Quatd q0, Quatd q1, double t, Quatd result)
    {
        result.w = Util.lerp(q0.w, q1.w, t);
        result.x = Util.lerp(q0.x, q1.x, t);
        result.y = Util.lerp(q0.y, q1.y, t);
        result.z = Util.lerp(q0.z, q1.z, t);
    }
    
    /**
     * Performs a spherical linear interpolation between the two given
     * quaternions and stores the result in {@code r}.
     * 
     * TODO: Actually fix this crap.
     * 
     * @param q0 The 'start' quaternion to interpolate from.
     * @param q1 The 'end' quaternion to interpolate to.
     * @param t The scalar interpolant, between zero and one (inclusive).
     * @param result The quaternion in which to store the result.
     */
    public static final void slerp(Quatd q0, Quatd q1, double t, Quatd result)
    {
        double dot = dot(q0, q1);
        if (dot < 0.0)
        {
            dot = -dot;
            q0 = negate(q0);
        }
        
        if (dot > 0.9995) lerp(q0, q1, t, result);
        else
        {
            double ang = Math.acos(dot);
            mult(q0, Math.sin((1.0 - t)*ang), result);
            madd(result, q1, Math.sin(t*ang), result);
        }
        
        normalize(result, result);
    }
    
    /**
     * Calculates the Tait–Bryan angles of this quaternion, and stores them in
     * the given vector as pitch, yaw, and roll.
     * 
     * @param q The quaternion whose angles to calculate.
     * @param result The vector in which to store the result.
     */
    public static final void angles(Quatd q, Vec3d result)
    {
        result.x = Math.atan2(2.0*(q.w*q.x - q.y*q.z), 1.0 - 2.0*(q.z*q.z + q.x*q.x));
        result.y = Math.atan2(2.0*(q.w*q.y - q.z*q.x), 1.0 - 2.0*(q.y*q.y + q.z*q.z));
        result.z = Math.asin(2.0*(q.x*q.y - q.w*q.z));
    }
    
    /**
     * Recovers the axis-angle representation of the given quaternion, and
     * stores the result in the given vector. The axis is the direction of the
     * vector, and the angle, in radians, is the length of the vector.
     */
    public static final void axisAngle(Quatd q, Vec3d result)
    {
        double norm = Math.sqrt(q.x*q.x + q.y*q.y + q.z*q.z);
        if (norm < EPSILON)
        {
            result.set();
            return;
        }
        
        result.set(q.x, q.y, q.z);
        double angle = (2.0*Math.atan2(norm, q.w));
        Vec3d.mult(result, angle/norm, result);
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Static factory methods">
    /**
     * Returns a new instance of the identity quaternion.
     * 
     * @return A new quaternion containing the result.
     */
    public static final Quatd identity()
    {
        Quatd result = new Quatd();
        result.w = 1.0;
        return result;
    }
    
    /**
     * The rotation around the given axis, by the given angle as a new quaternion.
     * 
     * @param axis The axis around which to rotate.
     * @param angle The angle to rotate by.
     * @return A new quaternion containing the result.
     */
    public static final Quatd rotation(Vec3d axis, double angle)
    {
        Quatd result = new Quatd();
        rotation(axis, angle, result);
        return result;
    }
    
    /**
     * Returns a new quaternion representing the shortest rotation from the
     * starting direction vector to the end vector.
     * 
     * @param start The start direction.
     * @param end The end direction.
     * @return A new quaternion containing the result.
     */
    public static final Quatd rotation(Vec3d start, Vec3d end)
    {
        Quatd result = new Quatd();
        rotation(start, end, result);
        return result;
    }
    
    /**
     * Returns a new quaternion from the given rotation matrix.
     * 
     * @param m A rotation matrix.
     * @return A new quaternion containing the result.
     */
    public static final Quatd rotation(Mat3d m)
    {
        Quatd result = new Quatd();
        rotation(m, result);
        return result;
    }
            
    /**
     * Rotates {@code q} about the given {@code axis} by the given angle
     * {@code angle} and returns the result as a new quaternion. The axis must
     * be  normalized.
     * 
     * @param q The quaternion to rotate.
     * @param axis The axis to rotate around.
     * @param angle The angle to rotate by.
     * @return A new quaternion containing the result.
     */
    public static final Quatd rotate(Quatd q, Vec3d axis, double angle)
    {
        Quatd result = new Quatd();
        rotate(q, axis, angle, result);
        return result;
    }
    
    /**
     * Adds the two given quaternions and returns a new vector containing the
     * result.
     * 
     * @param q0 The first quaternion to add.
     * @param q1 The second quaternion to add.
     * @return A new quaternion containing the result.
     */
    public static final Quatd add(Quatd q0, Quatd q1)
    {
        Quatd result = new Quatd();
        add(q0, q1, result);
        return result;
    }
    
    /**
     * Subtracts {@code q1} from {@code q0} and returns a new vector containing
     * the result.
     * 
     * @param q0 The quaternion to subtract from.
     * @param q1 The quaternion to subtract by.
     * @return A new quaternion containing the result.
     */
    public static final Quatd sub(Quatd q0, Quatd q1)
    {
        Quatd result = new Quatd();
        sub(q0, q1, result);
        return result;
    }
    
    /**
     * Multiplies {@code q0} by {@code q1} and returns a new vector containing
     * the result.
     * 
     * @param q0 The left-hand quaternion to multiply.
     * @param q1 The right-hand quaternion to multiply by.
     * @return A new quaternion containing the result.
     */
    public static final Quatd mult(Quatd q0, Quatd q1)
    {
        Quatd result = new Quatd();
        mult(q0, q1, result);
        return result;
    }
    
    /**
     * Multiplies {@code q1} by {@code s}, adds {@code q0}, and returns a new
     * vector contain the result.
     * 
     * @param q0 The quaternion to add to.
     * @param q1 The quaternion to multiply by {@code s} and then add to {@code q0}.
     * @param s The scalar by which to multiply {@code q1}.
     * @return A new quaternion containing the result.
     */
    public static final Quatd madd(Quatd q0, Quatd q1, double s)
    {
        Quatd result = new Quatd();
        madd(q0, q1, s, result);
        return result;
    }
    
    /**
     * Multiplies the given quaternion by the given scalar and returns a new
     * vector containing the result.
     * 
     * @param q The quaternion to multiply.
     * @param s The scalar to multiply by.
     * @return A new quaternion containing the result.
     */
    public static final Quatd mult(Quatd q, double s)
    {
        Quatd result = new Quatd();
        mult(q, s, result);
        return result;
    }
    
    /**
     * Divides the given quaternion by the given scalar and returns a new vector
     * containing the result.
     * 
     * @param q The quaternion to multiply.
     * @param s The scalar to multiply by.
     * @return A new quaternion containing the result.
     */
    public static final Quatd div(Quatd q, double s)
    {
        Quatd result = new Quatd();
        div(q, s, result);
        return result;
    }
    
    /**
     * Negates the given quaternion and returns a new vector containing the result.
     * 
     * @param q The quaternion to negate.
     * @return A new quaternion containing the result.
     */
    public static final Quatd negate(Quatd q)
    {
        Quatd result = new Quatd();
        negate(q, result);
        return result;
    }
    
    /**
     * Calculates the conjugate of the given quaternion and returns a new vector
     * containing the result.
     * 
     * @param q The quaternion to conjugate.
     * @return A new quaternion containing the result.
     */
    public static final Quatd conjugate(Quatd q)
    {
        Quatd result = new Quatd();
        conjugate(q, result);
        return result;
    }
    
    /**
     * Normalizes the given quaternion and returns a new vector containing the result.
     * 
     * @param q The quaternion to normalize.
     * @return A new quaternion containing the result.
     */
    public static final Quatd normalize(Quatd q)
    {
        Quatd result = new Quatd();
        normalize(q, result);
        return result;
    }
    
    /**
     * Inverts the given quaternion and returns a new vector containing the result.
     * 
     * @param q The quaternion to invert.
     * @return A new quaternion containing the result.
     */
    public static final Quatd invert(Quatd q)
    {
        Quatd result = new Quatd();
        invert(q, result);
        return result;
    }
    
    /**
     * Linearly interpolates between the two given quaternions using the given
     * scalar and returns a new vector containing the result.
     * 
     * @param q0 The 'start' quaternion to interpolate from.
     * @param q1 The 'end' quaternion to interpolate to.
     * @param t The scalar interpolant, between zero and one (inclusive).
     * @return A new quaternion containing the result.
     */
    public static final Quatd lerp(Quatd q0, Quatd q1, double t)
    {
        Quatd result = new Quatd();
        lerp(q0, q1, t, result);
        return result;
    }
    
    /**
     * Performs a spherical linear interpolation between the two given
     * quaternions and returns a new vector containing the result.
     * 
     * @param q0 The 'start' quaternion to interpolate from.
     * @param q1 The 'end' quaternion to interpolate to.
     * @param t The scalar interpolant, between zero and one (inclusive).
     * @return A new quaternion containing the result.
     */
    public static final Quatd slerp(Quatd q0, Quatd q1, double t)
    {
        Quatd result = new Quatd();
        slerp(q0, q1, t, result);
        return result;
    }
    
    /**
     * Calculates the Tait–Bryan angles of this quaternion, and returns them in
     * the a new vector as pitch, yaw, and roll.
     * 
     * @param q The quaternion whose angles to calculate.
     * @return A new vector containing the result.
     */
    public static final Vec3d angles(Quatd q)
    {
        Vec3d result = new Vec3d();
        angles(q, result);
        return result;
    }
    // </editor-fold>
    
    public double w, x, y, z;
    
    /**
     * Creates a zero quaternion. NOT the identity quaternion.
     */
    public Quatd()
    {
    }
    
    public Quatd(double w, double x, double y, double z)
    {
        this.w = w; this.x = x; this.y = y; this.z = z;
    }
    
    public Quatd(Quatd q)
    {
        w = q.w; x = q.x; y = q.y; z = q.z;
    }

    public Quatd(Quat q)
    {
        w = q.w; x = q.x; y = q.y; z = q.z;
    }

    /**
     * Loads a new quaternion from the given buffer.
     *
     * @param buffer The buffer to read from.
     */
    public Quatd(ByteBuffer buffer)
    {
        Quatd.this.read(buffer);
    }

    /**
     * Loads a new quaternion from the given input stream.
     * 
     * @param in The input stream to read from.
     * @throws IOException If an io error occurred.
     */
    public Quatd(DataInputStream in) throws IOException
    {
        Quatd.this.read(in);
    }
    
    // <editor-fold defaultstate="collapsed" desc="Instance accessor methods">
    /**
     * Return the specified component of this quaternion.
     */
    public double getComponent(int component)
    {
        return getComponent(this, component);
    }

    /**
     * Returns the dot product of this and the given quaternion.
     * 
     * @param q The quaternion with which to calculate the dot product.
     * @return The dot product of this and the given quaternion.
     */
    public double dot(Quatd q)
    {
        return dot(this, q);
    }
    
    /**
     * Returns the square length of this quaternion.
     * 
     * @return The square length of this quaternion.
     */
    public double squareLength()
    {
        return squareLength(this);
    }
    
    /**
     * Returns the length of this quaternion.
     * 
     * @return The length of this quaternion.
     */
    public double length()
    {
        return length(this);
    }
    
    /**
     * Returns the Tait–Bryan angles of this quaternion as a new vector.
     * 
     * @return THe Tait-Bryan angles of this quaternion.
     */
    public Vec3d angles()
    {
        return angles(this);
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Instance mutator methods">
    /**
     * Sets this to the given quaternion.
     * 
     * @param q The quaternion to set this to.
     * @return This quaternion.
     */
    public Quatd set(Quatd q)
    {
        copy(q, this);
        return this;
    }
    
    /**
     * Sets the coordinates of this quaternion.
     * 
     * @return This quaternion.
     */
    public Quatd set(double w, double x, double y, double z)
    {
        this.w = w; this.x = x; this.y = y; this.z = z;
        return this;
    }
    
    /**
     * Sets this to the zero quaternion.
     * 
     * @return This quaternion.
     */
    public Quatd set()
    {
        x = 0.0; y = 0.0; z = 0.0;
        return this;
    }
    
    /**
     * Sets the component specified by the given index to the given double.
     */
    public Quatd setComponent(int component, double value)
    {
        setComponent(this, component, value);
        return this;
    }
    
    /**
     * Sets this to the identity quaternion.
     * 
     * @return This quaternion.
     */
    public Quatd setIdentity()
    {
        identity(this);
        return this;
    }
    
    /**
     * Sets this to the identity quaternion.
     * 
     * @param axis The axis around which to rotate.
     * @param angle The angle to rotate by.
     * @return This quaternion.
     */
    public Quatd setRotation(Vec3d axis, double angle)
    {
        rotation(axis, angle, this);
        return this;
    }
    
    /**
     * Sets this to the shortest rotation from the starting direction vector to
     * the end vector.
     * 
     * @param start The starting direction vector.
     * @param end The end direction vector.
     * @return This quaternion.
     */
    public Quatd setRotation(Vec3d start, Vec3d end)
    {
        rotation(start, end, this);
        return this;
    }
    
    /**
     * Sets this to the rotation of the given matrix.
     * 
     * @param mat A rotation matrix.
     * @return This quaternion.
     */
    public Quatd setRotation(Mat3d mat)
    {
        rotation(mat, this);
        return this;
    }
    
    /**
     * Rotates this quaternion around the given axis, by the given angle.
     * 
     * @param axis The axis around which to rotate.
     * @param angle The angle to rotate by.
     * @return This quaternion.
     */
    public Quatd rotate(Vec3d axis, double angle)
    {
        rotate(this, axis, angle, this);
        return this;
    }
    
    /**
     * Adds the given quaternion to this.
     * 
     * @param q The quaternion to add.
     * @return This quaternion.
     */
    public Quatd add(Quatd q)
    {
        add(this, q, this);
        return this;
    }
    
    /**
     * Subtracts the given quaternion from this.
     * 
     * @param q The quaternion to subtract.
     * @return This quaternion.
     */
    public Quatd sub(Quatd q)
    {
        sub(this, q, this);
        return this;
    }
    
    /**
     * Multiplies this by the given quaternion.
     * 
     * @param q The right-hand quaternion to multiply by.
     * @return This quaternion.
     */
    public Quatd mult(Quatd q)
    {
        mult(this, q, this);
        return this;
    }
    
    /**
     * Multiplies the given quaternion by the given scalar, and adds the result
     * to this.
     * 
     * @param v The quaternion to multiply-add.
     * @param s The scalar to multiply {@code v} by.
     * @return This quaternion.
     */
    public Quatd madd(Quatd v, double s)
    {
        madd(this, v, s, this);
        return this;
    }
    
    /**
     * Multiplies this by the given scalar.
     * 
     * @param s The given scalar to multiply by.
     * @return This quaternion.
     */
    public Quatd mult(double s)
    {
        mult(this, s, this);
        return this;
    }
    
    /**
     * Divides this by the given scalar.
     * 
     * @param s The given scalar to divide by.
     * @return This quaternion.
     */
    public Quatd div(double s)
    {
        div(this, s, this);
        return this;
    }
    
    /**
     * Negates this quaternion.
     * 
     * @return This quaternion.
     */
    public Quatd negate()
    {
        negate(this, this);
        return this;
    }
    
    /**
     * Conjugates this quaternion.
     * 
     * @return This quaternion.
     */
    public Quatd conjugate()
    {
        conjugate(this, this);
        return this;
    }
    
    /**
     * Normalizes this quaternion.
     * 
     * @return This quaternion.
     */
    public Quatd normalize()
    {
        normalize(this, this);
        return this;
    }
    
    /**
     * Inverts this quaternion.
     * 
     * @return This quaternion.
     */
    public Quatd invert()
    {
        invert(this, this);
        return this;
    }
    
    /**
     * Linearly interpolates this towards the given quaternion using the given
     * scalar interpolant.
     * 
     * @param q The 'end' quaternion to interpolate to.
     * @param t The scalar interpolant, between zero and one (inclusive).
     * @return This quaternion.
     */
    public Quatd lerp(Quatd q, double t)
    {
        lerp(this, q, t, this);
        return this;
    }
    
    /**
     * Spherical-linearly interpolates this towards the given quaternion using
     * the given scalar interpolant.
     * 
     * @param q The 'end' quaternion to interpolate to.
     * @param t The scalar interpolant, between zero and one (inclusive).
     * @return This quaternion.
     */
    public Quatd slerp(Quatd q, double t)
    {
        slerp(this, q, t, this);
        return this;
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Overriden/implemented methods">
    /**
     * WARNING: W buffered last instead of first, for GLSL.
     */
    @Override
    public void read(ByteBuffer buffer)
    {
        x = buffer.getDouble();
        y = buffer.getDouble();
        z = buffer.getDouble();
        w = buffer.getDouble();
    }

    @Override
    public void write(ByteBuffer buffer)
    {
        buffer.putDouble(x);
        buffer.putDouble(y);
        buffer.putDouble(z);
        buffer.putDouble(w);
    }

    @Override
    public int bufferSize()
    {
        return 8*4;
    }

    @Override
    public Quatd read(DataInputStream in) throws IOException
    {
        w = in.readDouble();
        x = in.readDouble();
        y = in.readDouble();
        z = in.readDouble();
        return this;
    }

    @Override
    public Quatd write(DataOutputStream out) throws IOException
    {
        out.writeDouble(w);
        out.writeDouble(x);
        out.writeDouble(y);
        out.writeDouble(z);
        return this;
    }
    
    @Override
    public String toString()
    {
        return "(" + w + ": " + x + ", " + y + ", " + z + ")";
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (o == null) return false;
        if (o.getClass() != this.getClass()) return false;
        final Quatd q = (Quatd)o;
        return q.w == w && q.x == x && q.y == y && q.z == z;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(w, x, y, z);
    }
    // </editor-fold>
}
