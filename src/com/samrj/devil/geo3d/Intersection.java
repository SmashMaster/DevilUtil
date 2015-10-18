package com.samrj.devil.geo3d;

import com.samrj.devil.math.Vec3;

/**
 * Class for collisions between meshes and static geometry.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public abstract class Intersection
{
    /**
     * The depth of intersection.
     */
    public final float d;
    
    /**
     * The position on the intersected geometry which is deepest inside of the
     * intersecting volume.
     */
    public final Vec3 p;
    
    /**
     * The normal vector of the intersection.
     */
    public final Vec3 n;
    
    Intersection(float d, Vec3 p, Vec3 n)
    {
        this.d = d;
        this.p = p;
        this.n = n;
    }
    
    /**
     * @return The shape that is being intersected.
     */
    public abstract Shape shape();
    
    /**
     * @return The type of object that this contact handles.
     */
    public abstract Shape.Type type();
}
