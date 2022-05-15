package com.samrj.devil.geo3d;

import com.samrj.devil.math.Vec3;

import java.util.Collection;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Basic geometry class which stores unordered, unstructured mesh data.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2019 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class GeoSoup implements Geometry
{
    private static <T> Supplier<Stream<T>> streamSupplier(Collection<T> collection)
    {
        if (collection == null) return Stream::empty;
        else return collection::stream;
    }
    
    private static <T> Supplier<Stream<T>> streamSupplier(T[] arr)
    {
        if (arr == null) return Stream::empty;
        else return () -> Stream.of(arr);
    }
    
    private final Supplier<Stream<Vec3>> vertProvider;
    private final Supplier<Stream<Edge3>> edgeProvider;
    private final Supplier<Stream<Triangle3>> faceProvider;
    
    private final Box3 bounds = Box3.infinite();
    
    private boolean boundsDirty = true;
    
    public GeoSoup(Supplier<Stream<Vec3>> vProvider, Supplier<Stream<Edge3>> eProvider, Supplier<Stream<Triangle3>> fProvider)
    {
        if (vProvider == null) vProvider = Stream::empty;
        if (eProvider == null) eProvider = Stream::empty;
        if (fProvider == null) fProvider = Stream::empty;
        
        vertProvider = vProvider;
        edgeProvider = eProvider;
        faceProvider = fProvider;
    }
    
    public GeoSoup(Collection<Vec3> verts, Collection<Edge3> edges, Collection<Triangle3> faces)
    {
        this(streamSupplier(verts), streamSupplier(edges), streamSupplier(faces));
    }
    
    public GeoSoup(Vec3[] verts, Edge3[] edges, Triangle3[] faces)
    {
        this(streamSupplier(verts), streamSupplier(edges), streamSupplier(faces));
    }
    
    @Override
    public Stream<Vec3> verts()
    {
        return vertProvider.get();
    }
    
    @Override
    public Stream<Edge3> edges()
    {
        return edgeProvider.get();
    }
    
    @Override
    public Stream<Triangle3> faces()
    {
        return faceProvider.get();
    }
    
    @Override
    public Stream<RaycastResult> raycastUnsorted(Vec3 p0, Vec3 dp, boolean terminated)
    {
        return faces()
                .map(f -> Geo3DUtil.raycast(f, p0, dp, terminated))
                .filter(e -> e != null);
    }

    @Override
    public Stream<IsectResult> intersectUnsorted(ConvexShape shape)
    {
        return Stream.concat(Stream.concat(
                faces().map(f -> shape.isect(f)),
                edges().map(e -> shape.isect(e))),
                verts().map(v -> shape.isect(v)))
                    .filter(e -> e != null);
    }

    @Override
    public Stream<SweepResult> sweepUnsorted(ConvexShape shape, Vec3 dp)
    {
        Box3 sBounds = shape.getBounds().sweep(dp);
        return Stream.concat(Stream.concat(
                faces().filter(f -> Box3.contain(f).touching(sBounds)).map(f -> shape.sweep(dp, f)),
                edges().filter(e -> Box3.contain(e).touching(sBounds)).map(e -> shape.sweep(dp, e))),
                verts().filter(v -> sBounds.touching(v)).map(v -> shape.sweep(dp, v)))
                    .filter(e -> e != null);
    }
    
    @Override
    public Box3 getBounds()
    {
        if (boundsDirty) updateBounds();
        return new Box3(bounds);
    }
    
    @Override
    public boolean areBoundsDirty()
    {
        return boundsDirty;
    }
    
    @Override
    public void markBoundsDirty()
    {
        boundsDirty = true;
    }
    
    @Override
    public void updateBounds()
    {
        bounds.setEmpty();
        verts().forEach(bounds::expand);
        edges().forEach(bounds::expand);
        faces().forEach(bounds::expand);
        boundsDirty = false;
    }
}
