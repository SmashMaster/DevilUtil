package com.samrj.devil.geo3d;

import com.samrj.devil.math.Vec3;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Class for sets of geometry, which handles bounding box culling. Can contain
 * other GeoSets to act as a bounding volume hierarchy.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2019 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class GeoSet<V extends Vertex3, E extends Edge3, F extends Triangle3> implements Geometry<V, E, F>
{
    private final Supplier<Stream<Geometry<V, E, F>>> provider;
    private final Box3 bounds = Box3.infinite();
    
    public GeoSet(Supplier<Stream<Geometry<V, E, F>>> provider)
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
    public Stream<V> verts()
    {
        return provider.get().flatMap(Geometry::verts);
    }

    @Override
    public Stream<E> edges()
    {
        return provider.get().flatMap(Geometry::edges);
    }
    
    @Override
    public Stream<F> faces()
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
