package com.samrj.devil.math;

/**
 * Immutable 32 bit, 3 component vector.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 */
public class ImmutableVector3f extends Vector3f
{
    public final float x, y, z;
    
    /**
     * Initializes this to the given coordinates.
     */
    public ImmutableVector3f(float x, float y, float z)
    {
        this.x = x; this.y = y; this.z = z;
    }
    
    /**
     * Initializes this to the given vector.
     */
    public ImmutableVector3f(Vector3f v)
    {
        this(v.x(), v.y(), v.z());
    }
    
    /**
     * Initializes this to the null vector.
     */
    public ImmutableVector3f()
    {
        this(0.0f, 0.0f, 0.0f);
    }

    @Override
    public final float x()
    {
        return x;
    }

    @Override
    public final float y()
    {
        return y;
    }
    
    @Override
    public final float z()
    {
        return z;
    }

    @Override
    public final ImmutableVector3f toImmutable()
    {
        return this;
    }
}
