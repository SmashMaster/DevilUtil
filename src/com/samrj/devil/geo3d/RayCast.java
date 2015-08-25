package com.samrj.devil.geo3d;

import com.samrj.devil.math.Vec3;
import com.samrj.devil.util.SortedArray;

/**
 * Geometry ray cast class.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class RayCast
{
    public final Vec3 p0, p1;
    public final boolean terminated;
    public final SortedArray<FaceContact> contacts;
    
    RayCast(Vec3 p0, Vec3 p1, boolean terminated)
    {
        this.p0 = p0; this.p1 = p1;
        this.terminated = terminated;
        contacts = new SortedArray<>(10, Contact.comparator);
    }
}
