package com.samrj.devil.math;

/**
 * Two-dimensional vector.
 * 
 * @author SmashMaster
 * @copyright 2015 Samuel Johnson
 */
public class Vec2
{
    //INCOMPLETE
    
    // <editor-fold defaultstate="collapsed" desc="Static modifier methods">
    /**
     * Adds {@code v0} and {@code v1} and stores the result in {@code result}.
     * 
     * @param v0 The first addend.
     * @param v1 The second addend.
     * @param result The vector in which to store the sum of {@code v0} and {@code v1}.
     */
    public static final void add(Vec2 v0, Vec2 v1, Vec2 result)
    {
        result.x = v0.x + v1.x;
        result.y = v0.y + v1.y;
    }
    
    /**
     * Subtracts {@code v1} from {@code v0} and stores the result in {@code result}.
     * 
     * @param v0 The vector to subtract from.
     * @param v1 The vector to subtract by.
     * @param result The vector in which to store the result.
     */
    public static final void sub(Vec2 v0, Vec2 v1, Vec2 result)
    {
        result.x = v0.x - v1.x;
        result.y = v0.y - v1.y;
    }
    
    /**
     * Multiplies {@code v} by {@code s} and stores the result in {@code result}.
     * 
     * @param v The vector to multiply.
     * @param s The scalar to multiply the vector by.
     * @param result The vector in which to store the result.
     */
    public static final void mult(Vec2 v, float s, Vec2 result)
    {
        result.x = v.x*s;
        result.y = v.y*s;
    }
    
    /**
     * Multiplies {@code v} by {@code m} and stores the result in {@code result}.
     * 
     * @param v The vector to multiply.
     * @param m The 2x2 matrix to multiply the vector by.
     * @param result The vector in which to store the result.
     */
    public static final void mult(Vec2 v, Mat2 m, Vec2 result)
    {
        final float x = v.x*m.a + v.y*m.b;
        final float y = v.x*m.c + v.y*m.d;
        result.x = x; result.y = y;
    }
    
    /**
     * Multiplies {@code v} by {@code m} and stores the result in {@code result}.
     * 
     * @param v The vector to multiply.
     * @param m The 3x3 matrix to multiply the vector by.
     * @param result The vector in which to store the result.
     */
    public static final void mult(Vec2 v, Mat3 m, Vec2 result)
    {
        final float x = v.x*m.a + v.y*m.b + m.c;
        final float y = v.x*m.d + v.y*m.e + m.f;
        result.x = x; result.y = y;
    }
    
    /**
     * Divides {@code v} by {@code s} and stores the result in {@code result}.
     * 
     * @param v The vector to divide.
     * @param s The scalar to divide the vector by.
     * @param result The vector in which to store the result.
     */
    public static final void div(Vec2 v, float s, Vec2 result)
    {
        result.x = v.x/s;
        result.y = v.y/s;
    }
    
    /**
     * Sets the length of the given vector to one and stores the result in
     * {@code result}. Has undefined behavior if the length of the given vector
     * is zero.
     * 
     * @param v The vector to normalize.
     * @param result The vector in which to store the result.
     */
    public static final void normalize(Vec2 v, Vec2 result)
    {
        div(v, v.length(), result);
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Static factory methods">
    /**
     * Returns the sum of {@code v0} and {@code v1} in a new vector.
     * 
     * @param v0 The first addend.
     * @param v1 The second addend.
     * @return A new vector containing the result.
     */
    public static final Vec2 add(Vec2 v0, Vec2 v1)
    {
        final Vec2 result = new Vec2();
        add(v0, v1, result);
        return result;
    }
    
    /**
     * Subtracts {@code v1} from {@code v0} and returns the result in a new vector.
     * 
     * @param v0 The vector to subtract from.
     * @param v1 The vector to subtract by.
     * @return A new vector containing the result.
     */
    public static final Vec2 sub(Vec2 v0, Vec2 v1)
    {
        final Vec2 result = new Vec2();
        sub(v0, v1, result);
        return result;
    }
    
    /**
     * Multiplies {@code v} by {@code s} and returns the result in a new vector.
     * 
     * @param v The vector to multiply.
     * @param s The scalar to multiply by.
     * @return A new vector containing the result.
     */
    public static final Vec2 mult(Vec2 v, float s)
    {
        final Vec2 result = new Vec2();
        mult(v, s, result);
        return result;
    }
    
    /**
     * Multiplies {@code v} by {@code m} and returns the result in a new vector.
     * 
     * @param v The vector to multiply.
     * @param m The 2x2 matrix to multiply by.
     * @return A new vector containing the result.
     */
    public static final Vec2 mult(Vec2 v, Mat2 m)
    {
        final Vec2 result = new Vec2();
        mult(v, m, result);
        return result;
    }
    
    /**
     * Multiplies {@code v} by {@code m} and returns the result in a new vector.
     * 
     * @param v The vector to multiply.
     * @param m The 3x3 matrix to multiply by.
     * @return A new vector containing the result.
     */
    public static final Vec2 mult(Vec2 v, Mat3 m)
    {
        final Vec2 result = new Vec2();
        mult(v, m, result);
        return result;
    }
    
    /**
     * Divides {@code v} by {@code s} and returns the result in a new vector.
     * 
     * @param v The vector to divide.
     * @param s The scalar to divide by.
     * @return A new vector containing the result.
     */
    public static final Vec2 div(Vec2 v, float s)
    {
        final Vec2 result = new Vec2();
        div(v, s, result);
        return result;
    }
    
    /**
     * Normalizes {@code v} and returns the result in a new vector.
     * 
     * @param v The vector to normalize.
     * @return The normalized vector.
     */
    public static final Vec2 normalize(Vec2 v)
    {
        final Vec2 result = new Vec2();
        normalize(v, result);
        return result;
    }
    // </editor-fold>
    
    /**
     * Returns the dot product of two given vectors.
     * 
     * @param v0 The first vector.
     * @param v1 The second vector.
     * @return The dot product of {@code v0} and {@code v1}.
     */
    public static final float dot(Vec2 v0, Vec2 v1)
    {
        return v0.x*v1.x + v0.y*v1.y;
    }
    
    public float x, y;
    
    /**
     * Creates a new vector whose coordinates are both zero.
     */
    public Vec2()
    {
    }
    
    /**
     * Creates a new vector with the given coordinates.
     * 
     * @param x The x coordinate of this.
     * @param y The y coordinate of this.
     */
    public Vec2(float x, float y)
    {
        this.x = x; this.y = y;
    }
    
    /**
     * Copies the given vector.
     * 
     * @param v The vector to copy.
     */
    public Vec2(Vec2 v)
    {
        x = v.x; y = v.y;
    }
    
    /**
     * Returns the square length of this vector. Can be alternately defined as
     * the dot product of this with itself.
     * 
     * @return The square length of this.
     */
    public float squareLength()
    {
        return x*x + y*y;
    }
    
    /**
     * Returns the length of this.
     * 
     * @return The length of this.
     */
    public float length()
    {
        return (float)Math.sqrt(x*x + y*y);
    }
    
    // <editor-fold defaultstate="collapsed" desc="Local modifier methods">
    /**
     * Sets this to the given vector.
     * 
     * @param v The vector to set this to.
     */
    public void set(Vec2 v)
    {
        x = v.x; y = v.y;
    }
    
    /**
     * Adds the given vector to this.
     * 
     * @param v The vector to add to this.
     */
    public void add(Vec2 v)
    {
        add(this, v, this);
    }
    
    /**
     * Subtracts the given vector from this.
     * 
     * @param v The vector to subtract from this.
     */
    public void sub(Vec2 v)
    {
        sub(this, v, this);
    }
    
    /**
     * Multiplies this by the given scalar.
     * 
     * @param s The scalar to multiply this by.
     */
    public void mult(float s)
    {
        mult(this, s, this);
    }
    
    /**
     * Multiplies this by the given 2x2 matrix.
     * 
     * @param m The 2x2 matrix to multiply this by.
     */
    public void mult(Mat2 m)
    {
        mult(this, m, this);
    }
    
    /**
     * Multiplies this by the given 3x3 matrix.
     * 
     * @param m The 3x3 matrix to multiply this by.
     */
    public void mult(Mat3 m)
    {
        mult(this, m, this);
    }
    
    /**
     * Divides this by the given scalar.
     * 
     * @param s The scalar to divide by.
     */
    public void div(float s)
    {
        div(this, s, this);
    }
    
    /**
     * Sets the length of this to one. Has undefined behavior if the current
     * length of this is zero or close to zero.
     */
    public void normalize()
    {
        normalize(this, this);
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Overriden Object methods">
    @Override
    public String toString()
    {
        return '(' + x + ", " + y + ')';
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (o == null) return false;
        if (o.getClass() != this.getClass()) return false;
        final Vec2 v = (Vec2)o;
        return v.x == x && v.y == y;
    }

    @Override
    public int hashCode()
    {
        int hash = 185 + Float.floatToIntBits(this.x);
        return 37*hash + Float.floatToIntBits(this.y);
    }
    // </editor-fold>
}
