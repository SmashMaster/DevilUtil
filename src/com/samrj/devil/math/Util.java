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
public class Util
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
    public static final int sizeof(PrimType type)
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
        n--;
        n |= n >> 1;
        n |= n >> 2;
        n |= n >> 4;
        n |= n >> 8;
        n |= n >> 16;
        n++;
        return n;
    }
    
    /**
     * Returns the first integer that is larger than the given float.
     * Has undefined behavior for NaN and the infinities.
     * 
     * @param f A finite float.
     * @return The first integer that is larger than f.
     */
    public static final int ceil(float f)
    {
        int i = (int)f;
        if (i < f) i++;
        return i;
    }
    
    /**
     * Returns the first integer that is smaller than the given float.
     * Has undefined behavior for NaN and the infinities.
     * 
     * @param f A finite float.
     * @return The first integer that is smaller than f.
     */
    public static final int floor(float f)
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
    public static final float lerp(float f0, float f1, float t)
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
    public static final float clamp(float x, float min, float max)
    {
        if (x < min) return min;
        if (x > max) return max;
        return x; //Implicitly handles NaN and the infinites.
    }
    
    /**
     * Returns the smaller of the two given values. Does not check for negative
     * zero or NaN.
     * 
     * @param x0 Any float.
     * @param x1 Any float.
     * @return The smaller of the two given values.
     */
    public static final float min(float x0, float x1)
    {
        return x0 <= x1 ? x0 : x1;
    }
    
    /**
     * Returns the greater of the two given values. Does not check for negative
     * zero or NaN.
     * 
     * @param x0 Any float.
     * @param x1 Any float.
     * @return The greater of the two given values.
     */
    public static final float max(float x0, float x1)
    {
        return x0 >= x1 ? x0 : x1;
    }
    
    /**
     * Loops the given value into the given range.
     * 
     * @param x The value to loop.
     * @param min The minimum output value, inclusive.
     * @param max The maximum output value, exclusive.
     * @return The value, looped to within the given range.
     */
    public static final float loop(float x, float min, float max)
    {
        if (x >= min && x < max) return x;
        
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
    public static final float loop(float x, float max)
    {
        if (x >= 0.0f && x < max) return x;
        
        float t = x%max;
        return t < 0.0f ? t + max : t;
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
    public static final int compare(float f0, float f1, float tolerance)
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
    public static final int compare(float f0, float f1)
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
    public static final float nonzero(float x, float m, float n)
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
    public static final float attenuate(float x, float exp)
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
    public static final float reduceAngle(float angle)
    {
        return loop(angle, -PI, PI);
    }
    
    /**
     * Converts an angle in degrees to radians.
     * 
     * @param a An angle in degrees.
     * @return The given angle in radians.
     */
    public static final float toRadians(float a)
    {
        return a*TO_RADIANS;
    }
    
    /**
     * Converts an angle in radians to degrees.
     * 
     * @param a An angle in radians.
     * @return The given angle in degrees.
     */
    public static final float toDegrees(float a)
    {
        return a*TO_DEGREES;
    }
    
    /**
     * Loop the given square angle into the range between
     * 
     * @param a The square angle to reduce.
     * @return  The reduced square angle.
     */
    public static final float reduceSquareAngle(float a)
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
    public static final float squareSin(float a)
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
    public static final float squareCos(float a)
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
    public static final Vec2 squareDir(float a)
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
    public static final float squareAtan2(float y, float x)
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
    public static final boolean isSubnormal(float f)
    {
        return Math.abs(f) < Float.MIN_NORMAL;
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
    public static final boolean isZero(float f, float threshold)
    {
        if (threshold <= 0f) return f == 0f;
        return Math.abs(f) < threshold;
    }
    
    /**
     * Returns the epsilon of {@code f}, which is a measure of the precision of
     * IEEE 754 binary32 relative to {@code f}. It is the smallest power of two
     * that can added to or subtracted from {@code f} to result in a different
     * value. If {@code f} is infinite, it has no finite epsilon.
     * Epsilons may be subnormal.
     * 
     * @param f a floating point number.
     * @return the epsilon of {@code f} if it is finite; Float.NaN if it is NaN;
     *         or Float.POSITIVE_INFINITY if it is infinite.
     */
    public static final float getEpsilon(float f)
    {
        if (Float.isNaN(f)) return Float.NaN;
        if (Float.isInfinite(f)) return Float.POSITIVE_INFINITY;
        
        float finc = Float.intBitsToFloat(Float.floatToRawIntBits(f) + 1);
        //The first number whose absolute value is greater than f's.
        
        return Math.abs(finc - f); //Automatically works on subnormal values.
    }
    
    /**
     * Returns whether or not {@code a} and {@code b} are approximately equal,
     * based on their epsilons and a tolerance factor. If the two numbers have
     * different exponents, then the greater epsilon is used to determine
     * equality.
     * 
     * @param  a the first float to be compare.
     * @param  b the second float to be compare.
     * @param  tolerance the number of epsilons by which {@code a} and {@code b}
     *         may differ and still be approximately equal.
     * @return {@code true} if {@code a} and {@code b} are approximately equal;
     *         {@code false} otherwise.
     * @see    com.samrj.devil.math.Util#getEpsilon(float)
     */
    public static final boolean epsEqual(float a, float b, int tolerance)
    {
        if (a == b) return true;
        if (tolerance <= 0) return false;
        
        //Deal with the near-zero numbers.
        final boolean sna = isSubnormal(a), snb = isSubnormal(b);
        if (sna && snb) return true;
        if (sna || snb) return false;
        
        //Simply use the greater number's epsilon.
        float epsilon = a > b ? getEpsilon(a) : getEpsilon(b);
        
        return Math.abs(a - b) <= epsilon*tolerance;
    }
    
    /**
     * Returns whether or not the given value is finite.
     * 
     * @param x Any float.
     * @return False if the value is infinite or NaN, true otherwise.
     */
    public static final boolean isFinite(float x)
    {
        return !(Float.isInfinite(x) || Float.isNaN(x));
    }
    
    /**
     * Don't let anyone instantiate this.
     */
    private Util()
    {
    }
}
