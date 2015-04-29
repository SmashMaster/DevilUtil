package com.samrj.devil.math;

import com.samrj.devil.buffer.Bufferable;
import java.nio.FloatBuffer;

/**
 * Abstract class for all 32 bit, 2 component vectors.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 */
public abstract class Vector2f implements Bufferable<FloatBuffer>
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
     * Returns an immutable vector that equals this.
     */
    public abstract ImmutableVector2f toImmutable();
    
    /**
     * Returns true if this vector is the null vector, false otherwise.
     */
    public boolean isZero()
    {
        return x() == 0.0f && y() == 0.0f;
    }
    
    /**
     * Returns the squared length of this vector.
     */
    public float squareLength()
    {
        return x()*x() + y()*y();
    }
    
    /**
     * Returns the length of this vector.
     */
    public float length()
    {
        return (float)Math.sqrt(squareLength());
    }
    
    /**
     * Returns the squared distance between this and the given vector.
     */
    public float squareDist(Vector2f v)
    {
        final float dx = v.x() - x();
        final float dy = v.y() - y();
        return dx*dx + dy*dy;
    }
    
    /**
     * Returns this distance between this and the given vector.
     */
    public float dist(Vector2f v)
    {
        final float dx = v.x() - x();
        final float dy = v.y() - y();
        return (float)Math.sqrt(dx*dx + dy*dy);
    }
    
    /**
     * Returns the dot product of this and the given vector.
     */
    public float dot(Vector2f v)
    {
        return x()*v.x() + y()*v.y();
    }
    
    /**
     * Returns the z value of the cross product between this and the given
     * vector.
     * 
     * @param v The vector to use as a multiplier.
     */
    public float cross(Vector2f v)
    {
        return x()*v.y() - v.x()*y();
    }
    
    /**
     * Returns the scalar projection of this onto the given vector.
     */
    public float scalProj(Vector2f v)
    {
        return dot(v)/v.length();
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Nonlocal Mutators">
    /**
     * Returns the sum of this and the given coordinates.
     */
    public MutableVector2f add(float x, float y)
    {
        return new MutableVector2f(this).addLocal(x, y);
    }
    
    /**
     * Returns the sum of this and the given vector.
     */
    public MutableVector2f add(Vector2f v)
    {
        return new MutableVector2f(this).addLocal(v);
    }
    
    /**
     * Returns the difference of this and the given coordinates.
     */
    public MutableVector2f sub(float x, float y)
    {
        return new MutableVector2f(this).subLocal(x, y);
    }
    
    /**
     * Returns the difference of this and the given vector.
     */
    public MutableVector2f sub(Vector2f v)
    {
        return new MutableVector2f(this).subLocal(v);
    }
    
    /**
     * Returns the product of this and the given value.
     */
    public MutableVector2f mult(float f)
    {
        return new MutableVector2f(this).multLocal(f);
    }
    
    /**
     * Returns the negation of this vector.
     */
    public MutableVector2f negate()
    {
        return new MutableVector2f(this).negateLocal();
    }
    
    /**
     * Returns the cross product of this and a three-dimensional
     * vector of the form <0.0, 0.0, z>..
     * 
     * @param z The z value of the vector to use as a multiplier.
     */
    public MutableVector2f cross(float z)
    {
        return new MutableVector2f(this).crossLocal(z);
    }
    
    /**
     * Returns the quotient of this and the given value.
     */
    public MutableVector2f div(float f)
    {
        return new MutableVector2f(this).divLocal(f);
    }
    
    /**
     * Returns the average of this and the given vector.
     */
    public MutableVector2f avg(Vector2f v)
    {
        return new MutableVector2f(this).avgLocal(v);
    }
    
    /**
     * Returns a normalized copy of this.
     */
    public MutableVector2f norm()
    {
        return new MutableVector2f(this).normLocal();
    }
    
    /**
     * Returns a vector with the given length, pointing in the same direction as
     * this.
     */
    public MutableVector2f multNorm(float f)
    {
        return new MutableVector2f(this).multNormLocal(f);
    }
    
    /**
     * Returns the vector projection of this onto the given vector.
     */
    public MutableVector2f vecProj(Vector2f v)
    {
        return new MutableVector2f(this).vecProjLocal(v);
    }
    
    /**
     * Returns the vector projection of this onto the given vector. Assumes that
     * the given vector is normalized. Fast.
     */
    public MutableVector2f vecProjUnit(Vector2f v)
    {
        return new MutableVector2f(this).vecProjUnitLocal(v);
    }
    
    /**
     * Returns the reflection of this about the given vector.
     */
    public MutableVector2f reflectUnit(Vector2f n)
    {
        return new MutableVector2f(this).reflectUnitLocal(n);
    }
    
    /**
     * Returns the reflection of this about the given vector.
     */
    public MutableVector2f refractunit(Vector2f n, float eta)
    {
        return new MutableVector2f(this).refractUnitLocal(n, eta);
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Object Overriden Methods">
    @Override
    public String toString()
    {
        return "("+x()+", "+y()+")";
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        final Vector2f other = (Vector2f) obj;
        if (Float.floatToIntBits(this.x()) != Float.floatToIntBits(other.x())) return false;
        if (Float.floatToIntBits(this.y()) != Float.floatToIntBits(other.y())) return false;
        return true;
    }
    // </editor-fold>
    
    @Override
    public void writeTo(FloatBuffer buffer)
    {
        buffer.put(x());
        buffer.put(y());
    }
    
    @Override
    public int bufferSize()
    {
        return 2;
    }
}
