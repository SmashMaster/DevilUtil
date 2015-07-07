package com.samrj.devil.math;

public class Quat
{
    private static final Quat tempQuat = new Quat();
    
    // <editor-fold defaultstate="collapsed" desc="Static accessor methods">
    /**
     * Returns the dot product of the two given quaternions.
     * 
     * @param q0 The first quaternion to multiply.
     * @param q1 The second quaternion to multiply.
     * @return The dot product of the two given quaternions.
     */
    public static final float dot(Quat q0, Quat q1)
    {
        return q0.w*q1.w + q0.x*q1.x + q0.y*q1.y + q0.z*q1.z;
    }
    
    /**
     * Returns the square length of the given quaternion. May be alternately
     * defined as the dot product of the quaternion with itself.
     * 
     * @param q A quaternion.
     * @return The dot product of the given quaternion.
     */
    public static final float squareLength(Quat q)
    {
        return q.w*q.w + q.x*q.x + q.y*q.y + q.z*q.z;
    }
    
    /**
     * Returns the length of the given quaternion.
     * 
     * @param q A quaternion.
     * @return The length of the given quaternion.
     */
    public static final float length(Quat q)
    {
        return (float)Math.sqrt(squareLength(q));
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Static mutator methods">
    /**
     *  Copies {@code src} into {@code tgt}.
     * 
     * @param src The quaternion to copy.
     * @param tgt The quaternion to copy into.
     */
    public static final void copy(Quat src, Quat tgt)
    {
        tgt.w = src.w; tgt.x = src.x; tgt.y = src.y; tgt.z = src.z;
    }
    
    /**
     * Multiplies {@code q0} by {@code q1} and stores the result in {@code r}.
     * 
     * @param q0 The left-hand quaternion to multiply.
     * @param q1 The right-hand quaternion to multiply by.
     * @param r The quaternion in which to store the result.
     */
    public static final void mult(Quat q0, Quat q1, Quat r)
    {
        float w = q0.w*q1.w - q0.x*q1.x - q0.y*q1.y - q0.z*q1.z;
        float x = q0.w*q1.x + q0.x*q1.w + q0.y*q1.z - q0.z*q1.y;
        float y = q0.w*q1.y - q0.x*q1.z + q0.y*q1.w + q0.z*q1.x;
        float z = q0.w*q1.z + q0.x*q1.y - q0.y*q1.x + q0.z*q1.w;
        
        r.w = w; r.x = x; r.y = y; r.z = z;
    }
    
    /**
     * Adds the two given quaternions and stores the result in {@code r}.
     * 
     * @param q0 The first quaternion to add.
     * @param q1 The second quaternion to add.
     * @param r The quaternion in which to store the result.
     */
    public static final void add(Quat q0, Quat q1, Quat r)
    {
        r.w = q0.w+q1.w;
        r.x = q0.x+q1.x;
        r.y = q0.y+q1.y;
        r.z = q0.z+q1.z;
    }
    
    /**
     * Subtracts {@code q1} from {@code q0} and stores the result in {@code r}.
     * 
     * @param q0 The quaternion to subtract from.
     * @param q1 The quaternion to subtract by.
     * @param r The quaternion in which to store the result.
     */
    public static final void sub(Quat q0, Quat q1, Quat r)
    {
        r.w = q0.w-q1.w;
        r.x = q0.x-q1.x;
        r.y = q0.y-q1.y;
        r.z = q0.z-q1.z;
    }
    
    /**
     * Multiplies the given quaternion by the given scalar and stores the result
     * in {@code r}.
     * 
     * @param q The quaternion to multiply.
     * @param s The scalar to multiply by.
     * @param r The quaternion in which to store the result.
     */
    public static final void mult(Quat q, float s, Quat r)
    {
        r.w = q.w*s;
        r.x = q.x*s;
        r.y = q.y*s;
        r.z = q.z*s;
    }
    
    /**
     * Divides the given quaternion by the given scalar and stores the result
     * in {@code r}.
     * 
     * @param q The quaternion to multiply.
     * @param s The scalar to multiply by.
     * @param r The quaternion in which to store the result.
     */
    public static final void div(Quat q, float s, Quat r)
    {
        r.w = q.w/s;
        r.x = q.x/s;
        r.y = q.y/s;
        r.z = q.z/s;
    }
    
    /**
     * Negates this quaternion and stores the result in {@code r}.
     * 
     * @param q The quaternion to negate.
     * @param r The quaternion in which to store the result.
     */
    public static final void negate(Quat q, Quat r)
    {
        r.w = -q.w;
        r.x = -q.x;
        r.y = -q.y;
        r.z = -q.z;
    }
    
    /**
     * Performs a spherical linear interpolation between the two given
     * quaternions and stores the result in {@code r}.
     * 
     * @param q0 The 'start' quaternion to interpolate from.
     * @param q1 The 'end' quaternion to interpolate to.
     * @param t The scalar interpolant, between zero and one (inclusive).
     * @param r The quaternion in which to store the result.
     */
    public static final void slerp(Quat q0, Quat q1, float t, Quat r)
    {
        float cos = dot(q0, q1);
        if (cos < 0f)
        {
            negate(q1, tempQuat);
            cos = -cos;
        }
        else copy(q1, tempQuat);
        
        throw new UnsupportedOperationException();
        
    }
    // </editor-fold>
    
    public float w, x, y, z;
    
    /**
     * Creates a zero quaternion. NOT the identity quaternion.
     */
    public Quat()
    {
    }
    
    public Quat(float w, float x, float y, float z)
    {
        this.w = w; this.x = x; this.y = y; this.z = z;
    }
    
    public Quat(Quat q)
    {
        w = q.w; x = q.x; y = q.y; z = q.z;
    }
}
