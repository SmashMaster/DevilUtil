package com.samrj.devil.geo3d;

import com.samrj.devil.math.Vec3;

/**
 * The results of an intersection test.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2020 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class IsectResult
{
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
    public float depth;
    
    /**
     * The surface normal of the clipping volume at the surface point.
     */
    public final Vec3 normal = new Vec3();
    
    /**
     * The object that was intersected.
     */
    public final Object object;
    
    IsectResult(Object object)
    {
        this.object = object;
    }
}
