package com.samrj.devil.geo3d;

import com.samrj.devil.math.Vec3;

/**
 * Ellipsoid class.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Ellipsoid
{
    public final Vec3 pos;
    public final Vec3 radius;
    
    public Ellipsoid(Vec3 p, Vec3 radius)
    {
        this.pos = p;
        this.radius = radius;
    }
}
