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

import com.samrj.devil.util.Bufferable;
import com.samrj.devil.util.DataStreamable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * 3D double vector class.
 * 
 * @author Samuel Johnson (SmashMaster)
 */
public class Vec3d implements Bufferable, DataStreamable<Vec3d>
{
    // <editor-fold defaultstate="collapsed" desc="Static accessor methods">
    /**
     * Returns the given component of a vector.
     * 
     * @param v The vector whose component to get.
     * @param i The component to get.
     * @return The component of the vector.
     */
    public double getComponent(Vec3d v, int i)
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
    public static final double dot(Vec3d v0, Vec3d v1)
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
    public static final double squareLength(Vec3d v)
    {
        return v.x*v.x + v.y*v.y + v.z*v.z;
    }
    
    /**
     * Returns the length of the given vector.
     * 
     * @param v The vector to calculate the length of.
     * @return The length of {@code v}.
     */
    public static final double length(Vec3d v)
    {
        return Math.sqrt(squareLength(v));
    }
    
    /**
     * Returns the square distance between two given vectors.
     *  
     * @param v0 The first vector.
     * @param v1 The second vector.
     * @return The square distance between {@code v0} and {@code v1}.
     */
    public static final double squareDist(Vec3d v0, Vec3d v1)
    {
        double dx = v1.x - v0.x;
        double dy = v1.y - v0.y;
        double dz = v1.z - v0.z;
        return dx*dx + dy*dy + dz*dz;
    }
    
    /**
     * Returns the distance between two given vectors.
     *  
     * @param v0 The first vector.
     * @param v1 The second vector.
     * @return The distance between {@code v0} and {@code v1}.
     */
    public static final double dist(Vec3d v0, Vec3d v1)
    {
        return Math.sqrt(squareDist(v0, v1));
    }
    
    /**
     * Returns the scalar projection of {@code v0} onto {@code v1}. {@code v1}
     * need not be normalized.
     * 
     * @param v0 The vector to project.
     * @param v1 The vector on which to project.
     * @return The scalar projection of {@code v0} onto {@code v1}.
     */
    public static final double scalarProject(Vec3d v0, Vec3d v1)
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
    public static final boolean isZero(Vec3d v, double threshold)
    {
        return Util.isZero(v.x, threshold) &&
               Util.isZero(v.y, threshold) &&
               Util.isZero(v.z, threshold);
    }
    
    /**
     * Returns whether the given vector is exactly zero.
     * 
     * @param v The vector to check.
     * @return Whether the given vector is zero.
     */
    public static final boolean isZero(Vec3d v)
    {
        return v.x == 0.0 && v.y == 0.0 && v.z == 0.0;
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Static mutator methods">
    /**
     * Copies {@code source} into {@code target}.
     * 
     * @param source The vector to copy.
     * @param target The vector in which to store the result.
     */
    public static final void copy(Vec3d source, Vec3d target)
    {
        target.x = source.x;
        target.y = source.y;
        target.z = source.z;
    }
    
    /**
     * Copies {@code source} into {@code target}.
     * 
     * @param source The vector to copy.
     * @param target The vector in which to store the result.
     */
    public static final void copy(Vec3 source, Vec3d target)
    {
        target.x = source.x;
        target.y = source.y;
        target.z = source.z;
    }
    
    /**
     * Copies {@code source} into {@code target}.
     * 
     * @param source The vector to copy.
     * @param target The vector in which to store the result.
     */
    public static final void copy(Vec3i source, Vec3d target)
    {
        target.x = source.x;
        target.y = source.y;
        target.z = source.z;
    }
    
    /**
     * Adds {@code v0} and {@code v1} and stores the result in {@code result}.
     * 
     * @param v0 The first addend.
     * @param v1 The second addend.
     * @param result The vector in which to store the sum of {@code v0} and {@code v1}.
     */
    public static final void add(Vec3d v0, Vec3d v1, Vec3d result)
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
    public static final void sub(Vec3d v0, Vec3d v1, Vec3d result)
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
    public static final void mult(Vec3d v, double s, Vec3d result)
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
    public static final void mult(Vec3d v0, Vec3d v1, Vec3d result)
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
    public static final void madd(Vec3d v0, Vec3d v1, double s, Vec3d result)
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
    public static final void mult(Vec3d v, Mat3d m, Vec3d result)
    {
        double x = m.a*v.x + m.b*v.y + m.c*v.z;
        double y = m.d*v.x + m.e*v.y + m.f*v.z;
        double z = m.g*v.x + m.h*v.y + m.i*v.z;
        result.x = x; result.y = y; result.z = z;
    }
    
    /**
     * Multiplies {@code v} by {@code m} and stores the result in {@code result}.
     * 
     * @param v The vector to multiply.
     * @param m The 4x4 matrix to multiply the vector by.
     * @param result The vector in which to store the result.
     */
    public static final void mult(Vec3d v, Mat4d m, Vec3d result)
    {
        double x = m.a*v.x + m.b*v.y + m.c*v.z + m.d;
        double y = m.e*v.x + m.f*v.y + m.g*v.z + m.h;
        double z = m.i*v.x + m.j*v.y + m.k*v.z + m.l;
        result.x = x; result.y = y; result.z = z;
    }
    
    /**
     * Multiplies {@code v} by {@code t} and stores the result in {@code result}.
     * 
     * @param v The vector to multiply.
     * @param t A transform to multiply by.
     * @param result The vector in which to store the result.
     */
    public static final void mult(Vec3d v, Transform t, Vec3d result)
    {
        mult(v, new Vec3d(t.sca), result);
        mult(result, new Quatd(t.rot), result);
        add(result, new Vec3d(t.pos), result);
    }
    
    /**
     * Rotates the given vector by the given quaternion, and stores the result
     * in {@code result}.
     * 
     * @param v The vector to rotate.
     * @param q The quaternion to rotate by.
     * @param result The vector in which to store the result.
     */
    public static final void mult(Vec3d v, Quatd q, Vec3d result)
    {
        Vec3d temp1 = new Vec3d(q.x, q.y, q.z);
        Vec3d temp2 = cross(temp1, v);
        mult(temp2, 2.0, temp2);
        
        copy(v, result);
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
    public static final void cross(Vec3d v0, Vec3d v1, Vec3d result)
    {
        double x = v0.y*v1.z - v0.z*v1.y;
        double y = v0.z*v1.x - v0.x*v1.z;
        double z = v0.x*v1.y - v0.y*v1.x;
        result.x = x; result.y = y; result.z = z;
    }
    
    /**
     * Divides {@code v} by {@code s} and stores the result in {@code result}.
     * 
     * @param v The vector to divide.
     * @param s The scalar to divide the vector by.
     * @param result The vector in which to store the result.
     */
    public static final void div(Vec3d v, double s, Vec3d result)
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
    public static final void div(Vec3d v0, Vec3d v1, Vec3d result)
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
    public static final void negate(Vec3d v, Vec3d result)
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
    public static final void normalize(Vec3d v, Vec3d result)
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
    public static final void reflect(Vec3d v, Vec3d n, Vec3d result)
    {
        double m = 2.0*dot(v, n);
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
    public static final void project(Vec3d v0, Vec3d v1, Vec3d result)
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
    public static final void reject(Vec3d v0, Vec3d v1, Vec3d result)
    {
        Vec3d temp = project(v0, v1);
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
    public static final void lerp(Vec3d v0, Vec3d v1, double t, Vec3d result)
    {
        result.x = Util.lerp(v0.x, v1.x, t);
        result.y = Util.lerp(v0.y, v1.y, t);
        result.z = Util.lerp(v0.z, v1.z, t);
    }
    
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
    public static final void move(Vec3d v, Vec3d dest, double dist, Vec3d result)
    {
        Vec3d dp = sub(dest, v);
        double d0 = length(dp);
        if (d0 <= dist) copy(dest, result);
        else madd(v, dp, dist/d0, result);
    }
    
    /**
     * Rotates {@code v} around the given axis, by the given angle, and stores
     * the result in {@code result}. Assumes the given axis is normalized.
     * 
     * @param v The vector to rotate.
     * @param axis The unit axis vector to rotate around.
     * @param angle The angle to rotate by, in radians.
     * @param result The vector in which to store the result.
     */
    public static final void rotate(Vec3d v, Vec3d axis, double angle, Vec3d result)
    {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        
        Vec3d temp = mult(v, cos);
        madd(temp, cross(axis, v), sin, temp);
        madd(temp, axis, dot(axis, v)*(1.0 - cos), result);
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Static factory methods">
    /**
     * Returns the sum of {@code v0} and {@code v1} in a new vector.
     * 
     * @param v0 The first addend.
     * @param v1 The second addend.
     * @return A new vector containing the result.
     */
    public static final Vec3d add(Vec3d v0, Vec3d v1)
    {
        Vec3d result = new Vec3d();
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
    public static final Vec3d sub(Vec3d v0, Vec3d v1)
    {
        Vec3d result = new Vec3d();
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
    public static final Vec3d mult(Vec3d v, double s)
    {
        Vec3d result = new Vec3d();
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
    public static final Vec3d mult(Vec3d v0, Vec3d v1)
    {
        Vec3d result = new Vec3d();
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
    public static final Vec3d madd(Vec3d v0, Vec3d v1, double s)
    {
        Vec3d result = new Vec3d();
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
    public static final Vec3d mult(Vec3d v, Mat3d m)
    {
        Vec3d result = new Vec3d();
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
    public static final Vec3d mult(Vec3d v, Mat4d m)
    {
        Vec3d result = new Vec3d();
        mult(v, m, result);
        return result;
    }
    
    /**
     * Multiplies {@code v} by {@code t} and returns the result in a new vector.
     * 
     * @param v The vector to multiply.
     * @param t The transform to multiply by.
     * @return A new vector containing the result.
     */
    public static final Vec3d mult(Vec3d v, Transform t)
    {
        Vec3d result = new Vec3d();
        mult(v, t, result);
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
    public static final Vec3d mult(Vec3d v, Quatd q)
    {
        Vec3d result = new Vec3d();
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
    public static final Vec3d cross(Vec3d v0, Vec3d v1)
    {
        Vec3d result = new Vec3d();
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
    public static final Vec3d div(Vec3d v, double s)
    {
        Vec3d result = new Vec3d();
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
    public static final Vec3d div(Vec3d v0, Vec3d v1)
    {
        Vec3d result = new Vec3d();
        div(v0, v1, result);
        return result;
    }
    
    /**
     * Negates the given vector and returns the result in a new vector.
     * 
     * @param v The vector to negate.
     * @return A new vector containing the result.
     */
    public static final Vec3d negate(Vec3d v)
    {
        Vec3d result = new Vec3d();
        negate(v, result);
        return result;
    }
    
    /**
     * Normalizes {@code v} and returns the result in a new vector.
     * 
     * @param v The vector to normalize.
     * @return The normalized vector.
     */
    public static final Vec3d normalize(Vec3d v)
    {
        Vec3d result = new Vec3d();
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
    public static final Vec3d reflect(Vec3d v, Vec3d n)
    {
        Vec3d result = new Vec3d();
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
    public static final Vec3d project(Vec3d v0, Vec3d v1)
    {
        Vec3d result = new Vec3d();
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
    public static final Vec3d reject(Vec3d v0, Vec3d v1)
    {
        Vec3d result = new Vec3d();
        reject(v0, v1, result);
        return result;
    }
    
    /**
     * Rotates {@code v} around the given axis, by the given angle, and returns
     * the result in a new vector. Assumes the given axis is normalized.
     * 
     * @param v The vector to rotate.
     * @param axis The unit axis vector to rotate around.
     * @param angle The angle to rotate by, in radians.
     * @return A new vector containing the result.
     */
    public static final Vec3d rotate(Vec3d v, Vec3d axis, double angle)
    {
        Vec3d result = new Vec3d();
        rotate(v, axis, angle, result);
        return result;
    }
    // </editor-fold>
    
    public double x, y, z;
    
    // <editor-fold defaultstate="collapsed" desc="Constructors">
    /**
     * Creates a new zero vector.
     */
    public Vec3d()
    {
    }
    
    /**
     * Creates a new vector with the given coordinates.
     */
    public Vec3d(double x, double y, double z)
    {
        this.x = x; this.y = y; this.z = z;
    }
    
    /**
     * Creates a new vector with each coordinate set to the given scalar.
     */
    public Vec3d(double s)
    {
        x = s; y = s; z = s;
    }
    
    /**
     * Copies the given vector.
     * 
     * @param v The vector to copy.
     */
    public Vec3d(Vec3d v)
    {
        x = v.x; y = v.y; z = v.z;
    }
    
    /**
     * Copies the given vector.
     * 
     * @param v The vector to copy.
     */
    public Vec3d(Vec3 v)
    {
        x = v.x; y = v.y; z = v.z;
    }
    
    /**
     * Converts the given integer vector to a new double vector.
     * 
     * @param v The vector to copy.
     */
    public Vec3d(Vec3i v)
    {
        x = v.x; y = v.y; z = v.z;
    }

    /**
     * Loads a new vector from the given buffer.
     *
     * @param buffer The buffer to read from.
     */
    public Vec3d(ByteBuffer buffer)
    {
        Vec3d.this.read(buffer);
    }
    
    /**
     * Loads a new vector from the given input stream.
     * 
     * @param in The input stream to read from.
     * @throws IOException If an io error occurred.
     */
    public Vec3d(DataInputStream in) throws IOException
    {
        Vec3d.this.read(in);
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Instance accessor methods">
    /**
     * Returns the dot product of this and the given vector.
     * 
     * @param v The vector with which to calculate the dot product.
     * @return The dot product of this and the given vector.
     */
    public double dot(Vec3d v)
    {
        return dot(this, v);
    }
    
    /**
     * Returns the square length of this vector.
     * 
     * @return The square length of this vector.
     */
    public double squareLength()
    {
        return squareLength(this);
    }
    
    /**
     * Returns the length of this vector.
     * 
     * @return the length of this vector.
     */
    public double length()
    {
        return length(this);
    }
    
    /**
     * Returns the square distance between this and the given vector.
     * 
     * @param v The vector to calculate the square distance from.
     * @return The square distance between this and the given vector.
     */
    public double squareDist(Vec3d v)
    {
        return squareDist(this, v);
    }
    
    /**
     * Returns the distance between this and the given vector.
     * 
     * @param v The vector to calculate the distance from.
     * @return The distance between this and the given vector.
     */
    public double dist(Vec3d v)
    {
        return dist(this, v);
    }
    
    /**
     * Returns the scalar projection of this onto the given vector.
     * 
     * @param v The vector on which to project.
     * @return The scalar projection of this onto the given vector.
     */
    public double scalarProject(Vec3d v)
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
    public boolean isZero(double threshold)
    {
        return isZero(this, threshold);
    }
    
    /**
     * Returns whether this vector is exactly zero.
     * 
     * @return Whether this vector is exactly zero.
     */
    public boolean isZero()
    {
        return isZero(this);
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Instance mutator methods">
    /**
     * Sets this to the given vector.
     * 
     * @param v The vector to set this to.
     * @return This vector.
     */
    public Vec3d set(Vec3d v)
    {
        copy(v, this);
        return this;
    }
    
    /**
     * Sets this to the given vector.
     * 
     * @param v The vector to set this to.
     * @return This vector.
     */
    public Vec3d set(Vec3 v)
    {
        copy(v, this);
        return this;
    }
    
    /**
     * Sets this to the given vector.
     * 
     * @param v The vector to set this to.
     * @return This vector.
     */
    public Vec3d set(Vec3i v)
    {
        copy(v, this);
        return this;
    }
    
    /**
     * Sets the coordinates of this vector.
     * 
     * @return This vector.
     */
    public Vec3d set(double x, double y, double z)
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
    public Vec3d set(double s)
    {
        x = s; y = s; z = s;
        return this;
    }
    
    /**
     * Sets this to the zero vector.
     * 
     * @return This vector.
     */
    public Vec3d set()
    {
        x = 0.0; y = 0.0; z = 0.0;
        return this;
    }
    
    /**
     * Sets the component specified by the given index to the given double.
     * 
     * @param i The index of the component to set.
     * @param value The value to set the component to.
     * @return This vector.
     */
    public Vec3d setComponent(int i, double value)
    {
        switch (i)
        {
            case 0: x = value; return this;
            case 1: y = value; return this;
            case 2: z = value; return this;
            default: throw new ArrayIndexOutOfBoundsException(i);
        }
    }
    
    /**
     * Adds the given vector to this.
     * 
     * @param v The vector to add to this.
     * @return This vector.
     */
    public Vec3d add(Vec3d v)
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
    public Vec3d sub(Vec3d v)
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
    public Vec3d mult(double s)
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
    public Vec3d mult(Vec3d v)
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
    public Vec3d madd(Vec3d v, double s)
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
    public Vec3d mult(Mat3d m)
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
    public Vec3d mult(Mat4d m)
    {
        mult(this, m, this);
        return this;
    }
    
    /**
     * Multiplies this by the given transform.
     * 
     * @param t The transform to multiply this by.
     * @return This vector.
     */
    public Vec3d mult(Transform t)
    {
        mult(this, t, this);
        return this;
    }
    
    /**
     * Rotates this vector by the given quaternion.
     * 
     * @param q The quaternion to rotate by.
     * @return This vector.
     */
    public Vec3d mult(Quatd q)
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
    public Vec3d cross(Vec3d v)
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
    public Vec3d div(double s)
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
    public Vec3d div(Vec3d v)
    {
        div(this, v, this);
        return this;
    }
    
    /**
     * Negates this vector.
     * 
     * @return This vector.
     */
    public Vec3d negate()
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
    public Vec3d normalize()
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
    public Vec3d reflect(Vec3d n)
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
    public Vec3d project(Vec3d v)
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
    public Vec3d reject(Vec3d v)
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
    public Vec3d lerp(Vec3d v, double t)
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
    public Vec3d move(Vec3d dest, double dist)
    {
        move(this, dest, dist, this);
        return this;
    }
    
    /**
     * Rotates this vector around the given axis, by the given angle. Assumes
     * the given axis is normalized.
     * 
     * @param axis The unit axis vector to rotate around.
     * @param angle The angle to rotate by, in radians.
     * @return This vector.
     */
    public Vec3d rotate(Vec3d axis, double angle)
    {
        rotate(this, axis, angle, this);
        return this;
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Overriden/implemented methods">
    @Override
    public void read(ByteBuffer buffer)
    {
        x = buffer.getDouble();
        y = buffer.getDouble();
        z = buffer.getDouble();
    }

    @Override
    public void write(ByteBuffer buffer)
    {
        buffer.putDouble(x);
        buffer.putDouble(y);
        buffer.putDouble(z);
    }
    
    @Override
    public int bufferSize()
    {
        return 3*8;
    }

    @Override
    public Vec3d read(DataInputStream in) throws IOException
    {
        x = in.readDouble();
        y = in.readDouble();
        z = in.readDouble();
        return this;
    }

    @Override
    public Vec3d write(DataOutputStream out) throws IOException
    {
        out.writeDouble(x);
        out.writeDouble(y);
        out.writeDouble(z);
        return this;
    }
    
    @Override
    public String toString()
    {
        return "(" + x + ", " + y + ", " + z + ")";
    }
    
    public boolean equals(Vec3d v)
    {
        if (v == null) return false;
        return x == v.x && y == v.y && z == v.z;
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (o == null) return false;
        if (o.getClass() != this.getClass()) return false;
        final Vec3d v = (Vec3d)o;
        return equals(v);
    }
    
    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 79 * hash + (int)(Double.doubleToLongBits(this.x) ^ (Double.doubleToLongBits(this.x) >>> 32));
        hash = 79 * hash + (int)(Double.doubleToLongBits(this.y) ^ (Double.doubleToLongBits(this.y) >>> 32));
        hash = 79 * hash + (int)(Double.doubleToLongBits(this.z) ^ (Double.doubleToLongBits(this.z) >>> 32));
        return hash;
    }
    // </editor-fold>
}
