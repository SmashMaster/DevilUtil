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
     * @param v The vertex to clip against.
     * @return The results of the clip test, or null if not intersecting.
     */
    IsectResult isect(Vertex3 v);
    
    /**
     * Intersects this shape against the given line segment.
     * 
     * @param e The edge to clip against.
     * @return The results of the clip test, or null if not intersecting.
     */
    IsectResult isect(Edge3 e);
    
    /**
     * Intersects this shape against the given triangle.
     * 
     * @param f The face to clip against.
     * @return The results of the clip test, or null if not intersecting.
     */
    IsectResult isect(Triangle3 f);
    
    /**
     * Sweeps this shape in the given direction, against the given point.
     * 
     * @param dp The direction in which to sweep.
     * @param v The vertex to sweep against.
     * @return The results of the sweep test, or null if missed.
     */
    SweepResult sweep(Vec3 dp, Vertex3 v);
    
    /**
     * Sweeps this shape in the given direction, against the given line segment.
     * 
     * @param dp The direction in which to sweep.
     * @param e The edge to sweep against.
     * @return The results of the sweep test, or null if missed.
     */
    SweepResult sweep(Vec3 dp, Edge3 e);
    
    /**
     * Sweeps this shape in the given direction, against the given triangle.
     * 
     * @param dp The direction in which to sweep.
     * @param f The face to sweep against.
     * @return The results of the sweep test, or null if missed.
     */
    SweepResult sweep(Vec3 dp, Triangle3 f);
    
    /**
     * Returns a bounding box that contains this convex shape. Should be as
     * small as possible.
     */
    Box3 getBounds();
}
