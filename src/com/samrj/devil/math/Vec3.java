package com.samrj.devil.math;

import com.samrj.devil.io.Bufferable;
import com.samrj.devil.io.Streamable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.FloatBuffer;

/**
 * Optimized three-dimensional vector class. The design of this class is based
 * around minimizing overhead whilst improving readability. This class is very
 * lightweight and, after JVM optimization, should have comparable performance
 * to most native libraries.
 * 
 * Many methods are static and final that wouldn't normally be. This is to
 * encourage JVM inlining, because instance methods can be polymorphic.
 * The class itself is not final, to allow for user extension.
 * 
 * Local accessor and mutator methods are provided for user convenience, but the
 * static methods should be used if optimal performance is desired.
 * 
 * The data fields are public to increase readability and decrease overhead.
 * 
 * Manual inlining is avoided. The JVM should inline any code where enough time
 * is being spent.
 * 
 * No instance method should ever construct a new {@code Vec3}--a constructor or
 * static factory method must be called to do so.
 * 
 * @author SmashMaster
 * @copyright 2015 Samuel Johnson
 */
public class Vec3 implements Bufferable<FloatBuffer>, Streamable
{
    // <editor-fold defaultstate="collapsed" desc="Static accessor methods">
    /**
     * Returns the dot product of two given vectors.
     * 
     * @param v0 The first vector.
     * @param v1 The second vector.
     * @return The dot product of {@code v0} and {@code v1}.
     */
    public static final float dot(Vec3 v0, Vec3 v1)
    {
        return v0.x*v1.x + v0.y*v1.y + v0.z*v1.z;
    }
    
    /**
     * Returns the square length of the given vector. Can be alternately defined
     * as the dot product of the vector with itself.
     * 
     * @param v The vector to calculate the square length of.
     * @return The square length of {@code v}.
     */
    public static final float squareLength(Vec3 v)
    {
        return v.x*v.x + v.y*v.y + v.z*v.z;
    }
    
    /**
     * Returns the length of the given vector.
     * 
     * @param v The vector to calculate the length of.
     * @return The length of {@code v}.
     */
    public static final float length(Vec3 v)
    {
        return (float)Math.sqrt(squareLength(v));
    }
    
    /**
     * Returns the square distance between two given vectors.
     *  
     * @param v0 The first vector.
     * @param v1 The second vector.
     * @return The square distance between {@code v0} and {@code v1}.
     */
    public static final float squareDist(Vec3 v0, Vec3 v1)
    {
        float dx = v1.x - v0.x;
        float dy = v1.y - v0.y;
        float dz = v1.z - v0.z;
        return dx*dx + dy*dy + dz*dz;
    }
    
    /**
     * Returns the distance between two given vectors.
     *  
     * @param v0 The first vector.
     * @param v1 The second vector.
     * @return The distance between {@code v0} and {@code v1}.
     */
    public static final float dist(Vec3 v0, Vec3 v1)
    {
        return (float)Math.sqrt(squareDist(v0, v1));
    }
    
    /**
     * Returns the scalar projection of {@code v0} onto {@code v1}. {@code v1}
     * need not be normalized.
     * 
     * @param v0 The vector to project.
     * @param v1 The vector on which to project.
     * @return The scalar projection of {@code v0} onto {@code v1}.
     */
    public static final float scalarProject(Vec3 v0, Vec3 v1)
    {
        return dot(v0, v1)/length(v1);
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Static mutator methods">
    /**
     * Copies {@code source} into {@code target}.
     * 
     * @param source The vector to copy.
     * @param target The vector in which to store the result.
     */
    public static final void copy(Vec3 source, Vec3 target)
    {
        target.x = source.x;
        target.y = source.y;
        target.z = source.z;
    }
    
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
        float x = m.a*v.x + m.b*v.y + m.c*v.z;
        float y = m.d*v.x + m.e*v.y + m.f*v.z;
        float z = m.g*v.x + m.h*v.y + m.i*v.z;
        result.x = x; result.y = y; result.z = z;
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
        float x = m.a*v.x + m.b*v.y + m.c*v.z + m.d;
        float y = m.e*v.x + m.f*v.y + m.g*v.z + m.h;
        float z = m.i*v.x + m.j*v.y + m.k*v.z + m.l;
        result.x = x; result.y = y; result.z = z;
    }
    
    /**
     * Calculates the cross product between {@code v0} and {@code v1} then stores
     * the result in {@code result}.
     * 
     * @param v0 The vector to multiply.
     * @param v1 The vector to multiply by.
     * @param result The vector in which to store the result.
     */
    public static final void cross(Vec3 v0, Vec3 v1, Vec3 result)
    {
        float x = v0.y*v1.z - v0.z*v1.y;
        float y = v0.z*v1.x - v0.x*v1.z;
        float z = v0.x*v1.y - v0.y*v1.x;
        result.x = x; result.y = y; result.z = z;
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
    
    /**
     * Sets the length of the given vector to one and stores the result in
     * {@code result}. Has undefined behavior if the length of the given vector
     * is zero.
     * 
     * @param v The vector to normalize.
     * @param result The vector in which to store the result.
     */
    public static final void normalize(Vec3 v, Vec3 result)
    {
        div(v, length(v), result);
    }
    
    /**
     * Reflects {@code v} about {@code n} and stores the result in {@code result}.
     * {@code n} must be normalized.
     * 
     * @param v The vector to reflect.
     * @param n The normal vector about which to reflect.
     * @param result The vector in which to store the result.
     */
    public static final void reflect(Vec3 v, Vec3 n, Vec3 result)
    {
        float m = 2f*dot(v, n);
        result.x = n.x*m - v.x;
        result.y = n.y*m - v.y;
        result.z = n.z*m - v.z;
    }
    
    /**
     * Performs a vector projection of {@code v0} onto {@code v1} and stores the
     * result in {@code result}. {@code v1} need not be normalized.
     * 
     * @param v0 The vector to project.
     * @param v1 The vector on which to project.
     * @param result The vector in which to store the result.
     */
    public static final void project(Vec3 v0, Vec3 v1, Vec3 result)
    {
        mult(v1, dot(v0, v1)/squareLength(v1), result);
    }
    
    /**
     * Performs a vector rejection of {@code v1} from {@code v0} and stores the
     * result in {@code result}.
     * 
     * @param v0 The vector to reject from.
     * @param v1 The vector to reject by.
     * @param result The vector in which to store the result.
     */
    public static final void reject(Vec3 v0, Vec3 v1, Vec3 result)
    {
        project(v0, v1, result);
        sub(v0, result, result);
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
    public static final Vec3 add(Vec3 v0, Vec3 v1)
    {
        final Vec3 result = new Vec3();
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
    public static final Vec3 sub(Vec3 v0, Vec3 v1)
    {
        final Vec3 result = new Vec3();
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
    public static final Vec3 mult(Vec3 v, float s)
    {
        final Vec3 result = new Vec3();
        mult(v, s, result);
        return result;
    }
    
    /**
     * Multiplies {@code v} by {@code m} and returns the result in a new vector.
     * 
     * @param v The vector to multiply.
     * @param m The 3x3 matrix to multiply by.
     * @return A new vector containing the result.
     */
    public static final Vec3 mult(Vec3 v, Mat3 m)
    {
        final Vec3 result = new Vec3();
        mult(v, m, result);
        return result;
    }
    
    /**
     * Multiplies {@code v} by {@code m} and returns the result in a new vector.
     * 
     * @param v The vector to multiply.
     * @param m The 4x4 matrix to multiply by.
     * @return A new vector containing the result.
     */
    public static final Vec3 mult(Vec3 v, Mat4 m)
    {
        final Vec3 result = new Vec3();
        mult(v, m, result);
        return result;
    }
    
    
    /**
     * Calculates the cross product between {@code v0} and {@code v1} and returns
     * the result in a new vector.
     * 
     * @param v0 The vector to multiply.
     * @param v1 The vector to multiply by.
     * @return A new vector containing the result.
     */
    public static final Vec3 cross(Vec3 v0, Vec3 v1)
    {
        final Vec3 result = new Vec3();
        cross(v0, v1, result);
        return result;
    }
    
    /**
     * Divides {@code v} by {@code s} and returns the result in a new vector.
     * 
     * @param v The vector to divide.
     * @param s The scalar to divide by.
     * @return A new vector containing the result.
     */
    public static final Vec3 div(Vec3 v, float s)
    {
        final Vec3 result = new Vec3();
        div(v, s, result);
        return result;
    }
    
    /**
     * Normalizes {@code v} and returns the result in a new vector.
     * 
     * @param v The vector to normalize.
     * @return The normalized vector.
     */
    public static final Vec3 normalize(Vec3 v)
    {
        final Vec3 result = new Vec3();
        normalize(v, result);
        return result;
    }
    
    /**
     * Reflects {@code v} about {@code n} and stores the result in {@code result}.
     * {@code n} must be normalized.
     * 
     * @param v The vector to reflect.
     * @param n The normal vector about which to reflect.
     * @return A new vector containing the result.
     */
    public static final Vec3 reflect(Vec3 v, Vec3 n)
    {
        final Vec3 result = new Vec3();
        reflect(v, n, result);
        return result;
    }
    
    /**
     * Performs a vector projection of {@code v0} onto {@code v1} and stores the
     * result in {@code result}. {@code v1} need not be normalized.
     * 
     * @param v0 The vector to project.
     * @param v1 The vector on which to project.
     * @return A new vector containing the result.
     */
    public static final Vec3 project(Vec3 v0, Vec3 v1)
    {
        final Vec3 result = new Vec3();
        project(v0, v1, result);
        return result;
    }
    
    /**
     * Performs a vector rejection of {@code v1} from {@code v0} and stores the
     * result in {@code result}.
     * 
     * @param v0 The vector to reject from.
     * @param v1 The vector to reject by.
     * @return A new vector containing the result.
     */
    public static final Vec3 reject(Vec3 v0, Vec3 v1)
    {
        final Vec3 result = new Vec3();
        reject(v0, v1, result);
        return result;
    }
    // </editor-fold>
    
    public float x, y, z;
    
    /**
     * Creates a new zero vector.
     */
    public Vec3()
    {
    }
    
    /**
     * Creates a new vector with the given coordinates.
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
    
    // <editor-fold defaultstate="collapsed" desc="Instance accessor methods">
    /**
     * Returns the dot product of this and the given vector.
     * 
     * @param v The vector with which to calculate the dot product.
     * @return The dot product of this and the given vector.
     */
    public float dot(Vec3 v)
    {
        return dot(this, v);
    }
    
    /**
     * Returns the square length of this vector.
     * 
     * @return The square length of this vector.
     */
    public float squareLength()
    {
        return squareLength(this);
    }
    
    /**
     * Returns the length of this vector.
     * 
     * @return the length of this vector.
     */
    public float length()
    {
        return length(this);
    }
    
    /**
     * Returns the square distance between this and the given vector.
     * 
     * @param v The vector to calculate the square distance from.
     * @return The square distance between this and the given vector.
     */
    public float squareDist(Vec3 v)
    {
        return squareDist(this, v);
    }
    
    /**
     * Returns the distance between this and the given vector.
     * 
     * @param v The vector to calculate the distance from.
     * @return The distance between this and the given vector.
     */
    public float dist(Vec3 v)
    {
        return dist(this, v);
    }
    
    /**
     * Returns the scalar projection of this onto the given vector.
     * 
     * @param v The vector on which to project.
     * @return The scalar projection of this onto the given vector.
     */
    public float scalarProject(Vec3 v)
    {
        return scalarProject(this, v);
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Instance mutator methods">
    /**
     * Sets this to the given vector.
     * 
     * @param v The vector to set this to.
     */
    public void set(Vec3 v)
    {
        copy(v, this);
    }
    
    /**
     * Adds the given vector to this.
     * 
     * @param v The vector to add to this.
     */
    public void add(Vec3 v)
    {
        add(this, v, this);
    }
    
    /**
     * Subtracts the given vector from this.
     * 
     * @param v The vector to subtract from this.
     */
    public void sub(Vec3 v)
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
     * Multiplies this by the given 3x3 matrix.
     * 
     * @param m The 3x3 matrix to multiply this by.
     */
    public void mult(Mat3 m)
    {
        mult(this, m, this);
    }
    
    /**
     * Multiplies this by the given 4x4 matrix.
     * 
     * @param m The 4x4 matrix to multiply this by.
     */
    public void mult(Mat4 m)
    {
        mult(this, m, this);
    }
    
    /**
     * Sets this to the cross product between this and the given vector.
     * 
     * @param v The vector to multiply this by.
     */
    public void cross(Vec3 v)
    {
        cross(this, v, this);
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
    
    /**
     * Reflects this about the normalized vector {@code n}.
     * 
     * @param n The normal vector about which to reflect.
     */
    public void reflect(Vec3 n)
    {
        reflect(this, n, this);
    }
    
    /**
     * Projects this onto the given vector.
     * 
     * @param v The vector on which to project.
     */
    public void project(Vec3 v)
    {
        project(this, v, this);
    }
    
    /**
     * Rejects the given vector from this.
     * 
     * @param v The vector to reject from this.
     */
    public void reject(Vec3 v)
    {
        reject(this, v, this);
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Overriden/implemented methods">
    @Override
    public void read(FloatBuffer buffer)
    {
        x = buffer.get();
        y = buffer.get();
        z = buffer.get();
    }

    @Override
    public void write(FloatBuffer buffer)
    {
        buffer.put(x);
        buffer.put(y);
        buffer.put(z);
    }

    @Override
    public void read(DataInputStream in) throws IOException
    {
        x = in.readFloat();
        y = in.readFloat();
        z = in.readFloat();
    }

    @Override
    public void write(DataOutputStream out) throws IOException
    {
        out.writeFloat(x);
        out.writeFloat(y);
        out.writeFloat(z);
    }
    
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
