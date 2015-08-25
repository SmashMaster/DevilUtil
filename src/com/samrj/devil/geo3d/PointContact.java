package com.samrj.devil.geo3d;

import com.samrj.devil.math.Vec3;

/**
 * Contact class for points.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class PointContact extends Contact<Point>
{
    public final Point point;
    
    PointContact(float t, float d, Point p, Vec3 n)
    {
        super(Type.POINT, t, d, p, n);
        this.point = p;
    }

    @Override
    public Point contact()
    {
        return point;
    }
}
