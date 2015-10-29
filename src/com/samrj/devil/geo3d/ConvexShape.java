package com.samrj.devil.geo3d;

import com.samrj.devil.math.Vec3;

/**
 * Convex shape interface.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public interface ConvexShape
{
    /**
     * Intersects this shape against the given point.
     * 
     * @param p The point to clip against.
     * @return The results of the clip test, or null if not intersecting.
     */
    IsectResult isect(Vec3 p);
    
    /**
     * Intersects this shape against the given line segment.
     * 
     * @return The results of the clip test, or null if not intersecting.
     */
    IsectResult isect(Vec3 a, Vec3 b);
    
    /**
     * Intersects this shape against the given triangle.
     * 
     * @return The results of the clip test, or null if not intersecting.
     */
    IsectResult isect(Vec3 a, Vec3 b, Vec3 c);
    
    /**
     * Sweeps this shape in the given direction, against the given point.
     * 
     * @param p The point to sweep against.
     * @param dp The direction in which to sweep.
     * @return The results of the sweep test, or null if missed.
     */
    SweepResult sweep(Vec3 dp, Vec3 p);
    
    /**
     * Sweeps this shape in the given direction, against the given line segment.
     * 
     * @param dp The direction in which to sweep.
     * @return The results of the sweep test, or null if missed.
     */
    SweepResult sweep(Vec3 dp, Vec3 a, Vec3 b);
    
    /**
     * Sweeps this shape in the given direction, against the given triangle.
     * 
     * @param dp The direction in which to sweep.
     * @return The results of the sweep test, or null if missed.
     */
    SweepResult sweep(Vec3 dp, Vec3 a, Vec3 b, Vec3 c);
}
