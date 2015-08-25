package com.samrj.devil.geo3d;

import com.samrj.devil.math.Vec3;
import com.samrj.devil.util.SortedArray;

/**
 * This test checks whether an ellipsoid is clipping with the given geometry,
 * and returns a list of intersections.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class EllipsoidClip
{
    public final Vec3 p;
    public final Vec3 radius;
    
    /** List of intersections in ascending order of depth. **/
    public final SortedArray<Intersection> intersections;
    
    EllipsoidClip(Vec3 p, Vec3 radius)
    {
        this.p = p;
        this.radius = radius;
        intersections = new SortedArray<>(10, Intersection.comparator);
    }
    
    public interface Testable
    {
        public Intersection test(EllipsoidClip clip);
    }
}
