package com.samrj.devil.geo3d;

import com.samrj.devil.math.Vec3;

/**
 * 3D triangle class.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Face
{
    public Vec3 a, b, c;
    public Edge ab, bc, ca;
    
    public Face()
    {
    }
    
    public Face(Vec3 a, Vec3 b, Vec3 c)
    {
        this.a = a; this.b = b; this.c = c;
        ab = new Edge(a, b);
        bc = new Edge(b, c);
        ca = new Edge(c, a);
    }
    
    /**
     * Returns a face contact if the given ray hits this face, or null if it
     * does not.
     * 
     * @param ray The ray to cast against this face.
     * @return A face contact if the given ray hits this face, or null if it
     *         does not.
     */
    public FaceContact ray(RayCast ray)
    {
        Vec3 qp = Vec3.sub(ray.p0, ray.p1);
        Vec3 ab = Vec3.sub(b, a);
        Vec3 ac = Vec3.sub(c, a);

        Vec3 n = Vec3.cross(ab, ac);
        float d = qp.dot(n);
        if (d == 0.0f) return null;
        boolean backface = d < 0.0f;
        if (backface)
        {
            d = -d;
            n.negate();
        }

        float ood = 1.0f/d;
        Vec3 ap = Vec3.sub(ray.p0, a);
        float t = ap.dot(n)*ood;
        if (t < 0.0f) return null; //Pointing away or too far
        if (ray.terminated && t > 1.0f) return null;

        Vec3 e = backface ? Vec3.cross(ap, qp) : Vec3.cross(qp, ap);
        float v = ac.dot(e);
        if (v < 0.0f || v > d) return null; //Missed
        float w = -ab.dot(e);
        if (w < 0.0f || v + w > d) return null; //Missed

        v = v*ood;
        w = w*ood;
        float u = 1.0f - v - w;
        
        float dist = t*qp.length();
        Vec3 p = Vec3.mult(a, u).madd(b, v).madd(c, w);
        n.normalize();
        Vec3 bc = new Vec3(u, v, w);
        
        return new FaceContact(t, dist, p, n, this, bc);
    }
}
