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
 * Class which represents transformations in 3D space.
 * 
 * @author Samuel Johnson (SmashMaster)
 */
public class Transform implements FloatBufferable, DataStreamable<Transform>
{
    public enum Property
    {
        POSITION, ROTATION, SCALE;
    }
    
    // <editor-fold defaultstate="collapsed" desc="Static mutator methods">
    /**
     * Copies the first given transform into the second.
     * 
     * @param source The transform to copy from.
     * @param target The transform to copy to.
     */
    public static final void copy(Transform source, Transform target)
    {
        Vec3.copy(source.pos, target.pos);
        Quat.copy(source.rot, target.rot);
        Vec3.copy(source.sca, target.sca);
    }
    
    /**
     * Sets the given transform to the default, zero transform.
     * 
     * @param result The transform in which to store the result.
     */
    public static final void zero(Transform result)
    {
        result.pos.set();
        result.rot.set();
        result.sca.set();
    }
    
    /**
     * Sets the given transform to the identity transform.
     * 
     * @param result The transform in which to store the result.
     */
    public static final void identity(Transform result)
    {
        result.pos.set();
        result.rot.setIdentity();
        result.sca.set(1.0f);
    }
    
    /**
     * Decomposes the given matrix into a transform. Will not have valid results
     * for matrices that have any shearing or projection.
     * 
     * @param matrix The matrix to decompose.
     * @param r The transform in which to store the result.
     */
    public static final void decompose(Mat4 matrix, Transform r)
    {
        Vec3 colx = new Vec3(matrix.a, matrix.b, matrix.c);
        Vec3 coly = new Vec3(matrix.e, matrix.f, matrix.g);
        Vec3 colz = new Vec3(matrix.i, matrix.j, matrix.k);
        Vec3 sca = new Vec3(colx.length(), coly.length(), colz.length());
        Mat3 rotMat = new Mat3(matrix.a/sca.x, matrix.b/sca.y, matrix.c/sca.z,
                               matrix.e/sca.x, matrix.f/sca.y, matrix.g/sca.z,
                               matrix.i/sca.x, matrix.j/sca.y, matrix.k/sca.z);
        
        r.sca.set(sca);
        r.rot.setRotation(rotMat);
        r.pos.set(matrix.d, matrix.h, matrix.l);
    }
    
    /**
     * Performs a transform composition on {@code t0} and {@code t1}, and stores
     * the result in {@code r}. 
     * 
     * @param t0 The left-hand transform to multiply.
     * @param t1 The right-hand transform to multiply by.
     * @param r The transform in which to store the result.
     */
    public static final void mult(Transform t0, Transform t1, Transform r)
    {
        r.setDecomposition(Mat4.transform(t1).mult(t0));
    }
    
    /**
     * Multiplies the given transform by the given matrix, and stores the result
     * in {@code r}. This is expensive, and will not be valid for matrices that
     * have any shearing or projection.
     * 
     * @param t The left-hand transform to multiply.
     * @param m The right-hand matrix to multiply by.
     * @param r The transform in which to store the result.
     */
    public static final void mult(Transform t, Mat4 m, Transform r)
    {
        mult(t, decompose(m), r);
    }
    
    /**
     * Interpolates between the two given transform using the given scalar, and
     * stores the result in {@code r}. 
     * 
     * @param t0 The 'start' transform to interpolate from.
     * @param t1 The 'end' transform to interpolate to.
     * @param t The scalar interpolant, between zero and one (inclusive).
     * @param result The transform in which to store the result.
     */
    public static final void lerp(Transform t0, Transform t1, float t, Transform result)
    {
        Vec3.lerp(t0.pos, t1.pos, t, result.pos);
        Quat.slerp(t0.rot, t1.rot, t, result.rot);
        Vec3.lerp(t0.sca, t1.sca, t, result.sca);
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Static factory methods">
    /**
     * Returns a new identity transform.
     * 
     * @return A new identity transform.
     */
    public static final Transform identity()
    {
        Transform result = new Transform();
        identity(result);
        return result;
    }
    
    /**
     * Returns a transform representation of the given matrix. Will not have
     * valid results for matrices that have any shearing or projection.
     * 
     * @param matrix The matrix to decompose.
     * @return A new transform.
     */
    public static final Transform decompose(Mat4 matrix)
    {
        Transform result = new Transform();
        decompose(matrix, result);
        return result;
    }
    
    /**
     * Composes the two given transforms, and returns the result in a new
     * transform.
     * 
     * @param t0 The left-hand transform to multiply.
     * @param t1 The right-hand transform to multiply by.
     * @return A new transform.
     */
    public static final Transform mult(Transform t0, Transform t1)
    {
        Transform result = new Transform();
        mult(t0, t1, result);
        return result;
    }
    
    /**
     * Multiplies the given transform by the given matrix, and returns the
     * result in a new transform. This is expensive, and will not be valid for
     * matrices that have any shearing or projection.
     * 
     * @param t The left-hand transform to multiply.
     * @param m The right-hand matrix to multiply by.
     * @return A new transform.
     */
    public static final Transform mult(Transform t, Mat4 m)
    {
        Transform result = new Transform();
        mult(t, m, result);
        return result;
    }
    
    /**
     * Interpolates between the two given transforms by the given scalar, and
     * returns the result in a new transform.
     * 
     * @param t0 The 'start' transform to interpolate from.
     * @param t1 The 'end' transform to interpolate to.
     * @param t The scalar interpolant, between zero and one (inclusive).
     * @return A new transform.
     */
    public static final Transform lerp(Transform t0, Transform t1, float t)
    {
        Transform result = new Transform();
        lerp(t0, t1, t, result);
        return result;
    }
    // </editor-fold>
    
    public final Vec3 pos;
    public final Quat rot;
    public final Vec3 sca;
    
    // <editor-fold defaultstate="collapsed" desc="Constructors">
    /**
     * Creates a new zero transform, NOT an identity transform. Use identity()
     * to create an identity transform.
     */
    public Transform()
    {
        pos = new Vec3();
        rot = new Quat();
        sca = new Vec3();
    }
    
    /**
     * Creates a new transform using the given position, rotation, and scale.
     * 
     * @param pos A position.
     * @param rot A rotation.
     * @param sca A scale.
     */
    public Transform(Vec3 pos, Quat rot, Vec3 sca)
    {
        this.pos = new Vec3(pos);
        this.rot = new Quat(rot);
        this.sca = new Vec3(sca);
    }
    
    /**
     * Copies the given transform.
     * 
     * @param transform The transform to copy.
     */
    public Transform(Transform transform)
    {
        pos = new Vec3(transform.pos);
        rot = new Quat(transform.rot);
        sca = new Vec3(transform.sca);
    }

    /**
     * Loads a new transform from the given buffer.
     *
     * @param buffer The buffer to read from.
     */
    public Transform(ByteBuffer buffer)
    {
        pos = new Vec3(buffer);
        rot = new Quat(buffer);
        sca = new Vec3(buffer);
    }

    /**
     * Loads a new transform from the given input stream.
     * 
     * @param in The input stream to read from.
     * @throws IOException If an io error occurred.
     */
    public Transform(DataInputStream in) throws IOException
    {
        pos = new Vec3(in);
        rot = new Quat(in);
        sca = new Vec3(in);
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Instance mutator methods">
    /**
     * Sets this transform to zero.
     * 
     * @return This transform.
     */
    public Transform set()
    {
        zero(this);
        return this;
    }
    
    /**
     * Sets this to the identity transform.
     * 
     * @return This transform.
     */
    public Transform setIdentity()
    {
        identity(this);
        return this;
    }
    
    /**
     * Sets this transform to the given position, rotation, and scale.
     * 
     * @param pos A position.
     * @param rot A rotation.
     * @param sca A scale.
     * @return This transform.
     */
    public Transform set(Vec3 pos, Quat rot, Vec3 sca)
    {
        Vec3.copy(pos, this.pos);
        Quat.copy(rot, this.rot);
        Vec3.copy(sca, this.sca);
        return this;
    }
    
    /**
     * Sets this to the given transform.
     * 
     * @param transform The transform to set to.
     * @return This transform.
     */
    public Transform set(Transform transform)
    {
        copy(transform, this);
        return this;
    }
    
    /**
     * Sets this to the decomposition of the given matrix. Will not be valid if
     * the matrix has any shearing or projection.
     * 
     * @param matrix The matrix to decompose.
     * @return This transform.
     */
    public Transform setDecomposition(Mat4 matrix)
    {
        decompose(matrix, this);
        return this;
    }
    
    /**
     * Multiplies this by the given transform.
     * 
     * @param transform The transform to multiply by.
     * @return This transform.
     */
    public Transform mult(Transform transform)
    {
        mult(this, transform, this);
        return this;
    }
    
    /**
     * Multiplies this by the given matrix. This is expensive, and will not be
     * valid for matrices that have any shearing or projection.
     * 
     * @param matrix The matrix to multiply by.
     * @return This transform.
     */
    public Transform mult(Mat4 matrix)
    {
        mult(this, matrix, this);
        return this;
    }
    
    /**
     * Interpolates this towards the given transform with the given scalar
     * interpolant.
     * 
     * @param transform The 'end' transform to interpolate to.
     * @param t The scalar interpolant, between zero and one (inclusive).
     * @return This transform.
     */
    public Transform lerp(Transform transform, float t)
    {
        lerp(this, transform, t, this);
        return this;
    }
    // </editor-fold>
    
    public void setProperty(Property property, int index, float value)
    {
        switch (property)
        {
            case POSITION: pos.setComponent(index, value); break;
            case ROTATION: rot.setComponent(index, value); break;
            case SCALE:    sca.setComponent(index, value); break;
            default: throw new IllegalArgumentException();
        }
    }
    
    // <editor-fold defaultstate="collapsed" desc="Overriden/implemented methods">
    @Override
    public void read(ByteBuffer buffer)
    {
        pos.read(buffer);
        rot.read(buffer);
        sca.read(buffer);
    }

    @Override
    public void write(ByteBuffer buffer)
    {
        pos.write(buffer);
        rot.write(buffer);
        sca.write(buffer);
    }
    
    @Override
    public void read(FloatBuffer buffer)
    {
        pos.read(buffer);
        rot.read(buffer);
        sca.read(buffer);
    }
    
    @Override
    public void write(FloatBuffer buffer)
    {
        pos.write(buffer);
        rot.write(buffer);
        sca.write(buffer);
    }

    @Override
    public int bufferSize()
    {
        return 10*4;
    }

    @Override
    public Transform read(DataInputStream in) throws IOException
    {
        pos.read(in);
        rot.read(in);
        sca.read(in);
        return this;
    }

    @Override
    public Transform write(DataOutputStream out) throws IOException
    {
        pos.write(out);
        rot.write(out);
        sca.write(out);
        return this;
    }
    
    @Override
    public String toString()
    {
        return "{ " + pos + " " + rot + " " + sca + " }";
    }
    // </editor-fold>
}
