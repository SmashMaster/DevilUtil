package com.samrj.devil.math;

/**
 * Mathematics utility class.
 * 
 * @author SmashMaster
 * @copyright 2015 Samuel Johnson
 */
public class Util
{
    /**
     * Linearly interpolates between {@code f0} and {@code f1} using the scalar
     * interpolant {@code t}.
     * 
     * @param f0 The 'start' value to interpolate from.
     * @param f1 The 'end' value to interpolate to.
     * @param t The scalar interpolant.
     * @return The interpolated value.
     */
    public static final float lerp(float f0, float f1, float t)
    {
        return (f1 - f0)*t + f0;
    }
    
    /**
     * Clamps {@code x} between {@code min} and {@code max} (inclusive) and
     * returns the result. If {@code x} is NaN, return NaN.
     * 
     * @param min The minimum output value.
     * @param max The maximum output value.
     * @param x The value to clamp.
     * @return The clamped value.
     */
    public static final float clamp(float min, float max, float x)
    {
        if (x < min) return min;
        if (x > max) return max;
        return x; //Implicitly handles NaN and the infinites.
    }
    
    public static final float PI = (float)Math.PI;
    public static final float TO_RADIANS = (float)(Math.PI/180.0);
    public static final float TO_DEGREES = (float)(180.0/Math.PI);
    
    public static float toRadians(float a)
    {
        return a*TO_RADIANS;
    }
    
    public static float toDegrees(float a)
    {
        return a*TO_DEGREES;
    }
    
    private Util()
    {
    }
}
