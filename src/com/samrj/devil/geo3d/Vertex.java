package com.samrj.devil.geo3d;

import com.samrj.devil.math.Vec3;

/**
 * 3D point class. Simply extends Vec3.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Vertex extends Vec3 implements EllipsoidCast.Testable
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
        Vec3 p0 = Geometry.div(new Vec3(cast.p0), cast.radius);
        Vec3 p1 = Geometry.div(new Vec3(cast.p1), cast.radius);
        Vec3 cDir = Vec3.sub(p1, p0);
        float cSqLen = cDir.squareLength();
        
        Vec3 a = Geometry.div(new Vec3(this), cast.radius);
        Vec3 dir = Geometry.div(new Vec3(cast.p0), cast.radius).sub(a);

        float t = Geometry.solveQuadratic(cSqLen,
                                          2.0f*cDir.dot(dir),
                                          dir.squareLength() - 1.0f);

        if (Float.isNaN(t)) return null; //We miss the vertex.
        if (t <= 0.0f || (cast.terminated && t >= 1.0f))
            return null; //Moving away, or won't get there in time.
        
        Vec3 cp = Vec3.lerp(cast.p0, cast.p1, t);
        float dist = cast.p0.dist(p1)*t;
        Vec3 n = Vec3.sub(cp, this).normalize();
        return new VertexContact(t, dist, cp, n);
    }
    
    /**
     * Contact class for vertices.
     * 
     * @author Samuel Johnson (SmashMaster)
     * @copyright 2014 Samuel Johnson
     * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
     */
    public final class VertexContact extends Contact<Vertex>
    {
        VertexContact(float t, float d, Vec3 cp, Vec3 n)
        {
            super(Type.POINT, t, d, cp, Vertex.this, n);
        }

        @Override
        public Vertex contact()
        {
            return Vertex.this;
        }
    }
}
