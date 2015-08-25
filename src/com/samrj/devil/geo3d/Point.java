package com.samrj.devil.geo3d;

import com.samrj.devil.math.Vec3;

/**
 * 3D point class. Simply extends Vec3.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Point extends Vec3
{
    /**
     * Creates a new zero point.
     */
    public Point()
    {
    }
    
    /**
     * Creates a new point with the given coordinates.
     */
    public Point(float x, float y, float z)
    {
        this.x = x; this.y = y; this.z = z;
    }
    
    /**
     * Creates a point at the given vector.
     * 
     * @param v The vector to copy.
     */
    public Point(Vec3 v)
    {
        x = v.x; y = v.y; z = v.z;
    }
}
