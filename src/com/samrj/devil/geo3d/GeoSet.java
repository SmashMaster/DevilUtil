package com.samrj.devil.geo3d;

import com.samrj.devil.math.Vec3;

import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Class for sets of geometry, which handles bounding box culling. Can contain
 * other GeoSets to act as a bounding volume hierarchy.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2020 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class GeoSet implements Geometry
{
    private final Supplier<Stream<Geometry>> provider;
    private final Box3 bounds = Box3.infinite();
    
    public GeoSet(Supplier<Stream<Geometry>> provider)
    {
        this.provider = provider;
    }
    
    @Override
    public Stream<RaycastResult> raycastUnsorted(Vec3 p0, Vec3 dp, boolean terminated)
    {
        return provider.get()
                .filter(geom -> Box3.touchingRay(geom.getBounds(), p0, dp, terminated))
                .flatMap(geom -> geom.raycastUnsorted(p0, dp, terminated));
    }

    @Override
    public Stream<IsectResult> intersectUnsorted(ConvexShape shape)
    {
        Box3 shapeBounds = shape.getBounds();
        return provider.get()
                .filter(geom -> Box3.touching(shapeBounds, geom.getBounds()))
                .flatMap(geom -> geom.intersectUnsorted(shape));
    }

    @Override
    public Stream<SweepResult> sweepUnsorted(ConvexShape shape, Vec3 dp)
    {
        Box3 shapeBounds = shape.getBounds().sweep(dp);
        return provider.get()
                .filter(geom -> Box3.touching(shapeBounds, geom.getBounds()))
                .flatMap(geom -> geom.sweepUnsorted(shape, dp));
    }
    
    @Override
    public Stream<Vec3> verts()
    {
        return provider.get().flatMap(Geometry::verts);
    }

    @Override
    public Stream<Edge3> edges()
    {
        return provider.get().flatMap(Geometry::edges);
    }
    
    @Override
    public Stream<Triangle3> faces()
    {
        return provider.get().flatMap(Geometry::faces);
    }
    
    @Override
    public Box3 getBounds()
    {
        if (areBoundsDirty()) updateBounds();
        return new Box3(bounds);
    }
    
    @Override
    public boolean areBoundsDirty()
    {
        return provider.get().anyMatch(Geometry::areBoundsDirty);
    }
    
    @Override
    public void markBoundsDirty()
    {
        provider.get().forEach(Geometry::markBoundsDirty);
    }
    
    @Override
    public void updateBounds()
    {
        bounds.setEmpty();
        provider.get().filter(Geometry::areBoundsDirty).forEach(Geometry::updateBounds);
        provider.get().map(Geometry::getBounds).forEach(bounds::expand);
    }
}
