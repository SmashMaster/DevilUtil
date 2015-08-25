package com.samrj.devil.geo3d;

import com.samrj.devil.math.Vec3;

/**
 * 3D point class. Simply extends Vec3.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Vertex extends Vec3
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
    public VertexContact cast(EllipsoidCast cast)
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
        
        float dist = cast.p0.dist(p1)*t;
        Vec3 n = Vec3.lerp(p0, p1, t).sub(a).normalize();
        return new VertexContact(t, dist, this, n);
    }
}
