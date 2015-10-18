package com.samrj.devil.geo3d;

import com.samrj.devil.math.Vec3;

/**
 * Class for surface collisions between meshes and swept geometry.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public abstract class Contact
{
    /**
     * The contact interpolant.
     */
    public final float t;
    
    /**
     * The contact distance from the start.
     */
    public final float d;
    
    /**
     * The position of the cast at the time of the contact.
     */
    public final Vec3 cp;
    
    /**
     * The surface position of the contact.
     */
    public final Vec3 p;
    
    /**
     * The normal of the contact.
     */
    public final Vec3 n;
    
    Contact(float t, float d, Vec3 cp, Vec3 p, Vec3 n)
    {
        this.t = t;
        this.d = d;
        this.cp = cp;
        this.p = p;
        this.n = n;
    }
    
    /**
     * @return The shape that is being contacted.
     */
    public abstract Shape shape();
    
    /**
     * @return The type of object that this contact handles.
     */
    public abstract Shape.Type type();
}
