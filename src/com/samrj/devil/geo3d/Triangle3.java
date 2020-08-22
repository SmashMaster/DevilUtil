package com.samrj.devil.geo3d;

import com.samrj.devil.math.Vec3;
import com.samrj.devil.math.Vec4;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2020 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Triangle3
{
    // <editor-fold defaultstate="collapsed" desc="Static accessor methods">
    /**
     * Returns the area of the given triangle.
     * 
     * @param t A triangle.
     * @return The area of the given triangle.
     */
    public static float area(Triangle3 t)
    {
        return Vec3.sub(t.b, t.a).cross(Vec3.sub(t.c, t.a)).length()*0.5f;
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Static mutator methods">
    /**
     * Copies the given source triangle into the given target triangle. Does not
     * copy any information other than vertex positions.
     * 
     * @param source The triangle to copy from.
     * @param target The triangle to copy into.
     */
    public static void copy(Triangle3 source, Triangle3 target)
    {
        Vec3.copy(source.a, target.a);
        Vec3.copy(source.b, target.b);
        Vec3.copy(source.c, target.c);
    }
    
    /**
     * Computes the barycentric coordinates of the given point projected onto
     * the given triangle, and stores the result in {@code result};
     * 
     * @param t A triangle.
     * @param p The point whose barycentric coordinates to compute.
     * @param result The vector in which to store the result.
     */
    public static void barycentric(Triangle3 t, Vec3 p, Vec3 result)
    {
        Vec3 v0 = Vec3.sub(t.b, t.a), v1 = Vec3.sub(t.c, t.a), v2 = Vec3.sub(p, t.a);
        float d00 = v0.dot(v0);
        float d01 = v0.dot(v1);
        float d11 = v1.dot(v1);
        float d20 = v2.dot(v0);
        float d21 = v2.dot(v1);
        float denom = d00*d11 - d01*d01;
        
        result.y = (d11*d20 - d01*d21)/denom;
        result.z = (d00*d21 - d01*d20)/denom;
        result.x = 1.0f - result.y - result.z;
    }
    
    /**
     * Interpolates between the vertices of the given triangle with the given
     * barycentric coordinates, and stores the result in {@code result};
     * 
     * @param t A triangle.
     * @param bary The barycentric coordinates to interpolate with.
     * @param result The vector in which to store the result.
     */
    public static void interpolate(Triangle3 t, Vec3 bary, Vec3 result)
    {
        Vec3 out = Vec3.mult(t.a, bary.x); //Temp var in case bary == result
        Vec3.madd(out, t.b, bary.y, out);
        Vec3.madd(out, t.c, bary.z, out);
        Vec3.copy(out, result);
    }
    
    /**
     * Computes the normal vector of the given triangle, and stores it in the
     * given vector.
     * 
     * @param t A triangle.
     * @param result The vector in which to store the result.
     */
    public static void normal(Triangle3 t, Vec3 result)
    {
        Vec3 n = Vec3.sub(t.c, t.a).cross(Vec3.sub(t.b, t.a));
        Vec3.normalize(n, result);
    }
    
    /**
     * Computes the normal vector and plane constant for the given triangle, and
     * stores them in {@code result};
     * 
     * @param t A triangle.
     * @param result The vector in which to store the result.
     */
    public static void plane(Triangle3 t, Vec4 result)
    {
        Vec3 n = normal(t);
        result.x = n.x;
        result.y = n.y;
        result.z = n.z;
        result.w = t.a.dot(n);
    }
    // </editor-fold>
    /**
     * Creates a new triangle with the same current vertex positions as the
     * given triangle. Changes to the new triangle will *not* reflect in the old
     * one, or vice-versa. The new triangle is not guaranteed to be the same
     * class as the given one.
     * 
     * @param t The triangle to copy.
     * @return A new triangle.
     */
    public static Triangle3 copy(Triangle3 t)
    {
        return new Triangle3(new Vec3(t.a),
                             new Vec3(t.b),
                             new Vec3(t.c));
    }
    
    /**
     * Computes the barycentric coordinates of the given point projected onto
     * the given triangle, and returns the result as a new vector.
     * 
     * @param t A triangle.
     * @param p The point whose barycentric coordinates to compute.
     * @return A new vector containing the results.
     */
    public static Vec3 barycentric(Triangle3 t, Vec3 p)
    {
        Vec3 result = new Vec3();
        barycentric(t, p, result);
        return result;
    }
    
    /**
     * Interpolates between the vertices of the given triangle with the given
     * barycentric coordinates, and returns the result as a new vector.
     * 
     * @param t A triangle.
     * @param bary The barycentric coordinates to interpolate with.
     * @return A new vector containing the results.
     */
    public static Vec3 interpolate(Triangle3 t, Vec3 bary)
    {
        Vec3 result = new Vec3();
        interpolate(t, bary, result);
        return result;
    }
    
    /**
     * Computes the normal vector for the given triangle, and returns the result
     * as a new vector.
     * 
     * @param t A triangle.
     * @return A new vector containing the result.
     */
    public static Vec3 normal(Triangle3 t)
    {
        Vec3 result = new Vec3();
        normal(t, result);
        return result;
    }
    
    /**
     * Computes the normal vector and plane constant for the given triangle, and
     * returns the result as a new vector.
     * 
     * @param t A triangle.
     * @return A new vector containing the results.
     */
    public static Vec4 plane(Triangle3 t)
    {
        Vec4 result = new Vec4();
        plane(t, result);
        return result;
    }
    // </editor-fold>
    
    public final Vec3 a, b, c;
    
    public Triangle3(Vec3 a, Vec3 b, Vec3 c)
    {
        this.a = a;
        this.b = b;
        this.c = c;
    }
}
