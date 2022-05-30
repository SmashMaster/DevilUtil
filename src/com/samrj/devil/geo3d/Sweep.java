package com.samrj.devil.geo3d;

import com.samrj.devil.math.Vec3;

/**
 * The results of a sweep test.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2022 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Sweep
{
    /**
     * The object that was intersected.
     */
    public Object object;

    /**
     * The interpolation parameter at the time of contact.
     */
    public float time = Float.POSITIVE_INFINITY;
    
    /**
     * The point of contact between the swept volume and the static primitive.
     */
    public final Vec3 point = new Vec3();
    
    /**
     * The surface normal of the static primitive at the point of contact.
     */
    public final Vec3 normal = new Vec3();
    
    /**
     * The position of the swept volume at the time of contact.
     */
    public final Vec3 position = new Vec3();

    /**
     * Resets this result to prepare for a new sweep.
     */
    public void reset()
    {
        time = Float.POSITIVE_INFINITY;
    }

    /**
     * Copies the given result to this.
     */
    public void set(Sweep other)
    {
        object = other.object;
        time = other.time;
        point.set(other.point);
        normal.set(other.normal);
        position.set(other.position);
    }

    /**
     * Returns whether the sweep hit anything.
     */
    public boolean hit()
    {
        return Float.isFinite(time);
    }

    @Override
    public String toString()
    {
        return "Sweep{" + "object=" + object + ", time=" + time + ", point=" + point + ", normal=" + normal + ", position=" + position + '}';
    }
}
