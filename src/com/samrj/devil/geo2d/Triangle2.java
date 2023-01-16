package com.samrj.devil.geo2d;

import com.samrj.devil.geo3d.Triangle3;
import com.samrj.devil.math.Vec2;
import com.samrj.devil.math.Vec3;

public interface Triangle2
{
    /**
     * Returns the area of the given triangle.
     *
     * @param t A triangle.
     * @return The area of the given triangle.
     */
    public static float area(Triangle2 t)
    {
        return Math.abs(Vec2.sub(t.b(), t.a()).cross(Vec2.sub(t.c(), t.a()))*0.5f);
    }

    /**
     * Computes the barycentric coordinates of the given point projected onto
     * the given triangle, and stores the result in {@code result};
     *
     * @param t A triangle.
     * @param p The point whose barycentric coordinates to compute.
     * @param result The vector in which to store the result.
     */
    public static void barycentric(Triangle2 t, Vec2 p, Vec3 result)
    {
        Vec2 v0 = Vec2.sub(t.b(), t.a()), v1 = Vec2.sub(t.c(), t.a()), v2 = Vec2.sub(p, t.a());
        float den = v0.x*v1.y - v1.x*v0.y;
        result.y  = (v2.x*v1.y - v1.x*v2.y)/den;
        result.z  = (v0.x*v2.y - v2.x*v0.y)/den;
        result.x  = 1.0f - result.y - result.z;

//        Vec2 v0 = Vec2.sub(t.b(), t.a()), v1 = Vec2.sub(t.c(), t.a()), v2 = Vec2.sub(p, t.a());
//        float d00 = v0.dot(v0);
//        float d01 = v0.dot(v1);
//        float d11 = v1.dot(v1);
//        float d20 = v2.dot(v0);
//        float d21 = v2.dot(v1);
//        float denom = d00*d11 - d01*d01;
//
//        result.y = (d11*d20 - d01*d21)/denom;
//        result.z = (d00*d21 - d01*d20)/denom;
//        result.x = 1.0f - result.y - result.z;
    }

    /**
     * Interpolates between the vertices of the given triangle with the given
     * barycentric coordinates, and stores the result in {@code result};
     *
     * @param t A triangle.
     * @param bary The barycentric coordinates to interpolate with.
     * @param result The vector in which to store the result.
     */
    public static void interpolate(Triangle2 t, Vec3 bary, Vec2 result)
    {
        Vec2 out = Vec2.mult(t.a(), bary.x); //Temp var in case bary == result
        Vec2.madd(out, t.b(), bary.y, out);
        Vec2.madd(out, t.c(), bary.z, out);
        Vec2.copy(out, result);
    }

    /**
     * Computes the barycentric coordinates of the given point projected onto
     * the given triangle, and returns the result as a new vector.
     *
     * @param t A triangle.
     * @param p The point whose barycentric coordinates to compute.
     * @return A new vector containing the results.
     */
    public static Vec3 barycentric(Triangle2 t, Vec2 p)
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
    public static Vec2 interpolate(Triangle2 t, Vec3 bary)
    {
        Vec2 result = new Vec2();
        interpolate(t, bary, result);
        return result;
    }

    Vec2 a();
    Vec2 b();
    Vec2 c();
}
