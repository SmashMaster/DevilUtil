package com.samrj.devil.geo3d;

import com.samrj.devil.math.Vec3;

/**
 * The results of a sweep test.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class SweepResult
{
    /**
     * The interpolation parameter at the time of contact.
     */
    public float time;
    
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
     * The object that was intersected.
     */
    public final GeoPrimitive object;
    
    SweepResult(GeoPrimitive object)
    {
        this.object = object;
    }
}
