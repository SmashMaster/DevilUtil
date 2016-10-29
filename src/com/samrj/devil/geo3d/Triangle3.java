package com.samrj.devil.geo3d;

import com.samrj.devil.math.Vec3;
import com.samrj.devil.math.Vec4;

/**
 * Interface for triangle-like objects.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2016 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public interface Triangle3
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
        Vec3 a = t.a(), b = t.b(), c = t.c();
        return Vec3.sub(b, a).cross(Vec3.sub(c, a)).length()*0.5f;
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Static mutator methods">
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
        Vec3 a = t.a(), b = t.b(), c = t.c();
        Vec3 v0 = Vec3.sub(b, a), v1 = Vec3.sub(c, a), v2 = Vec3.sub(p, a);
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
        Vec3 a = t.a(), b = t.b(), c = t.c();
        Vec3 out = Vec3.mult(a, bary.x); //Temp var in case bary == result
        Vec3.madd(out, b, bary.y, out);
        Vec3.madd(out, c, bary.z, out);
        Vec3.copy(out, result);
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
        Vec3 a = t.a(), b = t.b(), c = t.c();
        Vec3 n = Vec3.sub(c, a).cross(Vec3.sub(b, a)).normalize();
        result.x = n.x;
        result.y = n.y;
        result.z = n.z;
        result.w = a.dot(n);
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Static factory methods">
    /**
     * Creates a new triangle from the three given vectors. Changes to the
     * vectors will reflect in the created triangle, and vice-versa.
     * 
     * @param a The first vector.
     * @param b The second vector.
     * @param c The third vector.
     * @return A new triangle using the given vectors as vertices.
     */
    public static Triangle3 from(Vec3 a, Vec3 b, Vec3 c)
    {
        return new Triangle3()
        {
            @Override
            public Vec3 a()
            {
                return a;
            }

            @Override
            public Vec3 b()
            {
                return b;
            }

            @Override
            public Vec3 c()
            {
                return c;
            }
        };
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
    
    /**
     * Returns a reference to the position of the first vertex of this triangle.
     * Changes to the triangle are reflected in the vertex, and vice-versa.
     * 
     * @return The first vertex of this triangle.
     */
    public Vec3 a();
    
    /**
     * Returns a reference to the position of the second vertex of this triangle.
     * Changes to the triangle are reflected in the vertex, and vice-versa.
     * 
     * @return The second vertex of this triangle.
     */
    public Vec3 b();
    
    /**
     * Returns a reference to the position of the third vertex of this triangle.
     * Changes to the triangle are reflected in the vertex, and vice-versa.
     * 
     * @return The third vertex of this triangle.
     */
    public Vec3 c();
}
