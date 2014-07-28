package com.samrj.devil.math;

public class Range
{
    public float min, max;
        
    public Range(float min, float max)
    {
        this.min = min;
        this.max = max;
    }

    public Range(float value)
    {
        this(value, value);
    }

    public Range()
    {
        this(Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY);
    }
    
    public Range(Range range)
    {
        this(range.min, range.max);
    }
    
    public Range set(float min, float max)
    {
        this.min = min;
        this.max = max;
        return this;
    }
    
    public Range set(Range range)
    {
        return set(range.min, range.max);
    }
    
    public Range translate(float value)
    {
        min += value;
        max += value;
        return this;
    }

    public Range expand(float value)
    {
        if (value < min) min = value;
        if (value > max) max = value;
        return this;
    }
    
    public Range expand(float... values)
    {
        for (float f : values) expand(f);
        return this;
    }
    
    public Range expand(Range range)
    {
        return expand(range.min, range.max);
    }
    
    /**
     * Calculate the distance between this and range. Will be negative if this
     * overlaps with range.
     */
    public float distance(Range range)
    {
        return (min < range.min) ? range.min - max : min - range.max;
    }
    
    public boolean touches(Range range)
    {
        return distance(range) <= 0f;
    }
    
    @Override
    public Range clone()
    {
        return new Range(this);
    }
}