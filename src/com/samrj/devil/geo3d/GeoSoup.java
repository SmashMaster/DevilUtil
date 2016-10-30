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
 * @copyright 2016 Samuel Johnson
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
    
    public Stream<V> verts()
    {
        return vertProvider.get();
    }
    
    public Stream<E> edges()
    {
        return edgeProvider.get();
    }
    
    public Stream<F> faces()
    {
        return faceProvider.get();
    }
    
    @Override
    public Stream<RaycastResult> raycastUnsorted(Vec3 p0, Vec3 dp, boolean terminated)
    {
        return faceProvider.get()
                .map(f -> Geo3DUtil.raycast(f, p0, dp, terminated))
                .filter(e -> e != null);
    }

    @Override
    public Stream<IsectResult> intersectUnsorted(ConvexShape shape)
    {
        return Stream.concat(Stream.concat(
                faceProvider.get().map(f -> shape.isect(f)),
                edgeProvider.get().map(e -> shape.isect(e))),
                vertProvider.get().map(v -> shape.isect(v)))
                    .filter(e -> e != null);
    }

    @Override
    public Stream<SweepResult> sweepUnsorted(ConvexShape shape, Vec3 dp)
    {
        return Stream.concat(Stream.concat(
                faceProvider.get().map(f -> shape.sweep(dp, f)),
                edgeProvider.get().map(e -> shape.sweep(dp, e))),
                vertProvider.get().map(v -> shape.sweep(dp, v)))
                    .filter(e -> e != null);
    }
    
    @Override
    public Box3 getBounds()
    {
        return new Box3(bounds);
    }
    
    public void markBoundsDirty()
    {
        boundsDirty = true;
        bounds.setInfinite();
    }
    
    @Override
    public boolean areBoundsDirty()
    {
        return boundsDirty;
    }
    
    @Override
    public void updateBounds()
    {
        bounds.setEmpty();
        vertProvider.get().forEach(v -> bounds.expand(v.p()));
        edgeProvider.get().forEach(e ->
        {
            bounds.expand(e.a().p());
            bounds.expand(e.b().p());
        });
        faceProvider.get().forEach(f ->
        {
            bounds.expand(f.a().p());
            bounds.expand(f.b().p());
            bounds.expand(f.c().p());
        });
        
        boundsDirty = false;
    }
}
