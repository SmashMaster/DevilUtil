package com.samrj.devil.math;

import com.samrj.devil.buffer.Bufferable;
import java.nio.FloatBuffer;

/**
 * Abstract class for all 32 bit, 3 component vectors.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 */
public abstract class Vector3f implements Bufferable<FloatBuffer>
{
    // <editor-fold defaultstate="collapsed" desc="Accessors">
    /**
     * Returns the x component of this vector.
     */
    public abstract float x();
    
    /**
     * Returns the y component of this vector.
     */
    public abstract float y();
    
    /**
     * Returns the z component of this vector.
     */
    public abstract float z();
    
    /**
     * Returns an immutable vector that equals this.
     */
    public abstract ImmutableVector3f toImmutable();
    
    /**
     * Returns true if this vector is the null vector, false otherwise.
     */
    public boolean isZero()
    {
        return x() == 0.0f && y() == 0.0f && z() == 0.0f;
    }
    
    /**
     * Returns the squared length of this vector.
     */
    public float squareLength()
    {
        return x()*x() + y()*y() + z()*z();
    }
    
    /**
     * Returns the length of this vector.
     */
    public float length()
    {
        return (float)Math.sqrt(x()*x() + y()*y() + z()*z());
    }
    
    /**
     * Returns the squared distance between this and the given vector.
     */
    public float squareDist(Vector3f v)
    {
        final float dx = v.x() - x();
        final float dy = v.y() - y();
        final float dz = v.z() - z();
        return dx*dx + dy*dy + dz*dz;
    }
    
    /**
     * Returns this distance between this and the given vector.
     */
    public float dist(Vector3f v)
    {
        final float dx = v.x() - x();
        final float dy = v.y() - y();
        final float dz = v.z() - z();
        return (float)Math.sqrt(dx*dx + dy*dy + dz*dz);
    }
    
    /**
     * Returns the dot product of this and the given vector.
     */
    public float dot(Vector3f v)
    {
        return x()*v.x() + y()*v.y() + z()*v.z();
    }
    
    /**
     * Returns the scalar projection of this onto the given vector.
     */
    public float scalProj(Vector3f v)
    {
        return dot(v)/v.length();
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Nonlocal Mutators">
    /**
     * Returns the sum of this vector and the given coordinates.
     */
    public MutableVector3f add(float x, float y, float z)
    {
        return new MutableVector3f(this).addLocal(x, y, z);
    }
    
    /**
     * Returns the sum of this vector and the given vector.
     */
    public MutableVector3f add(MutableVector3f v)
    {
        return new MutableVector3f(this).addLocal(v);
    }
    
    /**
     * Returns the difference between this and the given coordinates.
     */
    public MutableVector3f sub(float x, float y, float z)
    {
        return new MutableVector3f(this).subLocal(x, y, z);
    }
    
    /**
     * Returns the difference between this and the given vector.
     */
    public MutableVector3f sub(MutableVector3f v)
    {
        return new MutableVector3f(this).subLocal(v);
    }
    
    /**
     * Returns the product of this and the given value.
     */
    public MutableVector3f mult(float f)
    {
        return new MutableVector3f(this).multLocal(f);
    }
    
    /**
     * Returns the negation of this vector.
     */
    public MutableVector3f negate()
    {
        return new MutableVector3f(this).negateLocal();
    }
    
    /**
     * Returns the cross product of this and the given vector.
     */
    public MutableVector3f cross(Vector3f v)
    {
        return new MutableVector3f(this).crossLocal(v);
    }
    
    /**
     * Returns the quotient of this and the given value.
     */
    public MutableVector3f div(float f)
    {
        return new MutableVector3f(this).divLocal(f);
    }
    
    /**
     * Returns the average of this and the given vector.
     */
    public MutableVector3f avg(Vector3f v)
    {
        return new MutableVector3f(this).avgLocal(v);
    }
    
    /**
     * Returns a normalized copy of this vector.
     */
    public MutableVector3f norm()
    {
        return new MutableVector3f(this).normLocal();
    }
    
    /**
     * Returns a copy of this vector whose length is the given value.
     */
    public MutableVector3f multNorm(float f)
    {
        return new MutableVector3f(this).multNormLocal(f);
    }
    
    /**
     * Returns the vector projection of this onto the given vector.
     */
    public MutableVector3f vecProj(Vector3f v)
    {
        return new MutableVector3f(this).vecProjLocal(v);
    }
    
    /**
     * Returns the vector projection of this onto the given vector. Assumes that
     * the given vector is normalized. Fast.
     */
    public MutableVector3f vecProjUnit(Vector3f v)
    {
        return new MutableVector3f(this).vecProjUnitLocal(v);
    }
    
    /**
     * Returns the reflection of this about the given vector.
     */
    public MutableVector3f reflectUnit(Vector3f n)
    {
        return new MutableVector3f(this).reflectUnitLocal(n);
    }
    
    /**
     * Returns the reflection of this about the given vector.
     */
    public MutableVector3f refractunit(Vector3f n, float eta)
    {
        return new MutableVector3f(this).refractUnitLocal(n, eta);
    }
    // </editor-fold>
    
    @Override
    public void writeTo(FloatBuffer buffer)
    {
        buffer.put(x());
        buffer.put(y());
        buffer.put(z());
    }
    
    @Override
    public int bufferSize()
    {
        return 3;
    }
}
