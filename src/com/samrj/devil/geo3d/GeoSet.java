package com.samrj.devil.geo3d;

import com.samrj.devil.math.Vec3;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Class for sets of geometry, which handles bounding box culling. Can contain
 * other GeoSets to act as a bounding volume hierarchy.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2016 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class GeoSet implements Geometry
{
    public final List<Geometry> list = new ArrayList<>();
    
    private final Box3 bounds = Box3.infinite();
    
    @Override
    public Stream<RaycastResult> raycastUnsorted(Vec3 p0, Vec3 dp, boolean terminated)
    {
        return list.stream()
                .filter(geom -> Box3.touchingRay(geom.getBounds(), p0, dp, terminated))
                .flatMap(geom -> geom.raycastUnsorted(p0, dp, terminated));
    }

    @Override
    public Stream<IsectResult> intersectUnsorted(ConvexShape shape)
    {
        Box3 shapeBounds = shape.getBounds();
        return list.stream()
                .filter(geom -> Box3.touching(shapeBounds, geom.getBounds()))
                .flatMap(geom -> geom.intersectUnsorted(shape));
    }

    @Override
    public Stream<SweepResult> sweepUnsorted(ConvexShape shape, Vec3 dp)
    {
        Box3 shapeBounds = shape.getBounds().sweep(dp);
        return list.stream()
                .filter(geom -> Box3.touching(shapeBounds, geom.getBounds()))
                .flatMap(geom -> geom.sweepUnsorted(shape, dp));
    }
    
    @Override
    public Box3 getBounds()
    {
        return new Box3(bounds);
    }
    
    @Override
    public boolean areBoundsDirty()
    {
        return list.stream().anyMatch(Geometry::areBoundsDirty);
    }
    
    @Override
    public void updateBounds()
    {
        bounds.setEmpty();
        list.stream().filter(Geometry::areBoundsDirty).forEach(Geometry::updateBounds);
        list.stream().map(Geometry::getBounds).forEach(bounds::expand);
    }
}
