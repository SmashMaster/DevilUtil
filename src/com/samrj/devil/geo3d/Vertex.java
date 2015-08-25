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
     * @param ellipsoid The ellipsoid cast to test against this vertex.
     * @return A new vertex contact if the given ellipsoid cast hits this vertex,
     *         or null if it doesn't.
     */
    public VertexContact cast(EllipsoidCast ellipsoid)
    {
        return null;
    }
}
