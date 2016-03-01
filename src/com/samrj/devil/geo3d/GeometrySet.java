package com.samrj.devil.geo3d;

import com.samrj.devil.math.Vec3;
import java.util.stream.Stream;

/**
 * Interface for any kind of geometry which accepts collision tests.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2016 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public interface GeometrySet extends Geometry
{
    Stream<Geometry> getAllGeometry();
    
    @Override
    default Stream<RaycastResult> raycastUnsorted(Vec3 p0, Vec3 dp)
    {
        return getAllGeometry().flatMap((geom) -> geom.raycastUnsorted(p0, dp));
    }
    
    @Override
    default Stream<IsectResult> intersectUnsorted(ConvexShape shape)
    {
        return getAllGeometry().flatMap((geom) -> geom.intersectUnsorted(shape));
    }
    
    @Override
    default Stream<SweepResult> sweepUnsorted(ConvexShape shape, Vec3 dp)
    {
        return getAllGeometry().flatMap((geom) -> geom.sweepUnsorted(shape, dp));
    }
}
