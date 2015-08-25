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
    private static float sweepSpherePlane(Vec3 p0, Vec3 v, Vec3 n, Vec3 a, float r)
    {
        float dist = n.dot(Vec3.sub(p0, a));
        if (dist < 0.0f)
        {
            dist = -dist;
            n.negate();
        }
        
        if (dist <= r) return 0.0f;
        return (r - dist)/n.dot(v);
    }
    
    private static float triArea2D(float x1, float y1, float x2, float y2, float x3, float y3)
    {
        return (x1 - x2)*(y2 - y3) - (x2 - x3)*(y1 - y2);
    }
    
    private static Vec3 barycentric(Vec3 a, Vec3 b, Vec3 c, Vec3 p)
    {
        // Unnormalized triangle normal
        Vec3 m = Vec3.sub(b, a).cross(Vec3.sub(c, a));
        // Nominators and one-over-denominator for u and v ratios
        float nu, nv, ood;
        // Absolute components for determining projection plane
        float x = Math.abs(m.x), y = Math.abs(m.y), z = Math.abs(m.z);
        // Compute areas in plane of largest projection
        if (x >= y && x >= z)
        {
            // x is largest, project to the yz plane
            nu = triArea2D(p.y, p.z, b.y, b.z, c.y, c.z); // Area of PBC in yz plane
            nv = triArea2D(p.y, p.z, c.y, c.z, a.y, a.z); // Area of PCA in yz plane
            ood = 1.0f/m.x; // 1/(2*area of ABC in yz plane)
        }
        else if (y >= x && y >= z)
        {
            // y is largest, project to the xz plane
            nu = triArea2D(p.x, p.z, b.x, b.z, c.x, c.z);
            nv = triArea2D(p.x, p.z, c.x, c.z, a.x, a.z);
            ood = 1.0f/-m.y;
        }
        else
        {
            // z is largest, project to the xy plane
            nu = triArea2D(p.x, p.y, b.x, b.y, c.x, c.y);
            nv = triArea2D(p.x, p.y, c.x, c.y, a.x, a.y);
            ood = 1.0f/m.z;
        }
        
        Vec3 coords = new Vec3();
        coords.x = nu*ood;
        coords.y = nv*ood;
        coords.z = 1.0f - coords.x - coords.y;
        return coords;
    }
    
    public Vertex a, b, c;
    public Edge ab, bc, ca;
    
    public Face()
    {
    }
    
    public Face(Vertex a, Vertex b, Vertex c)
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
    public FaceContact cast(RayCast ray)
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
    
    /**
     * Returns a new face contact if the given ellipsoid cast hits this face,
     * or null if it doesn't.
     * 
     * @param cast The ellipsoid cast to test against this face.
     * @return A new face contact if the given ellipsoid cast hits this face,
     *         or null if it doesn't.
     */
    public FaceContact cast(EllipsoidCast cast)
    {
        Vec3 p0 = Geometry.div(new Vec3(cast.p0), cast.radius);
        Vec3 p1 = Geometry.div(new Vec3(cast.p1), cast.radius);
        Vec3 cDir = Vec3.sub(p1, p0);
        
        Vec3 ae = Geometry.div(new Vec3(a), cast.radius);
        Vec3 be = Geometry.div(new Vec3(b), cast.radius);
        Vec3 ce = Geometry.div(new Vec3(c), cast.radius);
        
        Vec3 normal = Vec3.sub(ce, ae).cross(Vec3.sub(be, ae)).normalize(); //Plane normal
        
        float t = sweepSpherePlane(p0, cDir, normal, ae, 1.0f); //Time of contact

        if (t <= 0.0f || (cast.terminated && t >= 1.0f))
            return null; //Moving away, or won't get there in time.
        
        Vec3 cp = Vec3.lerp(cast.p0, cast.p1, t);
        Vec3 bc = barycentric(a, b, c, cp);
        if (!(bc.y >= 0.0f && bc.z >= 0.0f && (bc.y + bc.z) <= 1.0f)) return null; //We will miss the face
        
        float dist = cast.p0.dist(cast.p1)*t;
        Vec3 p = Vec3.mult(a, bc.x).madd(b, bc.y).madd(c, bc.z);
        Vec3 n = Vec3.sub(cp, p).normalize();
        return new FaceContact(t, dist, p, n, this, bc);
    }
}
