package com.samrj.devil.geo3d;

import com.samrj.devil.math.Vec3;

/**
 * The results of a raycast.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class RaycastResult
{
    /**
     * The interpolation parameter at the time of contact.
     */
    public float time;
    
    /**
     * The point of contact.
     */
    public final Vec3 point = new Vec3();
    
    /**
     * The surface normal of the static primitive at the point of contact.
     */
    public final Vec3 normal = new Vec3();
    
    /**
     * The face that was hit.
     */
    public final Triangle3 face;
    
    RaycastResult(Triangle3 face)
    {
        this.face = face;
    }
}
