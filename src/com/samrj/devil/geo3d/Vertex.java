package com.samrj.devil.geo3d;

import com.samrj.devil.math.Vec3;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Vertex extends Vec3 implements Shape
{
    /**
     * Creates a new zero point.
     */
    public Vertex()
    {
    }
    
    /**
     * Creates a new point with the given coordinates.
     */
    public Vertex(float x, float y, float z)
    {
        this.x = x; this.y = y; this.z = z;
    }
    
    /**
     * Creates a point at the given vector.
     * 
     * @param v The vector to copy.
     */
    public Vertex(Vec3 v)
    {
        x = v.x; y = v.y; z = v.z;
    }
    
    @Override
    public VertexContact collide(SweptEllipsoid swEll)
    {
        Vec3 p0 = Vec3.div(swEll.p0, swEll.radius);
        Vec3 p1 = Vec3.div(swEll.p1, swEll.radius);
        Vec3 cDir = Vec3.sub(p1, p0);
        float cSqLen = cDir.squareLength();
        
        Vec3 a = Vec3.div(this, swEll.radius);
        Vec3 dir = Vec3.div(swEll.p0, swEll.radius).sub(a);

        float t = Geometry.solveQuadratic(cSqLen,
                                          2.0f*cDir.dot(dir),
                                          dir.squareLength() - 1.0f);

        if (Float.isNaN(t)) return null; //We miss the vertex.
        if (t != t || t<= 0.0f || (swEll.terminated && t >= 1.0f))
            return null; //Moving away, won't get there in time, or NaN.
        
        Vec3 cp = Vec3.lerp(swEll.p0, swEll.p1, t);
        float dist = swEll.p0.dist(p1)*t;
        Vec3 n = Vec3.sub(cp, this).normalize();
        
        return new VertexContact(t, dist, cp, n);
    }

    @Override
    public VertexIntersection collide(Ellipsoid ell)
    {
        Vec3 dir = Vec3.sub(ell.pos, this).div(ell.radius);
        float sqDist = dir.squareLength();
        if (sqDist != sqDist || sqDist > 1.0f) return null; //Too far apart or NaN.
        
        Vec3 n = Vec3.div(dir, (float)Math.sqrt(sqDist)).mult(ell.radius);
        float nLen = n.length();
        float depth = nLen - Vec3.dist(ell.pos, this);
        n.div(nLen);
        
        return new VertexIntersection(depth, n);
    }

    @Override
    public VertexContact collide(SweptPoint ray)
    {
        return null;
    }

    /**
     * Contact class for vertices.
     */
    public final class VertexContact extends Contact
    {
        private VertexContact(float t, float d, Vec3 cp, Vec3 n)
        {
            super(t, d, cp, Vertex.this, n);
        }
        
        @Override
        public Vertex shape()
        {
            return Vertex.this;
        }

        @Override
        public Type type()
        {
            return Type.VERTEX;
        }
    }
    
    /**
     * Intersection class for vertices.
     */
    public final class VertexIntersection extends Intersection
    {
        VertexIntersection(float d, Vec3 n)
        {
            super(d, Vertex.this,n);
        }
        
        @Override
        public Vertex shape()
        {
            return Vertex.this;
        }
        
        @Override
        public Type type()
        {
            return Type.VERTEX;
        }
    }
}
