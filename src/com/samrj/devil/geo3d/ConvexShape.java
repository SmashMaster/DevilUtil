package com.samrj.devil.geo3d;

import com.samrj.devil.geo3d.GeoMesh.Edge;
import com.samrj.devil.geo3d.GeoMesh.Face;
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
     * @param e The edge to clip against.
     * @return The results of the clip test, or null if not intersecting.
     */
    IsectResult isect(Edge e);
    
    /**
     * Intersects this shape against the given triangle.
     * 
     * @param f The face to clip against.
     * @return The results of the clip test, or null if not intersecting.
     */
    IsectResult isect(Face f);
    
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
     * @param e The edge to sweep against.
     * @return The results of the sweep test, or null if missed.
     */
    SweepResult sweep(Vec3 dp, Edge e);
    
    /**
     * Sweeps this shape in the given direction, against the given triangle.
     * 
     * @param dp The direction in which to sweep.
     * @param f The face to sweep against.
     * @return The results of the sweep test, or null if missed.
     */
    SweepResult sweep(Vec3 dp, Face f);
    
    /**
     * Returns a bounding box that contains this convex shape. Should be as
     * small as possible.
     */
    Box3 bounds();
}
