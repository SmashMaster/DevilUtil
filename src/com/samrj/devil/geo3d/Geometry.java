package com.samrj.devil.geo3d;

import com.samrj.devil.math.Util;
import com.samrj.devil.math.Vec3;
import java.util.stream.Stream;

/**
 * Interface for any kind of geometry which accepts collision tests.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2016 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public interface Geometry
{
    Stream<RaycastResult> raycastUnsorted(Vec3 p0, Vec3 dp);
    
    default Stream<RaycastResult> raycast(Vec3 p0, Vec3 dp)
    {
        return raycastUnsorted(p0, dp)
                .sorted((a, b) -> Util.compare(a.time, b.time));
    }
    
    default RaycastResult raycastFirst(Vec3 p0, Vec3 dp)
    {
        return raycastUnsorted(p0, dp)
                .reduce((a, b) -> a.time < b.time ? a : b)
                .orElse(null);
    }
    
    Stream<IsectResult> intersectUnsorted(ConvexShape shape);
    
    default Stream<IsectResult> intersect(ConvexShape shape)
    {
        return intersectUnsorted(shape)
                .sorted((a, b) -> Util.compare(b.depth, a.depth, 0.0f));
    }
    
    default IsectResult intersectDeepest(ConvexShape shape)
    {
        return intersectUnsorted(shape)
                .reduce((a, b) -> a.depth > b.depth ? a : b)
                .orElse(null);
    }
    
    Stream<SweepResult> sweepUnsorted(ConvexShape shape, Vec3 dp);
    
    default Stream<SweepResult> sweep(ConvexShape shape, Vec3 dp)
    {
        return sweepUnsorted(shape, dp)
                .sorted((a, b) -> Util.compare(a.time, b.time, 0.0f));
    }
    
    default SweepResult sweepFirst(ConvexShape shape, Vec3 dp)
    {
        return sweepUnsorted(shape, dp)
                .reduce((a, b) -> a.time < b.time ? a : b)
                .orElse(null);
    }
}
