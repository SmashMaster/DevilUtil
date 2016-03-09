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
 * Optimized three-dimensional vector class. The design of this class is based
 * around minimizing overhead whilst improving readability. This class is very
 * lightweight and, after JVM optimization, should have comparable performance
 * to most native libraries.
 * 
 * Many methods are static and final that wouldn't normally be. This is to
 * encourage JVM inlining, because instance methods can be polymorphic.
 * The class itself is not final, to allow for user extension.
 * 
 * Local accessor and mutator methods (with method chaining!) are provided for
 * user convenience, but the static methods should be used where optimal
 * performance is desired.
 * 
 * The data fields are public to increase readability and decrease overhead.
 * 
 * Manual inlining is avoided. The JVM should inline any code where enough time
 * is being spent.
 * 
 * No instance method should ever construct a new {@code Vec3}--a constructor or
 * static factory method must be called to do so.
 * 
 * @author Samuel Johnson (SmashMaster)
 */
public class Vec3 implements Bufferable, Streamable
{
    // <editor-fold defaultstate="collapsed" desc="Static accessor methods">
    /**
     * Returns the given component of a vector.
     * 
     * @param v The vector whose component to get.
     * @param i The component to get.
     * @return The component of the vector.
     */
    public float getComponent(Vec3 v, int i)
    {
        switch (i)
        {
            case 0: return v.x;
            case 1: return v.y;
            case 2: return v.z;
            default: throw new ArrayIndexOutOfBoundsException();
        }
    }
    
    /**
     * Returns the dot product of two given vectors.
     * 
     * @param v0 The first vector.
     * @param v1 The second vector.
     * @return The dot product of {@code v0} and {@code v1}.
     */
    public static final float dot(Vec3 v0, Vec3 v1)
    {
        return v0.x*v1.x + v0.y*v1.y + v0.z*v1.z;
    }
    
    /**
     * Returns the square length of the given vector. Can be alternately defined
     * as the dot product of the vector with itself.
     * 
     * @param v The vector to calculate the square length of.
     * @return The square length of {@code v}.
     */
    public static final float squareLength(Vec3 v)
    {
        return v.x*v.x + v.y*v.y + v.z*v.z;
    }
    
    /**
     * Returns the length of the given vector.
     * 
     * @param v The vector to calculate the length of.
     * @return The length of {@code v}.
     */
    public static final float length(Vec3 v)
    {
        return (float)Math.sqrt(squareLength(v));
    }
    
    /**
     * Returns the square distance between two given vectors.
     *  
     * @param v0 The first vector.
     * @param v1 The second vector.
     * @return The square distance between {@code v0} and {@code v1}.
     */
    public static final float squareDist(Vec3 v0, Vec3 v1)
    {
        float dx = v1.x - v0.x;
        float dy = v1.y - v0.y;
        float dz = v1.z - v0.z;
        return dx*dx + dy*dy + dz*dz;
    }
    
    /**
     * Returns the distance between two given vectors.
     *  
     * @param v0 The first vector.
     * @param v1 The second vector.
     * @return The distance between {@code v0} and {@code v1}.
     */
    public static final float dist(Vec3 v0, Vec3 v1)
    {
        return (float)Math.sqrt(squareDist(v0, v1));
    }
    
    /**
     * Returns the scalar projection of {@code v0} onto {@code v1}. {@code v1}
     * need not be normalized.
     * 
     * @param v0 The vector to project.
     * @param v1 The vector on which to project.
     * @return The scalar projection of {@code v0} onto {@code v1}.
     */
    public static final float scalarProject(Vec3 v0, Vec3 v1)
    {
        return dot(v0, v1)/length(v1);
    }
    
    /**
     * Returns whether the given vector is close to zero, such that each of its
     * components absolute values are smaller than the given threshold.
     * 
     * @param v The vector to check.
     * @param threshold The distance from zero the vector can be.
     * @return Whether the given vector is close to zero.
     */
    public static final boolean isZero(Vec3 v, float threshold)
    {
        return Util.isZero(v.x, threshold) &&
               Util.isZero(v.y, threshold) &&
               Util.isZero(v.z, threshold);
    }
    
    /**
     * Returns whether or not {@code v0} and {@code v1} are approximately equal,
     * based on their individual components epsilons and a tolerance factor.
     * 
     * @param v0 The first vector.
     * @param v1 The second vector.
     * @param tolerance The number of epsilons by which {@code v0} and {@code v1}
     *                  may differ and still be approximately equal.
     * @return Whether the two given vectors are approximately equal.
     * @see com.samrj.devil.math.Util#getEpsilon(float)
     */
    public static final boolean epsEqual(Vec3 v0, Vec3 v1, int tolerance)
    {
        return Util.epsEqual(v0.x, v1.x, tolerance) &&
               Util.epsEqual(v0.y, v1.y, tolerance) &&
               Util.epsEqual(v0.z, v1.z, tolerance);
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Static mutator methods">
    /**
     * Copies {@code source} into {@code target}.
     * 
     * @param source The vector to copy.
     * @param target The vector in which to store the result.
     */
    public static final void copy(Vec3 source, Vec3 target)
    {
        target.x = source.x;
        target.y = source.y;
        target.z = source.z;
    }
    
    /**
     * Sets a vector to the given row of a matrix.
     * 
     * @param m The matrix to copy from.
     * @param row A row of the matrix.
     * @param result The vector in which to store the result.
     */
    public static final void copyRow(Mat3 m, int row, Vec3 result)
    {
        switch (row)
        {
            case 0: result.x = m.a;
                    result.y = m.b;
                    result.z = m.c; return;
            case 1: result.x = m.d;
                    result.y = m.e;
                    result.z = m.f; return;
            case 2: result.x = m.g;
                    result.y = m.h;
                    result.z = m.i; return;
            default: throw new ArrayIndexOutOfBoundsException();
        }
    }
    
    /**
     * Sets a vector to the given column of a matrix.
     * 
     * @param m The matrix to copy from.
     * @param column A column of the matrix.
     * @param result The vector in which to store the result.
     */
    public static final void copyColumn(Mat3 m, int column, Vec3 result)
    {
        switch (column)
        {
            case 0: result.x = m.a;
                    result.y = m.d;
                    result.z = m.g; return;
            case 1: result.x = m.b;
                    result.y = m.e;
                    result.z = m.h; return;
            case 2: result.x = m.c;
                    result.y = m.f;
                    result.z = m.i; return;
            default: throw new ArrayIndexOutOfBoundsException();
        }
    }
    
    /**
     * Adds {@code v0} and {@code v1} and stores the result in {@code result}.
     * 
     * @param v0 The first addend.
     * @param v1 The second addend.
     * @param result The vector in which to store the sum of {@code v0} and {@code v1}.
     */
    public static final void add(Vec3 v0, Vec3 v1, Vec3 result)
    {
        result.x = v0.x + v1.x;
        result.y = v0.y + v1.y;
        result.z = v0.z + v1.z;
    }
    
    /**
     * Subtracts {@code v1} from {@code v0} and stores the result in {@code result}.
     * 
     * @param v0 The vector to subtract from.
     * @param v1 The vector to subtract by.
     * @param result The vector in which to store the result.
     */
    public static final void sub(Vec3 v0, Vec3 v1, Vec3 result)
    {
        result.x = v0.x - v1.x;
        result.y = v0.y - v1.y;
        result.z = v0.z - v1.z;
    }
    
    /**
     * Multiplies {@code v} by {@code s} and stores the result in {@code result}.
     * 
     * @param v The vector to multiply.
     * @param s The scalar to multiply the vector by.
     * @param result The vector in which to store the result.
     */
    public static final void mult(Vec3 v, float s, Vec3 result)
    {
        result.x = v.x*s;
        result.y = v.y*s;
        result.z = v.z*s;
    }
    
    /**
     * Multiplies each component of {@code v0} by the respective component of
     * {@code v1} and stores the result in {@code result}.
     * 
     * @param v0 The first vector to multiply.
     * @param v1 The second vector to multiply.
     * @param result The vector in which to store the result.
     */
    public static final void mult(Vec3 v0, Vec3 v1, Vec3 result)
    {
        result.x = v0.x*v1.x;
        result.y = v0.y*v1.y;
        result.z = v0.z*v1.z;
    }
    
    /**
     * Multiplies {@code v1} by {@code s}, adds {@code v0}, and stores the
     * result in {@code result}.
     * 
     * @param v0 The vector to add to.
     * @param v1 The vector to multiply by {@code s} and then add to {@code v0}.
     * @param s The scalar by which to multiply {@code v1}.
     * @param result The vector in which to store the result.
     */
    public static final void madd(Vec3 v0, Vec3 v1, float s, Vec3 result)
    {
        result.x = v0.x + v1.x*s;
        result.y = v0.y + v1.y*s;
        result.z = v0.z + v1.z*s;
    }
    
    /**
     * Multiplies {@code v} by {@code m} and stores the result in {@code result}.
     * 
     * @param v The vector to multiply.
     * @param m The 3x3 matrix to multiply the vector by.
     * @param result The vector in which to store the result.
     */
    public static final void mult(Vec3 v, Mat3 m, Vec3 result)
    {
        float x = m.a*v.x + m.b*v.y + m.c*v.z;
        float y = m.d*v.x + m.e*v.y + m.f*v.z;
        float z = m.g*v.x + m.h*v.y + m.i*v.z;
        result.x = x; result.y = y; result.z = z;
    }
    
    /**
     * Multiplies {@code v} by {@code m} and stores the result in {@code result}.
     * 
     * @param v The vector to multiply.
     * @param m The 4x4 matrix to multiply the vector by.
     * @param result The vector in which to store the result.
     */
    public static final void mult(Vec3 v, Mat4 m, Vec3 result)
    {
        float x = m.a*v.x + m.b*v.y + m.c*v.z + m.d;
        float y = m.e*v.x + m.f*v.y + m.g*v.z + m.h;
        float z = m.i*v.x + m.j*v.y + m.k*v.z + m.l;
        result.x = x; result.y = y; result.z = z;
    }
    
    /**
     * Rotates the given vector by the given quaternion, and stores the result
     * in {@code result}.
     * 
     * @param v The vector to rotate.
     * @param q The quaternion to rotate by.
     * @param result The vector in which to store the result.
     */
    public static final void mult(Vec3 v, Quat q, Vec3 result)
    {
        //t = 2 * cross(q.xyz, v)
        //v' = v + q.w * t + cross(q.xyz, t)
        
        Vec3 temp1 = new Vec3(q.x, q.y, q.z);
        Vec3 temp2 = cross(temp1, v);
        mult(temp2, 2.0f, temp2);
        
        copy(v, result); //v
        cross(temp1, temp2, temp1);
        add(result, temp1, result);
        mult(temp2, q.w, temp2);
        add(result, temp2, result);
    }
    
    /**
     * Calculates the cross product between {@code v0} and {@code v1} then stores
     * the result in {@code result}.
     * 
     * @param v0 The vector to multiply.
     * @param v1 The vector to multiply by.
     * @param result The vector in which to store the result.
     */
    public static final void cross(Vec3 v0, Vec3 v1, Vec3 result)
    {
        float x = v0.y*v1.z - v0.z*v1.y;
        float y = v0.z*v1.x - v0.x*v1.z;
        float z = v0.x*v1.y - v0.y*v1.x;
        result.x = x; result.y = y; result.z = z;
    }
    
    /**
     * Divides {@code v} by {@code s} and stores the result in {@code result}.
     * 
     * @param v The vector to divide.
     * @param s The scalar to divide the vector by.
     * @param result The vector in which to store the result.
     */
    public static final void div(Vec3 v, float s, Vec3 result)
    {
        result.x = v.x/s;
        result.y = v.y/s;
        result.z = v.z/s;
    }
    
    /**
     * Divides each component of {@code v0} by the respective component of
     * {@code v1} and stores the result in {@code result}.
     * 
     * @param v0 The vector to divide.
     * @param v1 The vector to divide by.
     * @param result The vector in which to store the result.
     */
    public static final void div(Vec3 v0, Vec3 v1, Vec3 result)
    {
        result.x = v0.x/v1.x;
        result.y = v0.y/v1.y;
        result.z = v0.z/v1.z;
    }
    
    /**
     * Negates the given vector and stores the result in {@code result}.
     * 
     * @param v The vector to negate.
     * @param result The vector in which to store the result.
     */
    public static final void negate(Vec3 v, Vec3 result)
    {
        result.x = -v.x;
        result.y = -v.y;
        result.z = -v.z;
    }
    
    /**
     * Sets the length of the given vector to one and stores the result in
     * {@code result}. Has undefined behavior if the length of the given vector
     * is zero.
     * 
     * @param v The vector to normalize.
     * @param result The vector in which to store the result.
     */
    public static final void normalize(Vec3 v, Vec3 result)
    {
        div(v, length(v), result);
    }
    
    /**
     * Reflects {@code v} about {@code n} and stores the result in {@code result}.
     * {@code n} must be normalized.
     * 
     * @param v The vector to reflect.
     * @param n The normal vector about which to reflect.
     * @param result The vector in which to store the result.
     */
    public static final void reflect(Vec3 v, Vec3 n, Vec3 result)
    {
        float m = 2.0f*dot(v, n);
        result.x = n.x*m - v.x;
        result.y = n.y*m - v.y;
        result.z = n.z*m - v.z;
    }
    
    /**
     * Performs a vector projection of {@code v0} onto {@code v1} and stores the
     * result in {@code result}. {@code v1} need not be normalized.
     * 
     * @param v0 The vector to project.
     * @param v1 The vector on which to project.
     * @param result The vector in which to store the result.
     */
    public static final void project(Vec3 v0, Vec3 v1, Vec3 result)
    {
        mult(v1, dot(v0, v1)/squareLength(v1), result);
    }
    
    /**
     * Performs a vector rejection of {@code v1} from {@code v0} and stores the
     * result in {@code result}.
     * 
     * @param v0 The vector to reject from.
     * @param v1 The vector to reject by.
     * @param result The vector in which to store the result.
     */
    public static final void reject(Vec3 v0, Vec3 v1, Vec3 result)
    {
        Vec3 temp = project(v0, v1);
        sub(v0, temp, result);
    }
    
    /**
     * Interpolates between the two given vectors using the given scalar, and
     * stores the result in {@code result}.
     * 
     * @param v0 The 'start' vector to interpolate from.
     * @param v1 The 'end' vector to interpolate to.
     * @param t The scalar interpolant, between zero and one (inclusive).
     * @param result The vector in which to store the result.
     */
    public static final void lerp(Vec3 v0, Vec3 v1, float t, Vec3 result)
    {
        result.x = Util.lerp(v0.x, v1.x, t);
        result.y = Util.lerp(v0.y, v1.y, t);
        result.z = Util.lerp(v0.z, v1.z, t);
    }
    // </editor-fold>
    
    /**
     * Linearly moves {@code v} towards the given destination, by specified
     * distance, and stores the result in {@code result}. If the starting
     * distance to the destination equals or is lower than {@code dist}, sets
     * result to {@code dest}.
     * 
     * @param v The vector to move.
     * @param dest The destination to move towards.
     * @param dist The distance to move by.
     * @param result The vector in which to store the result.
     */
    public static final void move(Vec3 v, Vec3 dest, float dist, Vec3 result)
    {
        Vec3 dp = sub(dest, v);
        float d0 = length(dp);
        if (d0 <= dist) copy(dest, result);
        else madd(v, dp, dist/d0, result);
    }
    // <editor-fold defaultstate="collapsed" desc="Static factory methods">
    /**
     * Returns the given row of a matrix in a new vector.
     * 
     * @param m The matrix to copy from.
     * @param row The row to get.
     * @return A new vector containing the result.
     */
    public static final Vec3 fromRow(Mat3 m, int row)
    {
        Vec3 result = new Vec3();
        copyRow(m, row, result);
        return result;
    }
    
    /**
     * Returns the given column of a matrix in a new vector.
     * 
     * @param m The matrix to copy from.
     * @param column The column to get.
     * @return A new vector containing the result.
     */
    public static final Vec3 fromColumn(Mat3 m, int column)
    {
        Vec3 result = new Vec3();
        copyColumn(m, column, result);
        return result;
    }
    
    /**
     * Returns the sum of {@code v0} and {@code v1} in a new vector.
     * 
     * @param v0 The first addend.
     * @param v1 The second addend.
     * @return A new vector containing the result.
     */
    public static final Vec3 add(Vec3 v0, Vec3 v1)
    {
        Vec3 result = new Vec3();
        add(v0, v1, result);
        return result;
    }
    
    /**
     * Subtracts {@code v1} from {@code v0} and returns the result in a new vector.
     * 
     * @param v0 The vector to subtract from.
     * @param v1 The vector to subtract by.
     * @return A new vector containing the result.
     */
    public static final Vec3 sub(Vec3 v0, Vec3 v1)
    {
        Vec3 result = new Vec3();
        sub(v0, v1, result);
        return result;
    }
    
    /**
     * Multiplies {@code v} by {@code s} and returns the result in a new vector.
     * 
     * @param v The vector to multiply.
     * @param s The scalar to multiply by.
     * @return A new vector containing the result.
     */
    public static final Vec3 mult(Vec3 v, float s)
    {
        Vec3 result = new Vec3();
        mult(v, s, result);
        return result;
    }
    
    /**
     * Multiplies each component of {@code v0} by the respective component of
     * {@code v1} and stores the result in a new vector.
     * 
     * @param v0 The first vector to multiply.
     * @param v1 The second vector to multiply.
     * @return A new vector containing the result.
     */
    public static final Vec3 mult(Vec3 v0, Vec3 v1)
    {
        Vec3 result = new Vec3();
        mult(v0, v1, result);
        return result;
    }
    
    /**
     * Multiplies {@code v1} by {@code s}, adds {@code v0}, and returns a new
     * vector containing the result.
     * 
     * @param v0 The vector to add to.
     * @param v1 The vector to multiply by {@code s} and then add to {@code v0}.
     * @param s The scalar by which to multiply {@code v1}.
     * @return A new vector containing the result.
     */
    public static final Vec3 madd(Vec3 v0, Vec3 v1, float s)
    {
        Vec3 result = new Vec3();
        madd(v0, v1, s, result);
        return result;
    }
    
    /**
     * Multiplies {@code v} by {@code m} and returns the result in a new vector.
     * 
     * @param v The vector to multiply.
     * @param m The 3x3 matrix to multiply by.
     * @return A new vector containing the result.
     */
    public static final Vec3 mult(Vec3 v, Mat3 m)
    {
        Vec3 result = new Vec3();
        mult(v, m, result);
        return result;
    }
    
    /**
     * Multiplies {@code v} by {@code m} and returns the result in a new vector.
     * 
     * @param v The vector to multiply.
     * @param m The 4x4 matrix to multiply by.
     * @return A new vector containing the result.
     */
    public static final Vec3 mult(Vec3 v, Mat4 m)
    {
        Vec3 result = new Vec3();
        mult(v, m, result);
        return result;
    }
    
    /**
     * Rotates the given vector by the given quaternion and returns the result
     * in a new vector.
     * 
     * @param v The vector to rotate.
     * @param q The quaternion to rotate by.
     * @return A new vector containing the result.
     */
    public static final Vec3 mult(Vec3 v, Quat q)
    {
        Vec3 result = new Vec3();
        mult(v, q, result);
        return result;
    }
    
    /**
     * Calculates the cross product between {@code v0} and {@code v1} and returns
     * the result in a new vector.
     * 
     * @param v0 The vector to multiply.
     * @param v1 The vector to multiply by.
     * @return A new vector containing the result.
     */
    public static final Vec3 cross(Vec3 v0, Vec3 v1)
    {
        Vec3 result = new Vec3();
        cross(v0, v1, result);
        return result;
    }
    
    /**
     * Divides {@code v} by {@code s} and returns the result in a new vector.
     * 
     * @param v The vector to divide.
     * @param s The scalar to divide by.
     * @return A new vector containing the result.
     */
    public static final Vec3 div(Vec3 v, float s)
    {
        Vec3 result = new Vec3();
        div(v, s, result);
        return result;
    }
    
    /**
     * Divides each component of {@code v0} by the respective component of
     * {@code v1} and stores the result in a new vector.
     * 
     * @param v0 The vector to divide.
     * @param v1 The vector to divide by.
     * @return A new vector containing the result.
     */
    public static final Vec3 div(Vec3 v0, Vec3 v1)
    {
        Vec3 result = new Vec3();
        div(v0, v1, result);
        return result;
    }
    
    /**
     * Negates the given vector and returns the result in a new vector.
     * 
     * @param v The vector to negate.
     * @return A new vector containing the result.
     */
    public static final Vec3 negate(Vec3 v)
    {
        Vec3 result = new Vec3();
        negate(v, result);
        return result;
    }
    
    /**
     * Normalizes {@code v} and returns the result in a new vector.
     * 
     * @param v The vector to normalize.
     * @return The normalized vector.
     */
    public static final Vec3 normalize(Vec3 v)
    {
        Vec3 result = new Vec3();
        normalize(v, result);
        return result;
    }
    
    /**
     * Reflects {@code v} about {@code n} and returns the result in a new vector.
     * {@code n} must be normalized.
     * 
     * @param v The vector to reflect.
     * @param n The normal vector about which to reflect.
     * @return A new vector containing the result.
     */
    public static final Vec3 reflect(Vec3 v, Vec3 n)
    {
        Vec3 result = new Vec3();
        reflect(v, n, result);
        return result;
    }
    
    /**
     * Performs a vector projection of {@code v0} onto {@code v1} and returns
     * the result in a new vector. {@code v1} need not be normalized.
     * 
     * @param v0 The vector to project.
     * @param v1 The vector on which to project.
     * @return A new vector containing the result.
     */
    public static final Vec3 project(Vec3 v0, Vec3 v1)
    {
        Vec3 result = new Vec3();
        project(v0, v1, result);
        return result;
    }
    
    /**
     * Performs a vector rejection of {@code v1} from {@code v0} and returns the
     * result in a new vector.
     * 
     * @param v0 The vector to reject from.
     * @param v1 The vector to reject by.
     * @return A new vector containing the result.
     */
    public static final Vec3 reject(Vec3 v0, Vec3 v1)
    {
        Vec3 result = new Vec3();
        reject(v0, v1, result);
        return result;
    }
    
    /**
     * Interpolates between the two given vectors using the given scalar and
     * returns a new vector containing the result.
     * 
     * @param v0 The 'start' vector to interpolate from.
     * @param v1 The 'end' vector to interpolate to.
     * @param t The scalar interpolant, between zero and one (inclusive).
     * @return A new vector containing the result.
     */
    public static final Vec3 lerp(Vec3 v0, Vec3 v1, float t)
    {
        Vec3 result = new Vec3();
        lerp(v0, v1, t, result);
        return result;
    }
    
    /**
     * Linearly moves {@code v} towards the given destination, by specified
     * distance, and returns the result in a new vector. If the starting
     * distance to the destination equals or is lower than {@code dist}, sets
     * the result to {@code dest}.
     * 
     * @param v The vector to move.
     * @param dest The destination to move towards.
     * @param dist The distance to move by.
     * @return A new vector containing the result.
     */
    public static final Vec3 move(Vec3 v, Vec3 dest, float dist)
    {
        Vec3 result = new Vec3();
        move(v, dest, dist, result);
        return result;
    }
    // </editor-fold>
    
    public float x, y, z;
    
    // <editor-fold defaultstate="collapsed" desc="Constructors">
    /**
     * Creates a new zero vector.
     */
    public Vec3()
    {
    }
    
    /**
     * Creates a new vector with the given coordinates.
     */
    public Vec3(float x, float y, float z)
    {
        this.x = x; this.y = y; this.z = z;
    }
    
    /**
     * Creates a new vector with the given data. Each coordinate is set to one
     * of the given values, in order.
     */
    public Vec3(float x, Vec2 v)
    {
        this.x = x; y = v.x; z = v.y;
    }
    
    /**
     * Creates a new vector with the given data. Each coordinate is set to one
     * of the given values, in order.
     */
    public Vec3(Vec2 v, float z)
    {
        x = v.x; y = v.y; this.z = z;
    }
    
    /**
     * Creates a new vector with each coordinate set to the given scalar.
     */
    public Vec3(float s)
    {
        x = s; y = s; z = s;
    }
    
    /**
     * Copies the given vector.
     * 
     * @param v The vector to copy.
     */
    public Vec3(Vec3 v)
    {
        x = v.x; y = v.y; z = v.z;
    }
    
    /**
     * Loads a new vector from the given input stream.
     * 
     * @param in The input stream to read from.
     * @throws IOException If an io error occurred.
     */
    public Vec3(DataInputStream in) throws IOException
    {
        Vec3.this.read(in);
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Instance accessor methods">
    /**
     * Gets the component of this vector specified by the given index.
     * 
     * @param i The index of the component to get.
     * @return The component.
     */
    public float getComponent(int i)
    {
        return getComponent(this, i);
    }
    
    /**
     * Returns the dot product of this and the given vector.
     * 
     * @param v The vector with which to calculate the dot product.
     * @return The dot product of this and the given vector.
     */
    public float dot(Vec3 v)
    {
        return dot(this, v);
    }
    
    /**
     * Returns the square length of this vector.
     * 
     * @return The square length of this vector.
     */
    public float squareLength()
    {
        return squareLength(this);
    }
    
    /**
     * Returns the length of this vector.
     * 
     * @return the length of this vector.
     */
    public float length()
    {
        return length(this);
    }
    
    /**
     * Returns the square distance between this and the given vector.
     * 
     * @param v The vector to calculate the square distance from.
     * @return The square distance between this and the given vector.
     */
    public float squareDist(Vec3 v)
    {
        return squareDist(this, v);
    }
    
    /**
     * Returns the distance between this and the given vector.
     * 
     * @param v The vector to calculate the distance from.
     * @return The distance between this and the given vector.
     */
    public float dist(Vec3 v)
    {
        return dist(this, v);
    }
    
    /**
     * Returns the scalar projection of this onto the given vector.
     * 
     * @param v The vector on which to project.
     * @return The scalar projection of this onto the given vector.
     */
    public float scalarProject(Vec3 v)
    {
        return scalarProject(this, v);
    }
    
    /**
     * Returns whether this vector is close to zero, such that each of its
     * components absolute values are smaller than the given threshold.
     * 
     * @param threshold The distance from zero the vector can be.
     * @return Whether this vector is close to zero.
     */
    public boolean isZero(float threshold)
    {
        return isZero(this, threshold);
    }
    
    /**
     * Returns whether or not this and {@code v} are approximately equal,
     * based on their individual components epsilons and a tolerance factor.
     * 
     * @param v A vector.
     * @param tolerance The number of epsilons by which this and and {@code v}
     *                  may differ and still be approximately equal.
     * @return Whether this is approximately equal to the given vector.
     * @see com.samrj.devil.math.Util#getEpsilon(float)
     */
    public boolean epsEqual(Vec3 v, int tolerance)
    {
        return epsEqual(this, v, tolerance);
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Instance mutator methods">
    /**
     * Sets this to the given vector.
     * 
     * @param v The vector to set this to.
     * @return This vector.
     */
    public Vec3 set(Vec3 v)
    {
        copy(v, this);
        return this;
    }
    
    /**
     * Sets the coordinates of this vector.
     * 
     * @return This vector.
     */
    public Vec3 set(float x, float y, float z)
    {
        this.x = x; this.y = y; this.z = z;
        return this;
    }
    
    /**
     * Sets each component of this vector to the given scalar.
     * 
     * @param s The scalar to set this to.
     * @return This vector.
     */
    public Vec3 set(float s)
    {
        x = s; y = s; z = s;
        return this;
    }
    
    /**
     * Sets this to the zero vector.
     * 
     * @return This vector.
     */
    public Vec3 set()
    {
        x = 0.0f; y = 0.0f; z = 0.0f;
        return this;
    }
    
    /**
     * Sets the component specified by the given index to the given float.
     * 
     * @param i The index of the component to set.
     * @param f The value to set the component to.
     * @return This vector.
     */
    public Vec3 setComponent(int i, float f)
    {
        switch (i)
        {
            case 0: x = f; return this;
            case 1: y = f; return this;
            case 2: z = f; return this;
            default: throw new ArrayIndexOutOfBoundsException();
        }
    }
    
    /**
     * Copies the given row of a matrix into this vector.
     * 
     * @param m The matrix to copy from.
     * @param row The row to copy.
     * @return This vector.
     */
    public Vec3 setAsRow(Mat3 m, int row)
    {
        copyRow(m, row, this);
        return this;
    }
    
    /**
     * Copies the given row of a matrix into this vector.
     * 
     * @param m The matrix to copy from.
     * @param column The column to copy.
     * @return This vector.
     */
    public Vec3 setAsColumn(Mat3 m, int column)
    {
        copyColumn(m, column, this);
        return this;
    }
    
    /**
     * Adds the given vector to this.
     * 
     * @param v The vector to add to this.
     * @return This vector.
     */
    public Vec3 add(Vec3 v)
    {
        add(this, v, this);
        return this;
    }
    
    /**
     * Subtracts the given vector from this.
     * 
     * @param v The vector to subtract from this.
     * @return This vector.
     */
    public Vec3 sub(Vec3 v)
    {
        sub(this, v, this);
        return this;
    }
    
    /**
     * Multiplies this by the given scalar.
     * 
     * @param s The scalar to multiply this by.
     * @return This vector.
     */
    public Vec3 mult(float s)
    {
        mult(this, s, this);
        return this;
    }
    
    /**
     * Multiplies each component of this by the respective component of the
     * given vector.
     * 
     * @param v The vector to multiply this by.
     * @return This vector.
     */
    public Vec3 mult(Vec3 v)
    {
        mult(this, v, this);
        return this;
    }
    
    /**
     * Multiplies the given vector by the given scalar, and adds the result to
     * this.
     * 
     * @param v The vector to multiply-add.
     * @param s The scalar to multiply {@code v} by.
     * @return This vector.
     */
    public Vec3 madd(Vec3 v, float s)
    {
        madd(this, v, s, this);
        return this;
    }
    
    /**
     * Multiplies this by the given 3x3 matrix.
     * 
     * @param m The 3x3 matrix to multiply this by.
     * @return This vector.
     */
    public Vec3 mult(Mat3 m)
    {
        mult(this, m, this);
        return this;
    }
    
    /**
     * Multiplies this by the given 4x4 matrix.
     * 
     * @param m The 4x4 matrix to multiply this by.
     * @return This vector.
     */
    public Vec3 mult(Mat4 m)
    {
        mult(this, m, this);
        return this;
    }
    
    /**
     * Rotates this vector by the given quaternion.
     * 
     * @param q The quaternion to rotate by.
     * @return This vector.
     */
    public Vec3 mult(Quat q)
    {
        mult(this, q, this);
        return this;
    }
    
    /**
     * Sets this to the cross product between this and the given vector.
     * 
     * @param v The vector to multiply this by.
     * @return This vector.
     */
    public Vec3 cross(Vec3 v)
    {
        cross(this, v, this);
        return this;
    }
    
    /**
     * Divides this by the given scalar.
     * 
     * @param s The scalar to divide by.
     * @return This vector.
     */
    public Vec3 div(float s)
    {
        div(this, s, this);
        return this;
    }
    
    /**
     * Divides each component of this by the respective component of the
     * given vector.
     * 
     * @param v The vector to divide this by.
     * @return This vector.
     */
    public Vec3 div(Vec3 v)
    {
        div(this, v, this);
        return this;
    }
    
    /**
     * Negates this vector.
     * 
     * @return This vector.
     */
    public Vec3 negate()
    {
        negate(this, this);
        return this;
    }
    
    /**
     * Sets the length of this to one. Has undefined behavior if the current
     * length of this is zero or close to zero.
     * 
     * @return This vector.
     */
    public Vec3 normalize()
    {
        normalize(this, this);
        return this;
    }
    
    /**
     * Reflects this about the normalized vector {@code n}.
     * 
     * @param n The normal vector about which to reflect.
     * @return This vector.
     */
    public Vec3 reflect(Vec3 n)
    {
        reflect(this, n, this);
        return this;
    }
    
    /**
     * Projects this onto the given vector.
     * 
     * @param v The vector on which to project.
     * @return This vector.
     */
    public Vec3 project(Vec3 v)
    {
        project(this, v, this);
        return this;
    }
    
    /**
     * Rejects the given vector from this.
     * 
     * @param v The vector to reject from this.
     * @return This vector.
     */
    public Vec3 reject(Vec3 v)
    {
        reject(this, v, this);
        return this;
    }
    
    /**
     * Interpolates this towards the given vector with the given scalar
     * interpolant.
     * 
     * @param v The 'end' vector to interpolate to.
     * @param t The scalar interpolant, between zero and one (inclusive).
     * @return This vector.
     */
    public Vec3 lerp(Vec3 v, float t)
    {
        lerp(this, v, t, this);
        return this;
    }
    
    /**
     * Moves this vector towards the given destination by the specified
     * distance. If the starting distance to the destination is lesser than
     * or equal to that distance, sets this vector to the destination.
     * 
     * @param dest The destination to move towards.
     * @param dist The distance to move by.
     * @return This vector.
     */
    public Vec3 move(Vec3 dest, float dist)
    {
        move(this, dest, dist, this);
        return this;
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Overriden/implemented methods">
    @Override
    public void read(ByteBuffer buffer)
    {
        x = buffer.getFloat();
        y = buffer.getFloat();
        z = buffer.getFloat();
    }

    @Override
    public void write(ByteBuffer buffer)
    {
        buffer.putFloat(x);
        buffer.putFloat(y);
        buffer.putFloat(z);
    }
    
    @Override
    public int bufferSize()
    {
        return 3*4;
    }

    @Override
    public void read(DataInputStream in) throws IOException
    {
        x = in.readFloat();
        y = in.readFloat();
        z = in.readFloat();
    }

    @Override
    public void write(DataOutputStream out) throws IOException
    {
        out.writeFloat(x);
        out.writeFloat(y);
        out.writeFloat(z);
    }
    
    @Override
    public String toString()
    {
        return "(" + x + ", " + y + ", " + z + ")";
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (o == null) return false;
        if (o.getClass() != this.getClass()) return false;
        final Vec3 v = (Vec3)o;
        return v.x == x && v.y == y && v.z == z;
    }
    
    @Override
    public int hashCode()
    {
        int hash = 161 + Float.floatToIntBits(this.x);
        hash = 23*hash + Float.floatToIntBits(this.y);
        return 23*hash + Float.floatToIntBits(this.z);
    }
    // </editor-fold>
}
