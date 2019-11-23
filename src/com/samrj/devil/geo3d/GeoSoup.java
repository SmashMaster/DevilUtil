package com.samrj.devil.geo3d;

import com.samrj.devil.math.Vec3;
import java.util.Collection;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Basic geometry class which stores unordered, unstructured mesh data.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @param <V> The type of vertex this soup contains.
 * @param <E> The type of edge this soup contains.
 * @param <F> The type of face this soup contains.
 * @copyright 2019 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class GeoSoup<V extends Vertex3, E extends Edge3, F extends Triangle3> implements Geometry
{
    private final Supplier<Stream<V>> vertProvider;
    private final Supplier<Stream<E>> edgeProvider;
    private final Supplier<Stream<F>> faceProvider;
    
    private final Box3 bounds = Box3.infinite();
    
    private boolean boundsDirty = true;
    
    public GeoSoup(Supplier<Stream<V>> vProvider, Supplier<Stream<E>> eProvider, Supplier<Stream<F>> fProvider)
    {
        if (vProvider == null) throw new NullPointerException();
        if (eProvider == null) throw new NullPointerException();
        if (fProvider == null) throw new NullPointerException();
        
        vertProvider = vProvider;
        edgeProvider = eProvider;
        faceProvider = fProvider;
    }
    
    public GeoSoup(Collection<V> verts, Collection<E> edges, Collection<F> faces)
    {
        this(verts::stream, edges::stream, faces::stream);
    }
    
    public GeoSoup(V[] verts, E[] edges, F[] faces)
    {
        this(() -> Stream.of(verts), () -> Stream.of(edges), () -> Stream.of(faces));
    }
    
    @Override
    public Stream<V> verts()
    {
        return vertProvider.get();
    }
    
    @Override
    public Stream<E> edges()
    {
        return edgeProvider.get();
    }
    
    @Override
    public Stream<F> faces()
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
