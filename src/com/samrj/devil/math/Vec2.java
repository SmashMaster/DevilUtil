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
 * 2D vector class.
 * 
 * @author Samuel Johnson (SmashMaster)
 */
public class Vec2 implements FloatBufferable, DataStreamable<Vec2>
{
    // <editor-fold defaultstate="collapsed" desc="Static accessor methods">
    /**
     * Returns the given component of a vector.
     * 
     * @param v The vector whose component to get.
     * @param i The component to get.
     * @return The component of the vector.
     */
    public float getComponent(Vec2 v, int i)
    {
        switch (i)
        {
            case 0: return v.x;
            case 1: return v.y;
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
    public static final float dot(Vec2 v0, Vec2 v1)
    {
        return v0.x*v1.x + v0.y*v1.y;
    }
    
    /**
     * Returns the z coordinate of the cross product of {@code v0} and {@code v1},
     * implicitly taking their z coordinates to be zero.
     * 
     * @param v0 The vector to multiply.
     * @param v1 The vector to multiply by.
     * @return The cross product of {@code v0} and {@code v1}.
     */
    public static final float cross(Vec2 v0, Vec2 v1)
    {
        return v0.x*v1.y - v0.y*v1.x;
    }
    
    /**
     * Returns the square length of the given vector. Can be alternately defined
     * as the dot product of the vector with itself.
     * 
     * @param v The vector to calculate the square length of.
     * @return The square length of {@code v}.
     */
    public static final float squareLength(Vec2 v)
    {
        return v.x*v.x + v.y*v.y;
    }
    
    /**
     * Returns the length of the given vector.
     * 
     * @param v The vector to calculate the length of.
     * @return The length of {@code v}.
     */
    public static final float length(Vec2 v)
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
    public static final float squareDist(Vec2 v0, Vec2 v1)
    {
        float dx = v1.x - v0.x;
        float dy = v1.y - v0.y;
        return dx*dx + dy*dy;
    }
    
    /**
     * Returns the distance between two given vectors.
     *  
     * @param v0 The first vector.
     * @param v1 The second vector.
     * @return The distance between {@code v0} and {@code v1}.
     */
    public static final float dist(Vec2 v0, Vec2 v1)
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
    public static final float scalarProject(Vec2 v0, Vec2 v1)
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
    public static final boolean isZero(Vec2 v, float threshold)
    {
        return Util.isZero(v.x, threshold) &&
               Util.isZero(v.y, threshold);
    }
    
    /**
     * Returns whether the given vector is exactly zero.
     * 
     * @param v The vector to check.
     * @return Whether the given vector is zero.
     */
    public static final boolean isZero(Vec2 v)
    {
        return v.x == 0.0f && v.y == 0.0f;
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Static mutator methods">
    /**
     * Copies {@code source} into {@code target}.
     * 
     * @param source The vector to copy from.
     * @param target The vector to copy into.
     */
    public static final void copy(Vec2 source, Vec2 target)
    {
        target.x = source.x;
        target.y = source.y;
    }
    
    /**
     * Copies {@code source} into {@code target}.
     * 
     * @param source The vector to copy from.
     * @param target The vector to copy into.
     */
    public static final void copy(Vec2i source, Vec2 target)
    {
        target.x = source.x;
        target.y = source.y;
    }
    
    /**
     * Sets a vector to the given row of a matrix.
     * 
     * @param m The matrix to copy from.
     * @param row A row of the matrix.
     * @param result The vector in which to store the result.
     */
    public static final void copyRow(Mat2 m, int row, Vec2 result)
    {
        switch (row)
        {
            case 0: result.x = m.a;
                    result.y = m.b; return;
            case 1: result.x = m.c;
                    result.y = m.d; return;
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
    public static final void copyColumn(Mat2 m, int column, Vec2 result)
    {
        switch (column)
        {
            case 0: result.x = m.a;
                    result.y = m.c; return;
            case 1: result.x = m.b;
                    result.y = m.d; return;
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
    public static final void add(Vec2 v0, Vec2 v1, Vec2 result)
    {
        result.x = v0.x + v1.x;
        result.y = v0.y + v1.y;
    }
    
    /**
     * Subtracts {@code v1} from {@code v0} and stores the result in {@code result}.
     * 
     * @param v0 The vector to subtract from.
     * @param v1 The vector to subtract by.
     * @param result The vector in which to store the result.
     */
    public static final void sub(Vec2 v0, Vec2 v1, Vec2 result)
    {
        result.x = v0.x - v1.x;
        result.y = v0.y - v1.y;
    }
    
    /**
     * Multiplies {@code v} by {@code s} and stores the result in {@code result}.
     * 
     * @param v The vector to multiply.
     * @param s The scalar to multiply the vector by.
     * @param result The vector in which to store the result.
     */
    public static final void mult(Vec2 v, float s, Vec2 result)
    {
        result.x = v.x*s;
        result.y = v.y*s;
    }
    
    /**
     * Multiplies each component of {@code v0} by the respective component of
     * {@code v1} and stores the result in {@code result}.
     * 
     * @param v0 The first vector to multiply.
     * @param v1 The second vector to multiply.
     * @param result The vector in which to store the result.
     */
    public static final void mult(Vec2 v0, Vec2 v1, Vec2 result)
    {
        result.x = v0.x*v1.x;
        result.y = v0.y*v1.y;
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
    public static final void madd(Vec2 v0, Vec2 v1, float s, Vec2 result)
    {
        result.x = v0.x + v1.x*s;
        result.y = v0.y + v1.y*s;
    }
    
    /**
     * Multiplies {@code v} by {@code m} and stores the result in {@code result}.
     * 
     * @param v The vector to multiply.
     * @param m The 2x2 matrix to multiply the vector by.
     * @param result The vector in which to store the result.
     */
    public static final void mult(Vec2 v, Mat2 m, Vec2 result)
    {
        float x = v.x*m.a + v.y*m.b;
        float y = v.x*m.c + v.y*m.d;
        result.x = x; result.y = y;
    }
    
    /**
     * Multiplies {@code v} by {@code m} and stores the result in {@code result}.
     * 
     * @param v The vector to multiply.
     * @param m The 3x3 matrix to multiply the vector by.
     * @param result The vector in which to store the result.
     */
    public static final void mult(Vec2 v, Mat3 m, Vec2 result)
    {
        float x = v.x*m.a + v.y*m.b + m.c;
        float y = v.x*m.d + v.y*m.e + m.f;
        result.x = x; result.y = y;
    }
    
    /**
     * Divides {@code v} by {@code s} and stores the result in {@code result}.
     * 
     * @param v The vector to divide.
     * @param s The scalar to divide the vector by.
     * @param result The vector in which to store the result.
     */
    public static final void div(Vec2 v, float s, Vec2 result)
    {
        result.x = v.x/s;
        result.y = v.y/s;
    }
    
    /**
     * Divides each component of {@code v0} by the respective component of
     * {@code v1} and stores the result in {@code result}.
     * 
     * @param v0 The vector to divide.
     * @param v1 The vector to divide by.
     * @param result The vector in which to store the result.
     */
    public static final void div(Vec2 v0, Vec2 v1, Vec2 result)
    {
        result.x = v0.x/v1.x;
        result.y = v0.y/v1.y;
    }
    
    /**
     * Negates the given vector and stores the result in {@code result}.
     * 
     * @param v The vector to negate.
     * @param result The vector in which to store the result.
     */
    public static final void negate(Vec2 v, Vec2 result)
    {
        result.x = -v.x;
        result.y = -v.y;
    }
    
    /**
     * Sets the length of the given vector to one and stores the result in
     * {@code result}. Has undefined behavior if the length of the given vector
     * is zero.
     * 
     * @param v The vector to normalize.
     * @param result The vector in which to store the result.
     */
    public static final void normalize(Vec2 v, Vec2 result)
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
    public static final void reflect(Vec2 v, Vec2 n, Vec2 result)
    {
        float m = 2.0f*dot(v, n);
        result.x = n.x*m - v.x;
        result.y = n.y*m - v.y;
    }
    
    /**
     * Performs a vector projection of {@code v0} onto {@code v1} and stores the
     * result in {@code result}. {@code v1} need not be normalized.
     * 
     * @param v0 The vector to project.
     * @param v1 The vector on which to project.
     * @param result The vector in which to store the result.
     */
    public static final void project(Vec2 v0, Vec2 v1, Vec2 result)
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
    public static final void reject(Vec2 v0, Vec2 v1, Vec2 result)
    {
        Vec2 temp = project(v0, v1);
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
    public static final void lerp(Vec2 v0, Vec2 v1, float t, Vec2 result)
    {
        result.x = Util.lerp(v0.x, v1.x, t);
        result.y = Util.lerp(v0.y, v1.y, t);
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
    public static final void move(Vec2 v, Vec2 dest, float dist, Vec2 result)
    {
        Vec2 dp = sub(dest, v);
        float d0 = length(dp);
        if (d0 <= dist) copy(dest, result);
        else madd(v, dp, dist/d0, result);
    }
    
    /**
     * Rotates {@code v} around the origin by the given angle, and stores the
     * result in {@code result}.
     * 
     * @param v The vector to rotate.
     * @param angle The angle to rotate by.
     * @param result The vector in which to store the result.
     */
    public static final void rotate(Vec2 v, float angle, Vec2 result)
    {
        float cos = (float)Math.cos(angle);
        float sin = (float)Math.sin(angle);
        float x = v.x*cos - v.y*sin;
        float y = v.x*sin - v.y*cos;
        result.x = x;
        result.y = y;
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Static factory methods">
    /**
     * Returns the given row of a matrix in a new vector.
     * 
     * @param m The matrix to copy from.
     * @param row The row to get.
     * @return A new vector containing the result.
     */
    public static final Vec2 fromRow(Mat2 m, int row)
    {
        Vec2 result = new Vec2();
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
    public static final Vec2 fromColumn(Mat2 m, int column)
    {
        Vec2 result = new Vec2();
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
    public static final Vec2 add(Vec2 v0, Vec2 v1)
    {
        Vec2 result = new Vec2();
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
    public static final Vec2 sub(Vec2 v0, Vec2 v1)
    {
        Vec2 result = new Vec2();
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
    public static final Vec2 mult(Vec2 v, float s)
    {
        Vec2 result = new Vec2();
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
    public static final Vec2 mult(Vec2 v0, Vec2 v1)
    {
        Vec2 result = new Vec2();
        mult(v0, v1, result);
        return result;
    }
    
    /**
     * Multiplies {@code v1} by {@code s}, adds {@code v0}, and returns a new
     * vector contain the result.
     * 
     * @param v0 The vector to add to.
     * @param v1 The vector to multiply by {@code s} and then add to {@code v0}.
     * @param s The scalar by which to multiply {@code v1}.
     * @return A new vector containing the result.
     */
    public static final Vec2 madd(Vec2 v0, Vec2 v1, float s)
    {
        Vec2 result = new Vec2();
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
    public static final Vec2 mult(Vec2 v, Mat2 m)
    {
        Vec2 result = new Vec2();
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
    public static final Vec2 mult(Vec2 v, Mat3 m)
    {
        Vec2 result = new Vec2();
        mult(v, m, result);
        return result;
    }
    
    /**
     * Divides {@code v} by {@code s} and returns the result in a new vector.
     * 
     * @param v The vector to divide.
     * @param s The scalar to divide by.
     * @return A new vector containing the result.
     */
    public static final Vec2 div(Vec2 v, float s)
    {
        Vec2 result = new Vec2();
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
    public static final Vec2 div(Vec2 v0, Vec2 v1)
    {
        Vec2 result = new Vec2();
        div(v0, v1, result);
        return result;
    }
    
    /**
     * Negates the given vector and returns the result in a new vector.
     * 
     * @param v The vector to negate.
     * @return A new vector containing the result.
     */
    public static final Vec2 negate(Vec2 v)
    {
        Vec2 result = new Vec2();
        negate(v, result);
        return result;
    }
    
    /**
     * Normalizes {@code v} and returns the result in a new vector.
     * 
     * @param v The vector to normalize.
     * @return The normalized vector.
     */
    public static final Vec2 normalize(Vec2 v)
    {
        Vec2 result = new Vec2();
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
    public static final Vec2 reflect(Vec2 v, Vec2 n)
    {
        Vec2 result = new Vec2();
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
    public static final Vec2 project(Vec2 v0, Vec2 v1)
    {
        Vec2 result = new Vec2();
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
    public static final Vec2 reject(Vec2 v0, Vec2 v1)
    {
        Vec2 result = new Vec2();
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
    public static final Vec2 lerp(Vec2 v0, Vec2 v1, float t)
    {
        Vec2 result = new Vec2();
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
    public static final Vec2 move(Vec2 v, Vec2 dest, float dist)
    {
        Vec2 result = new Vec2();
        move(v, dest, dist, result);
        return result;
    }
    
    /**
     * Rotates {@code v} around the origin by the given angle, and stores the
     * result in {@code result}.
     * 
     * @param v The vector to rotate.
     * @param angle The angle to rotate by.
     * @return A new vector containing the result.
     */
    public static final Vec2 rotate(Vec2 v, float angle)
    {
        Vec2 result = new Vec2();
        rotate(v, angle, result);
        return result;
    }
    // </editor-fold>
    
    public float x, y;
    
    // <editor-fold defaultstate="collapsed" desc="Constructors">
    /**
     * Creates a new zero vector.
     */
    public Vec2()
    {
    }
    
    /**
     * Creates a new vector with the given coordinates.
     */
    public Vec2(float x, float y)
    {
        this.x = x; this.y = y;
    }
    
    /**
     * Creates a new vector with each coordinate set to the given scalar.
     */
    public Vec2(float s)
    {
        x = s; y = s;
    }
    
    /**
     * Copies the given vector.
     * 
     * @param v The vector to copy.
     */
    public Vec2(Vec2 v)
    {
        x = v.x; y = v.y;
    }
    
    /**
     * Copies the given integer vector.
     * 
     * @param v The vector to copy.
     */
    public Vec2(Vec2i v)
    {
        x = v.x; y = v.y;
    }
    
    /**
     * Loads a new vector from the given input stream.
     * 
     * @param in The input stream to read from.
     * @throws IOException If an io error occurred.
     */
    public Vec2(DataInputStream in) throws IOException
    {
        Vec2.this.read(in);
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
    public float dot(Vec2 v)
    {
        return dot(this, v);
    }
    
    /**
     * Returns the z coordinate of the cross product of this and {@code v}.
     * 
     * @param v The vector to multiply by.
     * @return The cross product of this and {@code v}.
     */
    public float cross(Vec2 v)
    {
        return cross(this, v);
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
    public float squareDist(Vec2 v)
    {
        return squareDist(this, v);
    }
    
    /**
     * Returns the distance between this and the given vector.
     * 
     * @param v The vector to calculate the distance from.
     * @return The distance between this and the given vector.
     */
    public float dist(Vec2 v)
    {
        return dist(this, v);
    }
    
    /**
     * Returns the scalar projection of this onto the given vector.
     * 
     * @param v The vector on which to project.
     * @return The scalar projection of this onto the given vector.
     */
    public float scalarProject(Vec2 v)
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
    public Vec2 set(Vec2 v)
    {
        copy(v, this);
        return this;
    }
    
    /**
     * Copies the given integer vector.
     * 
     * @param v The vector to copy.
     */
    public Vec2 set(Vec2i v)
    {
        copy(v, this);
        return this;
    }
    
    /**
     * Sets the coordinates of this vector.
     * 
     * @return This vector.
     */
    public Vec2 set(float x, float y)
    {
        this.x = x; this.y = y;
        return this;
    }
    
    /**
     * Sets each component of this vector to the given scalar.
     * 
     * @param s The scalar to set this to.
     * @return This vector.
     */
    public Vec2 set(float s)
    {
        x = s; y = s;
        return this;
    }
    
    /**
     * Sets this to the zero vector.
     * 
     * @return This vector.
     */
    public Vec2 set()
    {
        x = 0.0f; y = 0.0f;
        return this;
    }
    
    /**
     * Sets the component specified by the given index to the given float.
     * 
     * @param i The index of the component to set.
     * @param f The value to set the component to.
     * @return This vector.
     */
    public Vec2 setComponent(int i, float f)
    {
        switch (i)
        {
            case 0: x = f; return this;
            case 1: y = f; return this;
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
    public Vec2 setAsRow(Mat2 m, int row)
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
    public Vec2 setAsColumn(Mat2 m, int column)
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
    public Vec2 add(Vec2 v)
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
    public Vec2 sub(Vec2 v)
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
    public Vec2 mult(float s)
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
    public Vec2 mult(Vec2 v)
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
    public Vec2 madd(Vec2 v, float s)
    {
        madd(this, v, s, this);
        return this;
    }
    
    /**
     * Multiplies this by the given 3x3 matrix.
     * 
     * @param m The 2x2 matrix to multiply this by.
     * @return This vector.
     */
    public Vec2 mult(Mat2 m)
    {
        mult(this, m, this);
        return this;
    }
    
    /**
     * Multiplies this by the given 4x4 matrix.
     * 
     * @param m The 3x3 matrix to multiply this by.
     * @return This vector.
     */
    public Vec2 mult(Mat3 m)
    {
        mult(this, m, this);
        return this;
    }
    
    /**
     * Divides this by the given scalar.
     * 
     * @param s The scalar to divide by.
     * @return This vector.
     */
    public Vec2 div(float s)
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
    public Vec2 div(Vec2 v)
    {
        div(this, v, this);
        return this;
    }
    
    /**
     * Negates this vector.
     * 
     * @return This vector.
     */
    public Vec2 negate()
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
    public Vec2 normalize()
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
    public Vec2 reflect(Vec2 n)
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
    public Vec2 project(Vec2 v)
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
    public Vec2 reject(Vec2 v)
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
    public Vec2 lerp(Vec2 v, float t)
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
    public Vec2 move(Vec2 dest, float dist)
    {
        move(this, dest, dist, this);
        return this;
    }
    
    /**
     * Rotates this vector around the origin by the given angle.
     * 
     * @param angle The angle to rotate by.
     * @return This vector.
     */
    public Vec2 rotate(float angle)
    {
        rotate(this, angle, this);
        return this;
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Overriden/implemented methods">
    @Override
    public void read(ByteBuffer buffer)
    {
        x = buffer.getFloat();
        y = buffer.getFloat();
    }

    @Override
    public void write(ByteBuffer buffer)
    {
        buffer.putFloat(x);
        buffer.putFloat(y);
    }
    
    @Override
    public void read(FloatBuffer buffer)
    {
        x = buffer.get();
        y = buffer.get();
    }

    @Override
    public void write(FloatBuffer buffer)
    {
        buffer.put(x);
        buffer.put(y);
    }
    
    @Override
    public int bufferSize()
    {
        return 2*4;
    }

    @Override
    public Vec2 read(DataInputStream in) throws IOException
    {
        x = in.readFloat();
        y = in.readFloat();
        return this;
    }

    @Override
    public Vec2 write(DataOutputStream out) throws IOException
    {
        out.writeFloat(x);
        out.writeFloat(y);
        return this;
    }
    
    @Override
    public String toString()
    {
        return "(" + x + ", " + y + ")";
    }
    
    public boolean equals(Vec2 v)
    {
        if (v == null) return false;
        return x == v.x && y == v.y;
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (o == null) return false;
        if (o.getClass() != this.getClass()) return false;
        final Vec2 v = (Vec2)o;
        return equals(v);
    }
    
    @Override
    public int hashCode()
    {
        int hash = 185 + Float.floatToIntBits(this.x);
        return 37*hash + Float.floatToIntBits(this.y);
    }
    // </editor-fold>
}
