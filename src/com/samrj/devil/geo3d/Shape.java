package com.samrj.devil.geo3d;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public interface Shape
{
    public enum Type
    {
        VERTEX, EDGE, FACE;
    }
    
    public Contact collide(SweptEllipsoid swEll);
    public Intersection collide(Ellipsoid ell);
    public Contact collide(Ray ray);
    public Intersection collide(Cylinder cyl);
}
