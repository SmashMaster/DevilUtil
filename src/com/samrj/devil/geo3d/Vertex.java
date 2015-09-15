package com.samrj.devil.geo3d;

import com.samrj.devil.math.Vec3;

/**
 * 3D point class. Simply extends Vec3.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Vertex extends Vec3 implements EllipsoidCast.Testable, EllipsoidClip.Testable
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
    
    /**
     * Returns a new vertex contact if the given ellipsoid cast hits this
     * vertex, or null if it doesn't.
     * 
     * @param cast The ellipsoid cast to test against this vertex.
     * @return A new vertex contact if the given ellipsoid cast hits this vertex,
     *         or null if it doesn't.
     */
    @Override
    public VertexContact test(EllipsoidCast cast)
    {
        Vec3 p0 = Vec3.div(cast.p0, cast.radius);
        Vec3 p1 = Vec3.div(cast.p1, cast.radius);
        Vec3 cDir = Vec3.sub(p1, p0);
        float cSqLen = cDir.squareLength();
        
        Vec3 a = Vec3.div(this, cast.radius);
        Vec3 dir = Vec3.div(cast.p0, cast.radius).sub(a);

        float t = Geometry.solveQuadratic(cSqLen,
                                          2.0f*cDir.dot(dir),
                                          dir.squareLength() - 1.0f);

        if (Float.isNaN(t)) return null; //We miss the vertex.
        if (t != t || t<= 0.0f || (cast.terminated && t >= 1.0f))
            return null; //Moving away, won't get there in time, or NaN.
        
        Vec3 cp = Vec3.lerp(cast.p0, cast.p1, t);
        float dist = cast.p0.dist(p1)*t;
        Vec3 n = Vec3.sub(cp, this).normalize();
        return new VertexContact(t, dist, cp, n);
    }

    @Override
    public VertexIntersection test(EllipsoidClip clip)
    {
        Vec3 dir = Vec3.sub(clip.p, this).div(clip.radius);
        float sqDist = dir.squareLength();
        if (sqDist != sqDist || sqDist > 1.0f) return null; //Too far apart or NaN.
        
        Vec3 n = Vec3.div(dir, (float)Math.sqrt(sqDist)).mult(clip.radius);
        float nLen = n.length();
        float depth = nLen - Vec3.dist(clip.p, this);
        n.div(nLen);
        
        return new VertexIntersection(depth, n);
    }
    
    /**
     * Contact class for vertices.
     */
    public final class VertexContact extends Contact<Vertex>
    {
        VertexContact(float t, float d, Vec3 cp, Vec3 n)
        {
            super(t, d, cp, Vertex.this, n);
        }
        
        @Override
        public Type type()
        {
            return Type.VERTEX;
        }

        @Override
        public Vertex contacted()
        {
            return Vertex.this;
        }
    }
    
    /**
     * Intersection class for vertices.
     */
    public final class VertexIntersection extends Intersection<Vertex>
    {
        VertexIntersection(float d, Vec3 n)
        {
            super(d, Vertex.this,n);
        }
        
        @Override
        public Type type()
        {
            return Type.VERTEX;
        }
        
        @Override
        public Vertex intersected()
        {
            return Vertex.this;
        }
    }
}
