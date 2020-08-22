package com.samrj.devil.geo3d;

import com.samrj.devil.math.Util;
import com.samrj.devil.math.Vec3;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Interface for any kind of geometry which accepts collision tests.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2020 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public interface Geometry
{
    //Base methods
    Stream<RaycastResult> raycastUnsorted(Vec3 p0, Vec3 dp, boolean terminated);
    Stream<IsectResult> intersectUnsorted(ConvexShape shape);
    Stream<SweepResult> sweepUnsorted(ConvexShape shape, Vec3 dp);
    Stream<Vec3> verts();
    Stream<Edge3> edges();
    Stream<Triangle3> faces();
    
    //Ordered streams
    default Stream<RaycastResult> raycast(Vec3 p0, Vec3 dp, boolean terminated)
    {
        return raycastUnsorted(p0, dp, terminated)
                .sorted((a, b) -> Util.compare(a.time, b.time));
    }
    
    default Stream<IsectResult> intersect(ConvexShape shape)
    {
        return intersectUnsorted(shape)
                .sorted((a, b) -> Util.compare(b.depth, a.depth, 0.0f));
    }
    
    default Stream<SweepResult> sweep(ConvexShape shape, Vec3 dp)
    {
        return sweepUnsorted(shape, dp)
                .sorted((a, b) -> Util.compare(a.time, b.time, 0.0f));
    }
    
    //Single-result methods
    default Optional<RaycastResult> raycastFirst(Vec3 p0, Vec3 dp, boolean terminated)
    {
        return raycastUnsorted(p0, dp, terminated)
                .reduce((a, b) -> a.time < b.time ? a : b);
    }
    
    default Optional<IsectResult> intersectDeepest(ConvexShape shape)
    {
        return intersectUnsorted(shape)
                .reduce((a, b) -> a.depth > b.depth ? a : b);
    }
    
    default Optional<SweepResult> sweepFirst(ConvexShape shape, Vec3 dp)
    {
        return sweepUnsorted(shape, dp)
                .reduce((a, b) -> a.time < b.time ? a : b);
    }
    
    //Bounds-related methods
    default Box3 getBounds()
    {
        return Box3.infinite();
    }
    
    default boolean areBoundsDirty()
    {
        return false;
    }
    
    void markBoundsDirty();
    
    default void updateBounds()
    {
    }
    
    //Utility stuff
    
    /**
     * Returns false if the ray between the two given points intersects this
     * geometry, true otherwise.
     */
    default boolean areVisible(Vec3 a, Vec3 b)
    {
        Vec3 dp = Vec3.sub(b, a);
        return !raycast(a, dp, true).findAny().isPresent();
    }
}
