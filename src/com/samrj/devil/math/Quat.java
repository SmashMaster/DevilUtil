/*
 * Copyright (c) 2019 Sam Johnson
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
 * Quaternion class.
 * 
 * @author Samuel Johnson (SmashMaster)
 */
public class Quat implements FloatBufferable, DataStreamable
{
    // <editor-fold defaultstate="collapsed" desc="Static accessor methods">
    /**
     * Returns the dot product of the two given quaternions.
     * 
     * @param q0 The first quaternion to multiply.
     * @param q1 The second quaternion to multiply.
     * @return The dot product of the two given quaternions.
     */
    public static final float dot(Quat q0, Quat q1)
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
    public static final float squareLength(Quat q)
    {
        return q.w*q.w + q.x*q.x + q.y*q.y + q.z*q.z;
    }
    
    /**
     * Returns the length of the given quaternion.
     * 
     * @param q A quaternion.
     * @return The length of the given quaternion.
     */
    public static final float length(Quat q)
    {
        return (float)Math.sqrt(squareLength(q));
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Static mutator methods">
    /**
     *  Copies {@code source} into {@code target}.
     * 
     * @param source The quaternion to copy.
     * @param target The quaternion to copy into.
     */
    public static final void copy(Quat source, Quat target)
    {
        target.w = source.w;
        target.x = source.x;
        target.y = source.y;
        target.z = source.z;
    }
    
    /**
     * Sets the given quaternion to the identity rotation.
     * 
     * @param result The quaternion in which to store the result.
     */
    public static final void identity(Quat result)
    {
        result.w = 1.0f;
        result.x = 0.0f;
        result.y = 0.0f;
        result.z = 0.0f;
    }
    
    /**
     * Sets {@code result} to the rotation around the given axis, by the given
     * angle. The axis must be normalized.
     * 
     * @param axis The axis around which to rotate.
     * @param angle The angle to rotate by.
     * @param result The quaternion in which to store the result.
     */
    public static final void rotation(Vec3 axis, float angle, Quat result)
    {
        float a = angle*.5f;
        float sin = (float)Math.sin(a);
        
        result.w = (float)Math.cos(a);
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
    public static final void rotation(Vec3 start, Vec3 end, Quat result)
    {
        Vec3 v0 = Vec3.normalize(start);
        Vec3 v1 = Vec3.normalize(end);

        float dot = v0.dot(v1);
        if (Util.epsEqual(dot, 1.0f, 1<<6)) //this and v have same direction.
        {
            result.setIdentity();
            return;
        }
        
        //this and v have opposite direction. Rotate 180 degrees about an
        //arbitrary axis normal to this.
        if (Util.epsEqual(dot, -1.0f, 1<<6))
        {
            Vec3 axis = new Vec3(1.0f, 0.0f, 0.0f).cross(v0);
            //this lies along X axis and v is our opposite, so we can optimize.
            if (axis.x == 0.0f && axis.y == 0.0f && axis.z == 0.0f)
                result.set(0.0f, 0.0f, 0.0f, -1.0f);
            else result.setRotation(axis.normalize(), Util.PI);
            return;
        }
        
        v0.cross(v1);
        float s = (float)Math.sqrt(2f + dot*2f);
        result.set(s*0.5f, v0.x/s, v0.y/s, v0.z/s).normalize();
    }
    
    /**
     * Sets {@code result} to the rotation represented by the given matrix.
     * 
     * @param m A rotation matrix.
     * @param result The quaternion in which to store the result.
     */
    public static final void rotation(Mat3 m, Quat result)
    {
        float[] tr = {m.a + m.e + m.i,
                      m.a - m.e - m.i,
                      m.e - m.a - m.i,
                      m.i - m.a - m.e};
        
        int i = Util.maxdex(tr);
        float s = 2.0f*(float)Math.sqrt(1.0f + tr[i]);
        switch(i)
        {
            case 0: result.w = 0.25f*s;
                    result.x = (m.h - m.f)/s;
                    result.y = (m.c - m.g)/s;
                    result.z = (m.d - m.b)/s;
                    return;
            case 1: result.w = (m.h - m.f)/s;
                    result.x = 0.25f*s;
                    result.y = (m.b + m.d)/s;
                    result.z = (m.c + m.g)/s;
                    return;
            case 2: result.w = (m.c - m.g)/s;
                    result.x = (m.b + m.d)/s;
                    result.y = 0.25f*s;
                    result.z = (m.f + m.h)/s;
                    return;
            case 3: result.w = (m.d - m.b)/s;
                    result.x = (m.c + m.g)/s;
                    result.y = (m.f + m.h)/s;
                    result.z = 0.25f*s;
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
    public static final void rotate(Quat q, Vec3 axis, float angle, Quat result)
    {
        Quat temp = rotation(axis, angle);
        mult(q, temp, result);
    }
    
    /**
     * Adds the two given quaternions and stores the result in {@code result}.
     * 
     * @param q0 The first quaternion to add.
     * @param q1 The second quaternion to add.
     * @param result The quaternion in which to store the result.
     */
    public static final void add(Quat q0, Quat q1, Quat result)
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
    public static final void sub(Quat q0, Quat q1, Quat result)
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
    public static final void mult(Quat q0, Quat q1, Quat result)
    {
        float w = q0.w*q1.w - q0.x*q1.x - q0.y*q1.y - q0.z*q1.z;
        float x = q0.w*q1.x + q0.x*q1.w + q0.y*q1.z - q0.z*q1.y;
        float y = q0.w*q1.y - q0.x*q1.z + q0.y*q1.w + q0.z*q1.x;
        float z = q0.w*q1.z + q0.x*q1.y - q0.y*q1.x + q0.z*q1.w;
        
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
    public static final void madd(Quat v0, Quat v1, float s, Quat result)
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
    public static final void mult(Quat q, float s, Quat result)
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
    public static final void div(Quat q, float s, Quat result)
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
    public static final void negate(Quat q, Quat result)
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
    public static final void conjugate(Quat q, Quat result)
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
    public static final void normalize(Quat q, Quat result)
    {
        div(q, length(q), result);
    }
    
    /**
     * Inverts the given quaternion and stores the result in {@code result}.
     * 
     * @param q The quaternion to invert.
     * @param result The quaternion in which to store the result.
     */
    public static final void invert(Quat q, Quat result)
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
    public static final void lerp(Quat q0, Quat q1, float t, Quat result)
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
    public static final void slerp(Quat q0, Quat q1, float t, Quat result)
    {
        float dot = dot(q0, q1);
        if (dot < 0.0)
        {
            dot = -dot;
            q0 = negate(q0);
        }
        
        if (dot > 0.9995f) lerp(q0, q1, t, result);
        else
        {
            float ang = (float)Math.acos(dot);
            mult(q0, (float)Math.sin((1.0f - t)*ang), result);
            madd(result, q1, (float)Math.sin(t*ang), result);
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
    public static final void angles(Quat q, Vec3 result)
    {
        result.x = (float)Math.atan2(2.0f*(q.w*q.x - q.y*q.z), 1.0f - 2.0f*(q.z*q.z + q.x*q.x));
        result.y = (float)Math.atan2(2.0f*(q.w*q.y - q.z*q.x), 1.0f - 2.0f*(q.y*q.y + q.z*q.z));
        result.z = (float)Math.asin(2.0f*(q.x*q.y - q.w*q.z));
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Static factory methods">
    /**
     * Returns a new instance of the identity quaternion.
     * 
     * @return A new quaternion containing the result.
     */
    public static final Quat identity()
    {
        Quat result = new Quat();
        result.w = 1.0f;
        return result;
    }
    
    /**
     * The rotation around the given axis, by the given angle as a new quaternion.
     * 
     * @param axis The axis around which to rotate.
     * @param angle The angle to rotate by.
     * @return A new quaternion containing the result.
     */
    public static final Quat rotation(Vec3 axis, float angle)
    {
        Quat result = new Quat();
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
    public static final Quat rotation(Vec3 start, Vec3 end)
    {
        Quat result = new Quat();
        rotation(start, end, result);
        return result;
    }
    
    /**
     * Returns a new quaternion from the given rotation matrix.
     * 
     * @param m A rotation matrix.
     * @return A new quaternion containing the result.
     */
    public static final Quat rotation(Mat3 m)
    {
        Quat result = new Quat();
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
    public static final Quat rotate(Quat q, Vec3 axis, float angle)
    {
        Quat result = new Quat();
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
    public static final Quat add(Quat q0, Quat q1)
    {
        Quat result = new Quat();
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
    public static final Quat sub(Quat q0, Quat q1)
    {
        Quat result = new Quat();
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
    public static final Quat mult(Quat q0, Quat q1)
    {
        Quat result = new Quat();
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
    public static final Quat madd(Quat q0, Quat q1, float s)
    {
        Quat result = new Quat();
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
    public static final Quat mult(Quat q, float s)
    {
        Quat result = new Quat();
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
    public static final Quat div(Quat q, float s)
    {
        Quat result = new Quat();
        div(q, s, result);
        return result;
    }
    
    /**
     * Negates the given quaternion and returns a new vector containing the result.
     * 
     * @param q The quaternion to negate.
     * @return A new quaternion containing the result.
     */
    public static final Quat negate(Quat q)
    {
        Quat result = new Quat();
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
    public static final Quat conjugate(Quat q)
    {
        Quat result = new Quat();
        conjugate(q, result);
        return result;
    }
    
    /**
     * Normalizes the given quaternion and returns a new vector containing the result.
     * 
     * @param q The quaternion to normalize.
     * @return A new quaternion containing the result.
     */
    public static final Quat normalize(Quat q)
    {
        Quat result = new Quat();
        normalize(q, result);
        return result;
    }
    
    /**
     * Inverts the given quaternion and returns a new vector containing the result.
     * 
     * @param q The quaternion to invert.
     * @return A new quaternion containing the result.
     */
    public static final Quat invert(Quat q)
    {
        Quat result = new Quat();
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
    public static final Quat lerp(Quat q0, Quat q1, float t)
    {
        Quat result = new Quat();
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
    public static final Quat slerp(Quat q0, Quat q1, float t)
    {
        Quat result = new Quat();
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
    public static final Vec3 angles(Quat q)
    {
        Vec3 result = new Vec3();
        angles(q, result);
        return result;
    }
    // </editor-fold>
    
    public float w, x, y, z;
    
    /**
     * Creates a zero quaternion. NOT the identity quaternion.
     */
    public Quat()
    {
    }
    
    public Quat(float w, float x, float y, float z)
    {
        this.w = w; this.x = x; this.y = y; this.z = z;
    }
    
    public Quat(Quat q)
    {
        w = q.w; x = q.x; y = q.y; z = q.z;
    }
    
    /**
     * Loads a new quaternion from the given input stream.
     * 
     * @param in The input stream to read from.
     * @throws IOException If an io error occurred.
     */
    public Quat(DataInputStream in) throws IOException
    {
        Quat.this.read(in);
    }
    
    // <editor-fold defaultstate="collapsed" desc="Instance accessor methods">
    /**
     * Returns the dot product of this and the given quaternion.
     * 
     * @param q The quaternion with which to calculate the dot product.
     * @return The dot product of this and the given quaternion.
     */
    public float dot(Quat q)
    {
        return dot(this, q);
    }
    
    /**
     * Returns the square length of this quaternion.
     * 
     * @return The square length of this quaternion.
     */
    public float squareLength()
    {
        return squareLength(this);
    }
    
    /**
     * Returns the length of this quaternion.
     * 
     * @return The length of this quaternion.
     */
    public float length()
    {
        return length(this);
    }
    
    /**
     * Returns the Tait–Bryan angles of this quaternion as a new vector.
     * 
     * @return THe Tait-Bryan angles of this quaternion.
     */
    public Vec3 angles()
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
    public Quat set(Quat q)
    {
        copy(q, this);
        return this;
    }
    
    /**
     * Sets the coordinates of this quaternion.
     * 
     * @return This quaternion.
     */
    public Quat set(float w, float x, float y, float z)
    {
        this.w = w; this.x = x; this.y = y; this.z = z;
        return this;
    }
    
    /**
     * Sets this to the zero quaternion.
     * 
     * @return This quaternion.
     */
    public Quat set()
    {
        x = 0.0f; y = 0.0f; z = 0.0f;
        return this;
    }
    
    /**
     * Sets the component specified by the given index to the given float.
     * 
     * @param i The index of the component to set.
     * @param f The value to set the component to.
     * @return This quaternion.
     */
    public Quat setComponent(int i, float f)
    {
        switch (i)
        {
            case 0: w = f; return this;
            case 1: x = f; return this;
            case 2: y = f; return this;
            case 3: z = f; return this;
            default: throw new ArrayIndexOutOfBoundsException();
        }
    }
    
    /**
     * Sets this to the identity quaternion.
     * 
     * @return This quaternion.
     */
    public Quat setIdentity()
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
    public Quat setRotation(Vec3 axis, float angle)
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
    public Quat setRotation(Vec3 start, Vec3 end)
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
    public Quat setRotation(Mat3 mat)
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
    public Quat rotate(Vec3 axis, float angle)
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
    public Quat add(Quat q)
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
    public Quat sub(Quat q)
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
    public Quat mult(Quat q)
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
    public Quat madd(Quat v, float s)
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
    public Quat mult(float s)
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
    public Quat div(float s)
    {
        div(this, s, this);
        return this;
    }
    
    /**
     * Negates this quaternion.
     * 
     * @return This quaternion.
     */
    public Quat negate()
    {
        negate(this, this);
        return this;
    }
    
    /**
     * Conjugates this quaternion.
     * 
     * @return This quaternion.
     */
    public Quat conjugate()
    {
        conjugate(this, this);
        return this;
    }
    
    /**
     * Normalizes this quaternion.
     * 
     * @return This quaternion.
     */
    public Quat normalize()
    {
        normalize(this, this);
        return this;
    }
    
    /**
     * Inverts this quaternion.
     * 
     * @return This quaternion.
     */
    public Quat invert()
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
    public Quat lerp(Quat q, float t)
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
    public Quat slerp(Quat q, float t)
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
        x = buffer.getFloat();
        y = buffer.getFloat();
        z = buffer.getFloat();
        w = buffer.getFloat();
    }

    @Override
    public void write(ByteBuffer buffer)
    {
        buffer.putFloat(x);
        buffer.putFloat(y);
        buffer.putFloat(z);
        buffer.putFloat(w);
    }
    
    @Override
    public void read(FloatBuffer buffer)
    {
        x = buffer.get();
        y = buffer.get();
        z = buffer.get();
        w = buffer.get();
    }

    @Override
    public void write(FloatBuffer buffer)
    {
        buffer.put(x);
        buffer.put(y);
        buffer.put(z);
        buffer.put(w);
    }
    
    @Override
    public int bufferSize()
    {
        return 4*4;
    }

    @Override
    public void read(DataInputStream in) throws IOException
    {
        w = in.readFloat();
        x = in.readFloat();
        y = in.readFloat();
        z = in.readFloat();
    }

    @Override
    public void write(DataOutputStream out) throws IOException
    {
        out.writeFloat(w);
        out.writeFloat(x);
        out.writeFloat(y);
        out.writeFloat(z);
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
        final Quat q = (Quat)o;
        return q.w == w && q.x == x && q.y == y && q.z == z;
    }
    
    @Override
    public int hashCode()
    {
        int hash = 177 + Float.floatToIntBits(this.w);
        hash = 59*hash + Float.floatToIntBits(this.x);
        hash = 59*hash + Float.floatToIntBits(this.y);
        return 59*hash + Float.floatToIntBits(this.z);
    }
    // </editor-fold>
}
