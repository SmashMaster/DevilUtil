package com.samrj.devil.math;

/**
 * Immutable 32 bit, 2 component vector.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 */
public class ImmutableVector2f extends Vector2f
{
    public final float x, y;
    
    /**
     * Initializes this to the given coordinates.
     */
    public ImmutableVector2f(float x, float y)
    {
        this.x = x; this.y = y;
    }
    
    /**
     * Initializes this to the given vector.
     */
    public ImmutableVector2f(Vector2f v)
    {
        this(v.x(), v.y());
    }
    
    /**
     * Initializes this to the null vector.
     */
    public ImmutableVector2f()
    {
        this(0.0f, 0.0f);
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
    public final ImmutableVector2f toImmutable()
    {
        return this;
    }
}
