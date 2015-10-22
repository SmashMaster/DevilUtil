package com.samrj.devil.geo3d;

import com.samrj.devil.math.Vec3;

/**
 * An ellipsoid swept through space.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class SweptEllipsoid
{
    public final Vec3 p0, p1;
    public final Vec3 radius;
    public final boolean terminated;
    
    public SweptEllipsoid(Vec3 p0, Vec3 p1, Vec3 radius, boolean terminated)
    {
        this.p0 = new Vec3(p0); this.p1 = new Vec3(p1);
        this.radius = new Vec3(radius);
        this.terminated = terminated;
    }
}
