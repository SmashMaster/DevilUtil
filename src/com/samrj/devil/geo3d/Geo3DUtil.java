package com.samrj.devil.geo3d;

import com.samrj.devil.math.Util;
import com.samrj.devil.math.Vec3;
import com.samrj.devil.math.Vec4;

/**
 * 3D geometry utility methods.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Geo3DUtil
{
    /**
     * Computes the barycentric coordinates of the given point p projected onto
     * the triangle formed by points a, b, and c, and stores the result in r.
     */
    public static final void baryCoords(Vec3 a, Vec3 b, Vec3 c, Vec3 p, Vec3 r)
    {
        Vec3 v0 = Vec3.sub(b, a), v1 = Vec3.sub(c, a), v2 = Vec3.sub(p, a);
        float d00 = v0.dot(v0);
        float d01 = v0.dot(v1);
        float d11 = v1.dot(v1);
        float d20 = v2.dot(v0);
        float d21 = v2.dot(v1);
        float denom = d00*d11 - d01*d01;
        
        r.y = (d11*d20 - d01*d21)/denom;
        r.z = (d00*d21 - d01*d20)/denom;
        r.x = 1.0f - r.y - r.z;
    }
    
    /**
     * Computes the barycentric coordinates of the given point p projected onto
     * the triangle formed by points a, b, and c, and returns a new vector
     * containing the result.
     * 
     * @return A new vector containing the result.
     */
    public static final Vec3 baryCoords(Vec3 a, Vec3 b, Vec3 c, Vec3 p)
    {
        Vec3 r = new Vec3();
        baryCoords(a, b, c, p, r);
        return r;
    }
    
    /**
     * Creates a point on the given triangle formed by points a, b, c, using
     * the given barycentric coordinates, and stores the result in r.
     */
    public static final void baryPoint(Vec3 a, Vec3 b, Vec3 c, Vec3 bary, Vec3 r)
    {
        Vec3.mult(a, bary.x, r);
        r.madd(b, bary.y);
        r.madd(c, bary.z);
    }
    
    /**
     * Creates a point on the given triangle formed by points a, b, c, using
     * the given barycentric coordinates, and returns it as a new vector.
     */
    public static final Vec3 baryPoint(Vec3 a, Vec3 b, Vec3 c, Vec3 bary)
    {
        Vec3 r = new Vec3();
        baryPoint(a, b, c, bary, r);
        return r;
    }
    
    /**
     * Returns whether the given barycentric coordinates lie on a triangle.
     */
    public static final boolean baryContained(Vec3 bary)
    {
        return bary.y >= 0.0f && bary.z >= 0.0f && (bary.y + bary.z) <= 1.0f;
    }
    
    /**
     * Computes the normal vector and plane constant for the plane formed by
     * points a, b, and c, and stores the result in r.
     */
    public static final void plane(Vec3 a, Vec3 b, Vec3 c, Vec4 r)
    {
        Vec3 n = Vec3.sub(c, a).cross(Vec3.sub(b, a)).normalize();
        r.x = n.x;
        r.y = n.y;
        r.z = n.z;
        r.w = a.dot(n);
    }
    
    /**
     * Computes the normal vector and plane constant for the plane formed by
     * points a, b, and c, and returns a new vector containing the result.
     */
    public static final Vec4 plane(Vec3 a, Vec3 b, Vec3 c)
    {
        Vec4 r = new Vec4();
        plane(a, b, c, r);
        return r;
    }
    
    /**
     * Returns the distance from the given point to the given plane.
     */
    public static final float dist(Vec3 p, Vec4 plane)
    {
        return plane.x*p.x + plane.y*p.y + plane.z*p.z - plane.w;
    }
    
    /**
     * Finds the closest point to p on the given plane, and stores it in r.
     */
    public static final void closest(Vec3 p, Vec4 plane, Vec3 r)
    {
        Vec3 n = normal(plane);
        Vec3.madd(p, n, plane.w - p.dot(n), r);
    }
    
    /**
     * Returns the closest point to p on the given plane as a new vector.
     */
    public static final Vec3 closest(Vec3 p, Vec4 plane)
    {
        Vec3 r = new Vec3();
        closest(p, plane, r);
        return r;
    }
    
    /**
     * Stores the normal vector for the given plane in r.
     */
    public static final void normal(Vec4 plane, Vec3 r)
    {
        r.x = plane.x;
        r.y = plane.y;
        r.z = plane.z;
    }
    
    /**
     * Returns the normal of the given plane as a new vector.
     */
    public static final Vec3 normal(Vec4 plane)
    {
        Vec3 r = new Vec3();
        normal(plane, r);
        return r;
    }
    
    static final float solveQuadratic(float a, float b, float c)
    {
        float[] solutions = Util.quadFormula(a, b, c);
        
        switch (solutions.length)
        {
            case 0: return Float.NaN;
            case 1: return solutions[0];
            case 2:
                float s1 = solutions[0];
                float s2 = solutions[1];
                
                if (s1 < 0.0f || s2 < 0.0f)
                     return s1 > s2 ? s1 : s2; //If either are negative, return the larger one.
                else return s1 < s2 ? s1 : s2; //Otherwise, return the smaller one.
            default:
                assert(false);
                throw new Error();
        }
    }
    
    static float sweepSpherePlane(Vec3 p, Vec3 dp, Vec4 plane, float r)
    {
        Vec3 n = normal(plane);
        float dist = dist(p, plane);
        if (dist < 0.0f)
        {
            dist = -dist;
            n.negate();
        }
        if (dist < r) return 0.0f;
        return (r - dist)/n.dot(dp);
    }
    
    /**
     * Casts the given ray against the given triangle and returns the results of
     * the cast, or null if the ray missed.
     */
    public static RaycastResult raycast(Vec3 p0, Vec3 dp, Vec3 a, Vec3 b, Vec3 c)
    {
        Vec3 ab = Vec3.sub(b, a);
        Vec3 ac = Vec3.sub(c, a);

        Vec3 n = Vec3.cross(ab, ac);
        float d = dp.dot(n);
        if (d == 0.0f) return null; //Ray parallel to triangle.
        boolean backface = d < 0.0f;
        if (backface)
        {
            d = -d;
            n.negate();
        }
        
        float ood = 1.0f/d;
        Vec3 ap = Vec3.sub(p0, a);
        float t = ap.dot(n)*ood;
        if (t < 0.0f || t > 1.0f) return null; //Behind or too far.
        
        Vec3 e = backface ? Vec3.cross(ap, dp) : Vec3.cross(dp, ap);
        float v = ac.dot(e);
        if (v < 0.0f || v > d) return null; //Missed triangle.
        float w = -ab.dot(e);
        if (w < 0.0f || v + w > d) return null; //Missed triangle.

        v = v*ood;
        w = w*ood;
        float u = 1.0f - v - w;
        
        RaycastResult out = new RaycastResult();
        out.time = t;
        Vec3.mult(a, u, out.point);
        out.point.madd(b, v).madd(c, w);
        Vec3.normalize(n, out.normal);
        return out;
    }
    
    /**
     * Reduces the degrees of freedom of the given vector, using the given array
     * of normal vectors.
     */
    public static final Vec3 restrain(Vec3 v, Vec3... normals)
    {
        Vec3[] opp = new Vec3[3];
        int num = 0;
        
        for (Vec3 n : normals) if (n.dot(v) < 0.0f)
        {
            opp[num++] = n;
            if (num == 3) break;
        }
        
        switch (num)
        {
            case 0: return v;
            case 1: return v.reject(opp[0]);
            case 2: return v.project(Vec3.cross(opp[0], opp[1]));
            default: return v.set();
        }
    }
    
    private Geo3DUtil()
    {
    }
}
