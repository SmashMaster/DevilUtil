package com.samrj.devil.math;

/**
 * Three-dimensional vector.
 * 
 * @author SmashMaster
 * @copyright 2015 Samuel Johnson
 */
public class Vec3
{
    ~~INCOMPLETE~~
    
    // <editor-fold defaultstate="collapsed" desc="Static modifier methods">
    /**
     * Adds {@code v0} and {@code v1} and stores the result in {@code result}.
     * 
     * @param v0 The first addend.
     * @param v1 The second addend.
     * @param result The vector in which to store the sum of {@code v0} and {@code v1}.
     */
    public static final void add(Vec3 v0, Vec3 v1, Vec3 result)
    {
        result.x = v0.x + v1.x;
        result.y = v0.y + v1.y;
        result.z = v0.z + v1.z;
    }
    
    /**
     * Subtracts {@code v1} from {@code v0} and stores the result in {@code result}.
     * 
     * @param v0 The vector to subtract from.
     * @param v1 The vector to subtract by.
     * @param result The vector in which to store the result.
     */
    public static final void sub(Vec3 v0, Vec3 v1, Vec3 result)
    {
        result.x = v0.x - v1.x;
        result.y = v0.y - v1.y;
        result.z = v0.z - v1.z;
    }
    
    /**
     * Multiplies {@code v} by {@code s} and stores the result in {@code result}.
     * 
     * @param v The vector to multiply.
     * @param s The scalar to multiply the vector by.
     * @param result The vector in which to store the result.
     */
    public static final void mult(Vec3 v, float s, Vec3 result)
    {
        result.x = v.x*s;
        result.y = v.y*s;
        result.z = v.z*s;
    }
    
    /**
     * Multiplies {@code v} by {@code m} and stores the result in {@code result}.
     * 
     * @param v The vector to multiply.
     * @param m The 3x3 matrix to multiply the vector by.
     * @param result The vector in which to store the result.
     */
    public static final void mult(Vec3 v, Mat3 m, Vec3 result)
    {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Multiplies {@code v} by {@code m} and stores the result in {@code result}.
     * 
     * @param v The vector to multiply.
     * @param m The 4x4 matrix to multiply the vector by.
     * @param result The vector in which to store the result.
     */
    public static final void mult(Vec3 v, Mat4 m, Vec3 result)
    {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Divides {@code v} by {@code s} and stores the result in {@code result}.
     * 
     * @param v The vector to divide.
     * @param s The scalar to divide the vector by.
     * @param result The vector in which to store the result.
     */
    public static final void div(Vec3 v, float s, Vec3 result)
    {
        result.x = v.x/s;
        result.y = v.y/s;
        result.z = v.z/s;
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Static constructor methods">
    // </editor-fold>
    /**
     * Returns the dot product of two given vectors.
     * 
     * @param v0 The first vector.
     * @param v1 The second vector.
     * @return The dot product of {@code v0} and {@code v1}.
     */
    public static float dot(Vec3 v0, Vec3 v1)
    {
        return v0.x*v1.x + v0.y*v1.y + v0.z*v1.z;
    }
    
    public float x, y, z;
    
    /**
     * Creates a new vector whose coordinates are zero.
     */
    public Vec3()
    {
    }
    
    /**
     * Creates a new vector with the given coordinates.
     * 
     * @param x The x coordinate of this.
     * @param y The y coordinate of this.
     * @param z The z coordinate of this.
     */
    public Vec3(float x, float y, float z)
    {
        this.x = x; this.y = y; this.z = z;
    }
    
    /**
     * Copies the given vector.
     * 
     * @param v The vector to copy.
     */
    public Vec3(Vec3 v)
    {
        x = v.x; y = v.y; z = v.z;
    }
    
    /**
     * Returns the length of this.
     * 
     * @return The length of this.
     */
    public float length()
    {
        return (float)Math.sqrt(x*x + y*y + z*z);
    }
    
    // <editor-fold defaultstate="collapsed" desc="Local modifier methods">
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Overriden Object methods">
    @Override
    public String toString()
    {
        return '(' + x + ", " + y + ", " + z + ')';
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (o == null) return false;
        if (o.getClass() != this.getClass()) return false;
        final Vec3 v = (Vec3)o;
        return v.x == x && v.y == y && v.z == z;
    }
    
    @Override
    public int hashCode()
    {
        int hash = 161 + Float.floatToIntBits(this.x);
        hash = 23*hash + Float.floatToIntBits(this.y);
        return 23*hash + Float.floatToIntBits(this.z);
    }
    // </editor-fold>
}
