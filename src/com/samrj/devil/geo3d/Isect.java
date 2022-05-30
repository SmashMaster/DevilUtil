package com.samrj.devil.geo3d;

import com.samrj.devil.math.Vec3;

/**
 * The results of an intersection test.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2022 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Isect
{
    /**
     * The object that was intersected.
     */
    public Object object;

    /**
     * The deepest point on the clipped primitive within the clipping volume.
     */
    public final Vec3 point = new Vec3();
    
    /**
     * The closest point on the surface of the clipping volume to the deepest
     * point.
     */
    public final Vec3 surface = new Vec3();
    
    /**
     * The distance between the deepest point and the surface point.
     */
    public float depth = Float.NEGATIVE_INFINITY;
    
    /**
     * The surface normal of the clipping volume at the surface point.
     */
    public final Vec3 normal = new Vec3();

    /**
     * Resets this result to prepare for a new intersection.
     */
    public void reset()
    {
        depth = Float.NEGATIVE_INFINITY;
    }

    /**
     * Copies the given result to this.
     */
    public void set(Isect other)
    {
        object = other.object;
        point.set(other.point);
        surface.set(other.surface);
        depth = other.depth;
        normal.set(other.normal);
    }

    /**
     * Returns whether the intersection touched anything.
     */
    public boolean hit()
    {
        return Float.isFinite(depth);
    }

    @Override
    public String toString()
    {
        return "Isect{" + "object=" + object + ", point=" + point + ", surface=" + surface + ", depth=" + depth + ", normal=" + normal + '}';
    }
}
