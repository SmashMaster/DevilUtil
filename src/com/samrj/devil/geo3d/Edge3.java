package com.samrj.devil.geo3d;

import com.samrj.devil.math.Vec3;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2020 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Edge3
{
    public final Vec3 a, b;
    
    public Edge3(Vec3 a, Vec3 b)
    {
        this.a = a;
        this.b = b;
    }
}
