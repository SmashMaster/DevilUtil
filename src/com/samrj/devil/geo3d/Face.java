package com.samrj.devil.geo3d;

import com.samrj.devil.math.Vec3;

/**
 * 3D triangle class.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Face implements EllipsoidCast.Testable, RayCast.Testable, EllipsoidClip.Testable
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
    
    private static Vec3 barycentric(Vec3 a, Vec3 b, Vec3 c, Vec3 p)
    {
        Vec3 v0 = Vec3.sub(b, a), v1 = Vec3.sub(c, a), v2 = Vec3.sub(p, a);
        float d00 = v0.dot(v0);
        float d01 = v0.dot(v1);
        float d11 = v1.dot(v1);
        float d20 = v2.dot(v0);
        float d21 = v2.dot(v1);
        float denom = d00*d11 - d01*d01;
        
        Vec3 coords = new Vec3();
        coords.y = (d11*d20 - d01*d21)/denom;
        coords.z = (d00*d21 - d01*d20)/denom;
        coords.x = 1.0f - coords.y - coords.z;
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
    @Override
    public FaceContact test(RayCast ray)
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
        return new FaceContact(t, dist, p, p, n, bc);
    }
    
    /**
     * Returns a new face contact if the given ellipsoid cast hits this face,
     * or null if it doesn't.
     * 
     * @param cast The ellipsoid cast to test against this face.
     * @return A new face contact if the given ellipsoid cast hits this face,
     *         or null if it doesn't.
     */
    @Override
    public FaceContact test(EllipsoidCast cast)
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
        return new FaceContact(t, dist, cp, p, n, bc);
    }
    
    @Override
    public FaceIntersection test(EllipsoidClip clip)
    {
        return null;
    }
    
    /**
     * Contact class for faces.
     */
    public class FaceContact extends Contact<Face>
    {
        /**
         * The contact barycentric coordinates.
         */
        public final Vec3 fbc;

        FaceContact(float t, float d, Vec3 cp, Vec3 p, Vec3 n, Vec3 fbc)
        {
            super(t, d, cp, p, n);
            this.fbc = fbc;
        }
        
        @Override
        public Type type()
        {
            return Type.FACE;
        }

        @Override
        public Face contacted()
        {
            return Face.this;
        }
    }
    
    public final class FaceIntersection extends Intersection<Face>
    {
        /**
         * The face barycentric coordinates.
         */
        public final Vec3 fbc;
        
        FaceIntersection(float d, Vec3 p, Vec3 n, Vec3 fbc)
        {
            super(d, p, n);
            this.fbc = fbc;
        }
        
        @Override
        public Type type()
        {
            return Type.FACE;
        }
        
        @Override
        public Face intersected()
        {
            return Face.this;
        }
    }
}
