package com.samrj.devil.math;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Util
{
    // <editor-fold defaultstate="collapsed" desc="Globals">
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
        
        public boolean isType(Object o)
        {
            return type.isAssignableFrom(o.getClass());
        }
    }
    
    public static int sizeof(PrimType type)
    {
        return type.size;
    }
    
    public static enum Axis
    {
        X(1, 0, 0),
        Y(0, 1, 0),
        Z(0, 0, 1);
        
        private final Vector3f dir;
        
        private Axis(float x, float y, float z)
        {
            this.dir = new Vector3f(x, y, z);
        }
        
        public Vector3f versor()
        {
            return dir.clone();
        }
    }
    
    public static final float PIm2 = (float)(Math.PI*2.0);
    public static final float PI = (float)Math.PI;
    public static final float PId2 = (float)(Math.PI*0.5);
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="lang.Math Wrappers">
    public static int signum(float v)
    {
        if (v > 0) return 1;
        if (v == 0) return 0;
        return -1;
    }
    
    public static int floor(float v)
    {
        int vi = (int) v;
        return v < vi ? vi - 1 : vi;
    }
    
    public static int ceil(float v)
    {
        int vi = (int) v;
        return v < vi ? vi : vi + 1;
    }
    
    public static int round(float v)
    {
        return floor(v + .5f);
    }
    
    public static float pow(float v, float p)
    {
        return (float)Math.pow(v, p);
    }
    
    public static float sqrt(float v)
    {
        return (float)Math.sqrt(v);
    }
    
    public static float toRadians(float a)
    {
        return a*(PI/180f);
    }
    
    public static float toDegrees(float a)
    {
        return a*(180f/PI);
    }
    
    public static float sin(float a)
    {
        return (float)Math.sin(toRadians(a));
    }
    
    public static float asin(float v)
    {
        return toDegrees((float)Math.asin(v));
    }
    
    public static float cos(float a)
    {
        return (float)Math.cos(toRadians(a));
    }
    
    public static float acos(float v)
    {
        return toDegrees((float)Math.acos(v));
    }
    
    public static float tan(float a)
    {
        return (float)Math.tan(toRadians(a));
    }
    
    public static float atan2(float y, float x)
    {
        return toDegrees((float)Math.atan2(y, x));
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Float Math">
    public static float[] quadFormula(float a, float b, float c)
    {
        float discriminant = b*b - 4f*a*c;
        
        if (discriminant < 0f) return new float[0];
        if (discriminant == 0f) return new float[] {-b/(a*2f)};
        
        float sqrtDisc = (float)Math.sqrt(discriminant);
        float a2 = a*2f;
        return new float[] {(-b - sqrtDisc)/a2, (sqrtDisc - b)/a2};
    }
    
    public static float move(float f, float target, float dist)
    {
        float d = target - f;
        
        if (Math.abs(d) <= dist) return target;
        
        return f + dist*signum(d);
    }
    
    public static Vector2f move(Vector2f v, Vector2f target, float dist)
    {
        Vector2f dif = target.csub(v);
        float d = dif.length();
        if (d <= dist)
        {
            v.set(target);
            return v;
        }
        
        dif.div(d).mult(dist);
        return v.add(dif);
    }
    
    public static float clamp(float f, float min, float max)
    {
        if (min > max) throw new IllegalArgumentException();
        
        if (f < min) return min;
        if (f > max) return max;
        return f;
    }
    
    public static Vector2f clamp(Vector2f v, float maxLength)
    {
        float len = v.squareLength();
        if (len <= maxLength*maxLength) return v;
        
        len = sqrt(len);
        
        return v.mult(maxLength/len);
    }
    
    public static float loop(float x, float min, float max)
    {
        if (min > max) throw new IllegalArgumentException();
        if (max == min) return min;
        
        if (x >= min && x < max) return x;
        
        float t = (x - min)%(max - min);
        return t<0f ? t+max : t+min;
    }
    
    public static float loop(float x, float max)
    {
        if (max < 0f) throw new IllegalArgumentException();
        if (max == 0f) return 0f;
        
        if (x < max) return x;
        
        float t = x%max;
        return t<0f ? t+max : t;
    }
    
    public static Vector2i loop(Vector2f v, float min, float max)
    {
        float ox = v.x;
        float oy = v.y;
        
        v.loop(min, max);
        
        if (v.x == ox && v.y == oy) return new Vector2i();
        
        final float size = max-min;
        ox = (ox - v.x)/size;
        oy = (oy - v.y)/size;
        
        return new Vector2i(ceil(ox), ceil(oy));
    }
    
    public static Vector3i loop(Vector3f v, float min, float max)
    {
        float ox = v.x;
        float oy = v.y;
        float oz = v.z;
        
        v.loop(min, max);
        
        if (v.x == ox && v.y == oy && v.z == oz) return Vector3i.zero();
        
        final float size = max-min;
        ox = (ox - v.x)/size;
        oy = (oy - v.y)/size;
        oz = (oz - v.z)/size;
        
        return new Vector3i(round(ox), round(oy), round(oz));
    }
    
    public static float invLerp(float a, float b, float x)
    {
        return (x - a)/(b - a);
    }
    
    public static float lerp(float a, float b, float t)
    {
        return a + t*(b - a);
    }
    
    public static float remap(float v, float min0, float max0, float min1, float max1)
    {
        return (max1 - min1)*(v - min0)/(max0 - min0) + min1;
    }
    
    public static float clampRemap(float v, float min0, float max0, float min1, float max1)
    {
        return clamp(remap(v, min0, max0, min1, max1), min1, max1);
    }
    
    /**
     * If x > m, returns x, otherwise, blend smoothly into n.
     * 
     * Useful for values that should never reach zero, where n =/= zero.
     * 
     * f(0) = n
     * f(m) = m
     * f’(0) = 0
     * f’(m) = 1
     * 
     * Thanks to:
     * http://www.iquilezles.org/www/articles/functions/functions.htm
     */
    public static float nonzero( float x, float m, float n )
    {
        if(x > m) return x;

        float a = 2f*n - m;
        float b = 2f*m - 3f*n;
        float t = x/m;

        return (a*t + b)*t*t + n;
    }
    
    /**
     * Returns f(x) such that f(0) = 1 and f(1) = 0. Higher exponents make the
     * curve sharper near x = 1. Lower makes it sharper near x = 0. An exponent
     * of 1 returns clamp(1f - x, 0f, 1f).
     */
    public static float attenuate(float x, float exp)
    {
        if (exp == 1f) return clamp(1f - x, 0f, 1f);
        return 1f - pow(clamp(x, 0f, 1f), exp);
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
    public static float getEpsilon(float f)
    {
        if (Float.isNaN(f)) return Float.NaN;
        if (Float.isInfinite(f)) return Float.POSITIVE_INFINITY;
        if (!isSubnormal(f)) return Float.MIN_VALUE;
        
        //Isolate exponent.
        final int exp = (Float.floatToRawIntBits(f) & 0x7F800000) >>> 23;
        
        //f is the smallest number with a normal epsilon.
        if (exp == 24) return Float.MIN_NORMAL;
        //f may still be normal, but its epsilon is subnormal.
        if (exp < 24) return Float.intBitsToFloat(1 << (exp - 1));
        
        //Lower exp such that new MSB is old LSF, and return float.
        return Float.intBitsToFloat((exp - 23) << 23);
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
     *         may be apart and still be approximately equal.
     * @return {@code true} if {@code a} and {@code b} are approximately equal;
     *         {@code false} otherwise.
     * @see    gameutil.math.Util#getEpsilon(float)
     */
    public static boolean epsEqual(float a, float b, int tolerance)
    {
        if (a == b) return true;
        if (tolerance <= 0) return false;
        
        //Deal with the near-zero numbers.
        final boolean sna = isSubnormal(a), snb = isSubnormal(b);
        if (sna && snb) return true;
        if (sna || snb) return false;
        
        //Simply use the greater number's epsilon.
        final float epsilon = a > b ? getEpsilon(a) : getEpsilon(b);
        
        return Math.abs(a - b) <= epsilon*tolerance;
    }
    
    public static boolean isFinite(float x)
    {
        return !(Float.isInfinite(x) || Float.isNaN(x));
    }
    
    /**
     * @return  a negative integer, zero, or a positive integer as {@code a}
     *          is less than, equal to, or greater than {@code b}.
     */
    public static int compare(float a, float b)
    {
        if (a == b) return 0;
        return a < b ? -1 : 1;
    }
    
    public static float reduceRad(float angle)
    {
        return loop(angle, -PI, PI);
    }
    
    public static float reduceDeg(float angle)
    {
        return loop(angle, -180, 180);
    }
    
    public static float floor(float f, float size)
    {
        return Util.floor(f/size)*size;
    }
    
    public static float round(float f, float size)
    {
        return Util.round(f/size)*size;
    }
    
    public static Vector2f round(Vector2f f, float size)
    {
        return f.set(round(f.x, size), round(f.y, size));
    }
    
    public static float ceil(float f, float size)
    {
        return Util.ceil(f/size)*size;
    }
    
    public static int indexMin(float... values)
    {
        if (values.length == 0) throw new IllegalArgumentException();
        if (values.length == 1) return 0;
        
        float min = values[0];
        int out = 0;
        
        for (int i=1; i<values.length; i++) if (values[i] < min)
        {
            min = values[i];
            out = i;
        }
        
        return out;
    }
    
    public static float min(float... values)
    {
        return values[indexMin(values)];
    }
    
    public static int indexMax(float... values)
    {
        if (values.length == 0) throw new IllegalArgumentException();
        if (values.length == 1) return 0;
        
        float max = values[0];
        int out = 0;
        
        for (int i=1; i<values.length; i++) if (values[i] > max)
        {
            max = values[i];
            out = i;
        }
        
        return out;
    }
    
    public static float max(float... values)
    {
        return values[indexMax(values)];
    }
    
    public static boolean isInteger(float f)
    {
        return f == floor(f);
    }
    
    public static float reduceSq(float sqa)
    {
        return Util.loop(sqa, 8f);
    }
    
    public static float sqSin(float sqa)
    {
        sqa = reduceSq(sqa);
        
        if (sqa >= 7f) return sqa - 8f;
        if (sqa >= 5f) return -1f;
        if (sqa >= 3f) return 4f - sqa;
        if (sqa >= 1f) return 1f;
        return sqa;
    }
    
    public static float sqCos(float sqa)
    {
        sqa = reduceSq(sqa);
        
        if (sqa >= 7f) return 1f;
        if (sqa >= 5f) return sqa - 6f;
        if (sqa >= 3f) return -1f;
        if (sqa >= 1f) return 2f - sqa;
        return 1f;
    }
    
    public static Vector2f sqDir(float sqa)
    {
        return new Vector2f(sqCos(sqa), sqSin(sqa));
    }
    
    public static float sqAtan2(float y, float x)
    {
        float chebLen = max(Math.abs(x), Math.abs(y));
        y /= chebLen; x /= chebLen;
        
        int signs = (x >= 0f ? 0 : 1) | (y >= 0f ? 0 : 2);
        
        switch (signs)
        {
            case 0: return -x + y + 1f; //(+, +)
            case 1: return -x - y + 3f; //(-, +)
            case 2: return  x + y + 7f; //(+, -)
            case 3: return  x - y + 5f; //(-, -)
            default: return -1f;
        }
    }
    
    /**
     * Returns, in degrees, the signed angle between two normalized vectors.
     */
    public static float angleNrm(Vector2f a, Vector2f b)
    {
        float sin = b.cross(a);
        float cos = a.dot(b);
        return atan2(sin, cos);
    }
    
    /**
     * Returns, in degrees, the signed angle between two vectors.
     */
    public static float angle(Vector2f a, Vector2f b)
    {
        return angleNrm(a.cnormalize(), b.cnormalize());
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Integer Math">
    public static int clamp(int i, int min, int max)
    {
        if (min > max) throw new IllegalArgumentException();
        
        if (i < min) return min;
        else if (i > max) return max;
        return i;
    }
    
    public static int loop(int x, int min, int max)
    {
        if (max < min) throw new IllegalArgumentException();
        if (max == min) return min;
        
        if (x >= min && x < max) return x;
        
        int t = (x - min)%(max - min);
        return t<0 ? t+max : t+min;
    }
    
    public static boolean isPower2(int n)
    {
        return (n & (n - 1)) == 0;
    }
    
    public static int indexMax(int... values)
    {
        if (values.length == 0) throw new IllegalArgumentException();
        if (values.length == 1) return 0;
        
        int max = values[0];
        int out = 0;
        
        for (int i=1; i<values.length; i++) if (values[i] > max)
        {
            max = values[i];
            out = i;
        }
        
        return out;
    }
    
    public static int max(int... values)
    {
        return values[indexMax(values)];
    }
    // </editor-fold>
    
    /**
     * Don't let anyone instantiate this.
     */
    private Util() {}
}
