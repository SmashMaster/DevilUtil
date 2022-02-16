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

/**
 * Mathematics utility class.
 * 
 * @author Samuel Johnson (SmashMaster)
 */
public final class Util
{
    /**
     * Primitive type enum, to help make buffering code more readable.
     */
    public static enum PrimType
    {
        BYTE   (Byte.SIZE,      Byte.TYPE),
        SHORT  (Short.SIZE,     Short.TYPE),
        INT    (Integer.SIZE,   Integer.TYPE),
        LONG   (Long.SIZE,      Long.TYPE),
        FLOAT  (Float.SIZE,     Float.TYPE),
        DOUBLE (Double.SIZE,    Double.TYPE),
        BOOLEAN(                Boolean.TYPE),
        CHAR   (Character.SIZE, Character.TYPE);
        
        /**
         * The size of this primitive, in bytes.
         */
        public final int size;
        private final Class type;
        
        private PrimType(int size, Class type)
        {
            this.size = size/Byte.SIZE;
            this.type = type;
        }
        
        private PrimType(Class type)
        {
            size = -1;
            this.type = type;
        }
        
        /**
         * Returns whether or not the given object is an instance of this
         * primitive type.
         * 
         * @param o Any object.
         * @return Whether the given object is an instance of this primitive.
         */
        public boolean isType(Object o)
        {
            return type.isAssignableFrom(o.getClass());
        }
    }
    
    /**
     * Returns the size, in bytes, of the primitive type corresponding with the
     * given enum.
     * 
     * @param type A primitive type enum.
     * @return The size, in bytes, of the given primitive type.
     */
    public static int sizeof(PrimType type)
    {
        return type.size;
    }
    
    /**
     * Returns whether or not the given integer is a power of two.
     * 
     * @param n Any integer.
     * @return Whether the given integer is a power of two.
     */
    public static boolean isPower2(int n)
    {
        return (n & (n - 1)) == 0;
    }
    
    /**
     * Returns the smallest power of 2 that is greater than the given integer.
     * 
     * @param n Any positive integer.
     * @return The smallest power of 2 that is greater than {@code n}.
     */
    public static int nextPower2(int n)
    {
        if (n <= 0) return 1;
        n |= n >> 1;
        n |= n >> 2;
        n |= n >> 4;
        n |= n >> 8;
        n |= n >> 16;
        n++;
        return n;
    }
    
    /**
     * Returns the smallest integer that is larger or equal to the given float.
     * Has undefined behavior for NaN and the infinities.
     * 
     * @param f A finite float.
     * @return The first integer that is larger than f.
     */
    public static int ceil(float f)
    {
        int i = (int)f;
        if (i < f) i++;
        return i;
    }
    
    /**
     * Returns the largest integer that is smaller or equal to the given float.
     * Has undefined behavior for NaN and the infinities.
     * 
     * @param f A finite float.
     * @return The first integer that is smaller than f.
     */
    public static int floor(float f)
    {
        int i = (int)f;
        if (i > f) i--;
        return i;
    }
    
    /**
     * Linearly interpolates between {@code f0} and {@code f1} using the scalar
     * interpolant {@code t}.
     * 
     * @param f0 The 'start' value to interpolate from.
     * @param f1 The 'end' value to interpolate to.
     * @param t The scalar interpolant.
     * @return The interpolated value.
     */
    public static float lerp(float f0, float f1, float t)
    {
        return (f1 - f0)*t + f0;
    }
    
    /**
     * Linearly interpolates between {@code f0} and {@code f1} using the scalar
     * interpolant {@code t}.
     * 
     * @param f0 The 'start' value to interpolate from.
     * @param f1 The 'end' value to interpolate to.
     * @param t The scalar interpolant.
     * @return The interpolated value.
     */
    public static double lerp(double f0, double f1, double t)
    {
        return (f1 - f0)*t + f0;
    }
    
    /**
     * Clamps {@code x} between {@code min} and {@code max} (inclusive) and
     * returns the result. If {@code x} is NaN, returns NaN.
     * 
     * @param min The minimum output value.
     * @param max The maximum output value.
     * @param x The value to clamp.
     * @return The clamped value.
     */
    public static float clamp(float x, float min, float max)
    {
        if (x < min) return min;
        if (x > max) return max;
        return x; //Implicitly handles NaN and the infinites.
    }
    
    /**
     * Clamps the given value to between zero and one (inclusive) and returns
     * the result.
     * 
     * @param x The value to saturate.
     * @return The saturated value.
     */
    public static float saturate(float x)
    {
        return clamp(x, 0.0f, 1.0f);
    }
    
    /**
     * Returns a value between 0 and 1, linearly interpolated when'
     * edge1 > x > edge0.
     * 
     * @param edge0 The value of the lower edge.
     * @param edge1 The value of the upper edge.
     * @param x The source value for interpolation.
     * @return The interpolated value.
     */
    public static float linstep(float edge0, float edge1, float x)
    {
        return saturate((x - edge0)/(edge1 - edge0));
    }
    
    /**
     * Performs smooth Hermite interpolation between 0 and 1 when
     * edge1 > x > edge0. This is useful in cases where a threshold function
     * with a smooth transition is desired. Results undefined if edge0 >= edge1.
     * 
     * @param edge0 The value of the lower edge of the Hermite function.
     * @param edge1 The value of the upper edge of the Hermite function. 
     * @param x The source value for interpolation. 
     * @return The interpolated value.
     */
    public static float smoothstep(float edge0, float edge1, float x)
    {
        float t = linstep(edge0, edge1, x);
        return t*t*(3.0f - 2.0f*t);
    }
    
    /**
     * Returns a value between zero and one (inclusive) depending on the given
     * values. The value starts at zero as x increases, linearly fades to one,
     * stays there, and then fades back down to zero.
     * 
     * @param x The 'time' value.
     * @param start The time at which to start fading in.
     * @param fadeIn The amount of time to spend fading in.
     * @param fadeOut The amount of time to spend fading out.
     * @param end The time at which to finish fading out.
     * @return The envelope of the given value.
     */
    public static float envelope(float x, float start, float fadeIn, float fadeOut, float end)
    {
        if (x < start || x > end) return 0.0f;
        
        if (x <= end - fadeOut)
        {
            if (x >= start + fadeIn) return 1.0f;
            else return (x - start)/fadeIn;
        }
        else return (end - x)/fadeOut;
    }
    
    /**
     * Moves the given value towards the given target, by the given distance.
     * If the value is closer than the given distance to the target, returns the
     * target.
     * 
     * @param x
     * @param target
     * @param distance
     * @return 
     */
    public static float move(float x, float target, float distance)
    {
        float diff = target - x;
        if (Math.abs(diff) <= distance) return target;
        else return x + signum(diff)*distance;
    }
    
    /**
     * Returns the smaller of the two given values. Does not check for negative
     * zero or NaN.
     * 
     * @param x0 Any float.
     * @param x1 Any float.
     * @return The smaller of the two given values.
     */
    public static float min(float x0, float x1)
    {
        return x0 <= x1 ? x0 : x1;
    }
    
    /**
     * Returns -1.0, 0.0, or 1.0 as the given float is negative, zero, or
     * positive, respectively. Returns undefined result for NaN.
     * 
     * @param f Any float.
     * @return -1.0, 0.0, or 1.0 as the given float is negative, zero, or
     *         positive, respectively.
     */
    public static float signum(float f)
    {
        if (f == 0.0f) return 0.0f;
        return f < 0.0f ? -1.0f : 1.0f;
    }
    
    /**
     * Returns the greater of the two given values. Does not check for negative
     * zero or NaN.
     * 
     * @param x0 Any float.
     * @param x1 Any float.
     * @return The greater of the two given values.
     */
    public static float max(float x0, float x1)
    {
        return x0 >= x1 ? x0 : x1;
    }
    
    
    /**
     * Returns the index of the largest value in the given array. 
     * 
     * @param values An array of floats.
     * @return The index of the largest value in the given array. 
     */
    public static int maxdex(float... values)
    {
        int index = 0;
        float max = values[0];
        
        for (int i=1; i<values.length; i++)
        {
            float value = values[i];
            if (value > max)
            {
                max = value;
                index = i;
            }
        }
        
        return index;
    }
    
    
    /**
     * Loops the given value into the given range.
     * 
     * @param x The value to loop.
     * @param min The minimum output value, inclusive.
     * @param max The maximum output value, exclusive.
     * @return The value, looped to within the given range.
     */
    public static float loop(float x, float min, float max)
    {
        if (x >= min && x < max) return x;
        if (min >= max) return min;
        
        float t = (x - min)%(max - min);
        return t < 0.0f ? t + max : t + min;
    }
    
    /**
     * Loops the given value into the range between zero and the given bound.
     * 
     * @param x The value to loop.
     * @param max The maximum output value, exclusive.
     * @return The looped value.
     */
    public static float loop(float x, float max)
    {
        if (x >= 0.0f && x < max) return x;
        
        float t = x%max;
        return t < 0.0f ? t + max : t;
    }
    
    /**
     * Loops the given value into the given range.
     * 
     * @param x The value to loop.
     * @param min The minimum output value, inclusive.
     * @param max The maximum output value, exclusive.
     * @return The value, looped to within the given range.
     */
    public static int loop(int x, int min, int max)
    {
        if (x >= min && x < max) return x;
        
        int t = (x - min)%(max - min);
        return t < 0 ? t + max : t + min;
    }
    
    /**
     * Loops the given value into the range between zero and the given bound.
     * 
     * @param x The value to loop.
     * @param max The maximum output value, exclusive.
     * @return The looped value.
     */
    public static int loop(int x, int max)
    {
        if (x >= 0 && x < max) return x;
        
        int t = x%max;
        return t < 0 ? t + max : t;
    }
    
    /**
     * Compares two floats for order. Returns a negative integer,
     * zero, or a positive integer as the first float is less than, equal
     * to, or greater than the second. The two floats are considered to be equal
     * if they are closer than the given tolerance.
     * 
     * Has undefined behavior for NaN.
     * 
     * @param f0 The first float to compare.
     * @param f1 The second float to compare.
     * @param tolerance The amount by which f0 and f1 may differ.
     * @return A negative integer, zero, or a positive integer as the first
     *         float is less than, equal to, or greater than the second.
     */
    public static int compare(float f0, float f1, float tolerance)
    {
        float d = f0 - f1;
        if (Math.abs(d) <= tolerance) return 0;
        return d < 0 ? -1 : 1;
    }
    
    /**
     * Compares two floats for order. Returns a negative integer,
     * zero, or a positive integer as the first float is less than, equal
     * to, or greater than the second.
     * 
     * Has undefined behavior for NaN.
     * 
     * @param f0 The first float to compare.
     * @param f1 The second float to compare.
     * @return A negative integer, zero, or a positive integer as the first
     *         float is less than, equal to, or greater than the second.
     */
    public static int compare(float f0, float f1)
    {
        return f0 == f1 ? 0 : (f0 < f1 ? -1 : 1);
    }
    
    /**
     * Prevents {@code x} from reaching zero. If {@code x < m}, ease it towards
     * {@code n}. Otherwise, simply return {@code x}.
     * 
     * Useful for values that should never reach zero, where n =/= zero.
     * 
     * Thanks to Inigo Quilez:
     * http://www.iquilezles.org/www/articles/functions/functions.htm
     * 
     * @param x The value to adjust.
     * @param m The threshold at which {@code x} starts to ease towards {@code n}.
     * @param n The value to ease towards.
     * @return The adjusted value.
     */
    public static float nonzero(float x, float m, float n)
    {
        if (x >= m) return x;

        float a = 2.0f*n - m;
        float b = 2.0f*m - 3.0f*n;
        float t = x/m;

        return (a*t + b)*t*t + n;
    }
    
    /**
     * Returns f(x) such that f(0) = 1 and f(1) = 0. Higher exponents make the
     * curve sharper near x = 1. Lower makes it sharper near x = 0. An exponent
     * of 1 returns clamp(1f - x, 0f, 1f).
     * 
     * @param x The value to attenuate.
     * @param exp The exponent, which affects the sharpness of the curve.
     * @return The attenuated value.
     */
    public static float attenuate(float x, float exp)
    {
        if (exp == 1.0f) return clamp(1.0f - x, 0.0f, 1.0f);
        return 1.0f - (float)Math.pow(clamp(x, 0.0f, 1.0f), exp);
    }
    
    /**
     * Solves the quadratic formula with the given coefficients.
     * 
     * @param a The quadratic coefficient.
     * @param b The linear coefficient.
     * @param c The constant coefficient.
     * @return A float array of length 0, 1, or 2.
     */
    public static float[] quadFormula(float a, float b, float c)
    {
        if (Util.isSubnormal(a)) return new float[0];
        
        float discriminant = b*b - 4f*a*c;
        
        if (discriminant < 0.0f || !Float.isFinite(discriminant)) return new float[0];
        if (discriminant == 0.0f) return new float[] {-b/(a*2f)};
        
        float sqrtDisc = (float)Math.sqrt(discriminant);
        
        float a2 = a*2f;
        return new float[] {(-b - sqrtDisc)/a2, (sqrtDisc - b)/a2};
    }
    
    public static final float PI = (float)Math.PI;
    public static final float PIm2 = (float)(Math.PI*2.0);
    public static final float PId2 = (float)(Math.PI/2.0);
    public static final float TO_RADIANS = (float)(Math.PI/180.0);
    public static final float TO_DEGREES = (float)(180.0/Math.PI);
    
    /**
     * Reduces the given angle to its equivalent angle between -pi and pi.
     * 
     * @param angle The angle to reduce.
     * @return The given angle, reduced to within -pi and pi.
     */
    public static float reduceAngle(float angle)
    {
        return loop(angle, -PI, PI);
    }
    
    /**
     * Converts an angle in degrees to radians.
     * 
     * @param a An angle in degrees.
     * @return The given angle in radians.
     */
    public static float toRadians(float a)
    {
        return a*TO_RADIANS;
    }
    
    /**
     * Converts an angle in radians to degrees.
     * 
     * @param a An angle in radians.
     * @return The given angle in degrees.
     */
    public static float toDegrees(float a)
    {
        return a*TO_DEGREES;
    }
    
    /**
     * Returns the smallest signed difference between the two given angles, from
     * a to b.
     * 
     * @param a The starting angle.
     * @param b The ending angle.
     * @return The smallest difference between the two given angles.
     */
    public static float angleDiff(float a, float b)
    {
        return reduceAngle(b - a);
    }
    
    /**
     * Moves the given angle towards the given target along the shortest
     * available path, using the given distance.
     * 
     * @param angle The angle to move.
     * @param target The angle to move towards.
     * @param distance The amount to move.
     * @return The moved angle.
     */
    public static float moveAngle(float angle, float target, float distance)
    {
        float diff = angleDiff(angle, target);
        if (Math.abs(diff) <= distance) return target;
        else return reduceAngle(angle + signum(diff)*distance);
    }
    
    /**
     * Loop the given square angle into the range between
     * 
     * @param a The square angle to reduce.
     * @return  The reduced square angle.
     */
    public static float reduceSquareAngle(float a)
    {
        return Util.loop(a, 8.0f);
    }
    
    /**
     * Calculates the y coordinate of the given square angle on a 2x2 square
     * centered at the origin.
     * 
     * @param a A square angle.
     * @return The y coordinate corresponding with the given angle.
     */
    public static float squareSin(float a)
    {
        a = reduceSquareAngle(a);
        
        if (a >= 7.0f) return a - 8.0f;
        if (a >= 5.0f) return -1.0f;
        if (a >= 3.0f) return 4.0f - a;
        if (a >= 1.0f) return 1.0f;
        return a;
    }
    
    /**
     * Calculates the x coordinate of the given square angle on a 2x2 square
     * centered at the origin.
     * 
     * @param a A square angle.
     * @return The x coordinate corresponding with the given angle.
     */
    public static float squareCos(float a)
    {
        a = reduceSquareAngle(a);
        
        if (a >= 7.0f) return 1.0f;
        if (a >= 5.0f) return a - 6.0f;
        if (a >= 3.0f) return -1.0f;
        if (a >= 1.0f) return 2.0f - a;
        return 1.0f;
    }
    
    /**
     * Calculates the position of the given square angle on a 2x2 square
     * centered at the origin.
     * 
     * @param a A square angle.
     * @return The position corresponding with the given angle.
     */
    public static Vec2 squareDir(float a)
    {
        return new Vec2(squareCos(a), squareSin(a));
    }
    
    /**
     * Calculates the square angle corresponding with the given direction.
     * 
     * @param y The y coordinate of the given direction.
     * @param x The x coordinate of the given direction.
     * @return The square angle corresponding with the given direction.
     */
    public static float squareAtan2(float y, float x)
    {
        float squareLength = Math.max(Math.abs(x), Math.abs(y));
        y /= squareLength; x /= squareLength;
        
        int signs = (x < 0.0f ? 1 : 0) | (y < 0.0f ? 2 : 0);
        
        switch (signs)
        {
            case 0: return -x + y + 1.0f; //(+, +)
            case 1: return -x - y + 3.0f; //(-, +)
            case 2: return  x + y + 7.0f; //(+, -)
            case 3: return  x - y + 5.0f; //(-, -)
            default:
                assert false : signs; //Shouldn't happen.
                return Float.NaN;
        }
    }
    
    /**
     * The significand of a normal number has an implicit MSB (most significant
     * bit) of one. All other numbers, including zero, are subnormal.
     * 
     * @param  f a floating point number.
     * @return {@code true} if {@code f} is subnormal; {@code false} otherwise.
     */
    public static boolean isSubnormal(float f)
    {
        return Math.abs(f) < Float.MIN_NORMAL;
    }
    
    /**
     * Returns whether the two given values are close together.
     * 
     * @param f0 The first number to check.
     * @param f1 The second number to check.
     * @param threshold The greatest distance at which the values are equal.
     * @return Whether the two values are close together.
     */
    public static boolean equals(float f0, float f1, float threshold)
    {
        return Math.abs(f0 - f1) <= threshold;
    }
    
    /**
     * Checks if {@code f} is close to zero. Threshold needs to be managed by
     * the user, because floats are arbitrarily precise near zero and there's no
     * way to tell how much error has accumulated in {@code f}.
     * 
     * @param  f a floating point number.
     * @param  threshold the number to compare against.
     * @return {@code true} if the magnitude of {@code f} is less than
     *         {@code threshold}; {@code false} otherwise.
     */
    public static boolean isZero(float f, float threshold)
    {
        if (threshold <= 0.0f) return f == 0.0f;
        return Math.abs(f) <= threshold;
    }
    
    /**
     * Checks if {@code f} is close to zero. Threshold needs to be managed by
     * the user, because floats are arbitrarily precise near zero and there's no
     * way to tell how much error has accumulated in {@code f}.
     * 
     * @param  f a floating point number.
     * @param  threshold the number to compare against.
     * @return {@code true} if the magnitude of {@code f} is less than
     *         {@code threshold}; {@code false} otherwise.
     */
    public static boolean isZero(double f, double threshold)
    {
        if (threshold <= 0.0) return f == 0.0;
        return Math.abs(f) <= threshold;
    }
    
    /**
     * Returns a random positive float, greater than or equal to {@code 0.0} and
     * less than {@code 1.0}.
     */
    public static float random()
    {
        return (float)Math.random();
    }
    
    /**
     * Creates a "dead zone" around the origin, within which the result will be
     * set to zero. Useful for controller input from analog sticks.
     * 
     * @param v The vector to process.
     * @param threshold The radius of the dead-zone around the origin.
     * @param result The vector in which to store the result.
     */
    public static void deadZone(Vec2 v, float threshold, Vec2 result)
    {
        float length = v.length();
        if (length < threshold) result.set();
        else if (length < 1.0f) Vec2.mult(v, (length - threshold)/(1.0f - threshold), result);
        else Vec2.div(v, length, result);
    }
    
    /**
     * Don't let anyone instantiate this.
     */
    private Util()
    {
    }
}
