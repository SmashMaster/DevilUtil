package com.samrj.devil.geo3d;

import com.samrj.devil.math.*;
import java.util.ArrayDeque;
import java.util.Collection;

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
     * Returns whether the given barycentric coordinates lie on a triangle.
     */
    public static final boolean baryContained(Vec3 bary)
    {
        return bary.y >= 0.0f && bary.z >= 0.0f && (bary.y + bary.z) <= 1.0f;
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
    public static RaycastResult raycast(Triangle3 f, Vec3 p0, Vec3 dp, boolean terminated)
    {
        Vec3 ab = Vec3.sub(f.b, f.a);
        Vec3 ac = Vec3.sub(f.c, f.a);
        
        Vec3 n = Vec3.cross(ab, ac);
        float d = -dp.dot(n);
        if (d == 0.0f) return null; //Ray parallel to triangle.
        boolean backface = d < 0.0f;
        if (backface)
        {
            d = -d;
            n.negate();
        }
        
        float ood = 1.0f/d;
        Vec3 ap = Vec3.sub(p0, f.a);
        float t = ap.dot(n)*ood;
        if (t < 0.0f) return null; //Ray behind triangle.
        if (terminated && t > 1.0f) return null; //Triangle too far.
        
        Vec3 e = backface ? Vec3.cross(dp, ap) : Vec3.cross(ap, dp);
        float v = ac.dot(e);
        if (v < 0.0f || v > d) return null; //Missed triangle.
        float w = -ab.dot(e);
        if (w < 0.0f || v + w > d) return null; //Missed triangle.

        v = v*ood;
        w = w*ood;
        float u = 1.0f - v - w;
        
        RaycastResult out = new RaycastResult(f);
        out.time = t;
        Vec3.mult(f.a, u, out.point);
        out.point.madd(f.b, v).madd(f.c, w);
        Vec3.normalize(n, out.normal);
        return out;
    }
    
    /**
     * Reduces the degrees of freedom of the given vector, using the given array
     * of normal vectors. Returns the result as a new vector.
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
            case 0: return new Vec3(v);
            case 1: return Vec3.reject(v, opp[0]);
            case 2: return Vec3.project(v, Vec3.cross(opp[0], opp[1]));
            default: return new Vec3();
        }
    }
    
    /**
     * Finds the two closest points between two lines. The result is returned as
     * an array containing the two interpolation parameters to get the closest
     * point along both lines.
     */
    public static final float[] closestTwoLines(Vec3 ap0, Vec3 ap1, Vec3 bp0, Vec3 bp1)
    {
        Vec3 u = Vec3.sub(ap1, ap0);
        Vec3 v = Vec3.sub(bp1, bp0);
        Vec3 w = Vec3.sub(ap0, bp0);
        
        float uu = u.dot(u);
        float uv = u.dot(v);
        float vv = v.dot(v);
        float uw = u.dot(w);
        float vw = v.dot(w);
        
        float denom = uu*vv - uv*uv;
        float[] result = new float[2];
        
        if (denom < 0.001f)
        {
            result[0] = 0.0f;
            result[1] = (uv>vv ? uw/uv : vw/vv);
        }
        else
        {
            result[0] = (uv*vw - vv*uw)/denom;
            result[1] = (uu*vw - uv*uw)/denom;
        }
        
        return result;
    }
    
    /**
     * Creates a 3D rotation matrix from the given normal vector, which is
     * assumed to be normalized. The rows of the resulting matrix will form an
     * orthonormal basis: they will all be perpendicular to one another, and
     * each have a length of one. The third row will equal the given normal.
     */
    public static void orthonormalBasis(Vec3 n, Mat3 result)
    {
        float length = n.length();

        if (length != 0.0f)
        {
            Vec3 b = new Vec3(0.0f, n.z, -n.y);
            if (b.isZero(0.01f)) b.set(-n.z, 0.0f, n.x);
            b.normalize();
            
            Vec3 t = Vec3.cross(n, b).normalize();
            result.set(b.x, b.y, b.z,
                       t.x, t.y, t.z,
                       n.x, n.y, n.z);
        }
        else result.setIdentity();
    }
    
    public static Mat3 orthonormalBasis(Vec3 n)
    {
        Mat3 result = new Mat3();
        orthonormalBasis(n, result);
        return result;
    }
    
    /**
     * Returns a bounding sphere that contains all of the given vectors. It is
     * not necessarily a minimum bounding sphere. The last component of the
     * returned vector is the sphere's radius.
     */
    public static Vec4 boundingSphere(Collection<Vec3> vecs)
    {
        //Find bounding sphere by Ritter's algorithm.
        ArrayDeque<Vec3> remaining = new ArrayDeque<>(vecs.size());
        remaining.addAll(vecs);
        
        //Start with any point.
        Vec3 start = remaining.peek();
        if (vecs.size() == 1) return new Vec4(start, 0.0f);
        
        //Find the furthest point A from the starting point.
        Vec3 furthest = null;
        float furthestDist = Float.NEGATIVE_INFINITY;
        for (Vec3 vec : remaining)
        {
            float dist = vec.squareDist(start);
            if (dist > furthestDist)
            {
                furthest = vec;
                furthestDist = dist;
            }
        }
        Vec3 a = furthest;
        
        //Find the furthest point B from A.
        furthestDist = Float.NEGATIVE_INFINITY;
        for (Vec3 vec : remaining)
        {
            float dist = vec.squareDist(a);
            if (dist > furthestDist)
            {
                furthest = vec;
                furthestDist = dist;
            }
        }
        Vec3 b = furthest;
        
        //Our initial sphere contains A and B.
        Vec3 center = Vec3.add(a, b).mult(0.5f);
        float sqRadius = a.squareDist(b)*0.25f;
        
        //Make sure every point is contained by the sphere, expanding it if not.
        while (!remaining.isEmpty())
        {
            Vec3 vec = remaining.removeLast();
            Vec3 dir = Vec3.sub(vec, center);
            float sqDist = dir.squareLength();
            if (sqDist > sqRadius)
            {
                float radius = (float)Math.sqrt(sqRadius);
                float dist = (float)Math.sqrt(sqDist);
                center.madd(dir, ((dist - radius)*0.5f)/dist);
                
                float newRadius = (radius + dist)*0.5f;
                sqRadius = newRadius*newRadius;
            }
        }
        
        return new Vec4(center, (float)Math.sqrt(sqRadius));
    }
    
    private Geo3DUtil()
    {
    }
}
