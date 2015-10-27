package com.samrj.devil.geo3d;

import com.samrj.devil.math.Vec3;

/**
 * Cylinder class.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Cylinder
{
    public final Vec3 pos;
    public final float radius, halfHeight;
    public final float rsq;
    
    public Cylinder(Vec3 pos, float radius, float halfHeight)
    {
        this.pos = new Vec3(pos);
        this.radius = radius;
        this.halfHeight = halfHeight;
        rsq = radius*radius;
    }
}
