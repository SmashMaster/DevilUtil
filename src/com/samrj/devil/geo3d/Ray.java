package com.samrj.devil.geo3d;

import com.samrj.devil.math.Vec3;

/**
 * The results of a raycast.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2022 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Ray
{
    /**
     * The face that was hit.
     */
    public Triangle3 face;

    /**
     * The interpolation parameter at the time of contact.
     */
    public float time = Float.POSITIVE_INFINITY;
    
    /**
     * The point of contact.
     */
    public final Vec3 point = new Vec3();
    
    /**
     * The surface normal of the static primitive at the point of contact.
     */
    public final Vec3 normal = new Vec3();

    /**
     * Resets this result to prepare for a new raycast.
     */
    public void reset()
    {
        time = Float.POSITIVE_INFINITY;
    }

    /**
     * Copies the given result to this.
     */
    public void set(Ray other)
    {
        face = other.face;
        time = other.time;
        point.set(other.point);
        normal.set(other.normal);
    }

    /**
     * Returns whether the ray hit anything.
     */
    public boolean hit()
    {
        return Float.isFinite(time);
    }

    @Override
    public String toString()
    {
        return "Ray{" + "face=" + face + ", time=" + time + ", point=" + point + ", normal=" + normal + '}';
    }
}
