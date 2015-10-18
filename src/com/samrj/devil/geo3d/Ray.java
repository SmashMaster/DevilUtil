package com.samrj.devil.geo3d;

import com.samrj.devil.math.Vec3;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Ray
{
    public final Vec3 p0, p1;
    public final boolean terminated;
    
    public Ray(Vec3 p0, Vec3 p1, boolean terminated)
    {
        this.p0 = p0; this.p1 = p1;
        this.terminated = terminated;
    }
}
