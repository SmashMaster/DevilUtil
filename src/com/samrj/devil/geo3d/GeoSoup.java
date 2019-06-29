package com.samrj.devil.geo3d;

import com.samrj.devil.math.Vec3;
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
    private final V[] verts;
    private final E[] edges;
    private final F[] faces;
    
    private final Box3 bounds = Box3.infinite();
    private boolean boundsDirty = true;
    
    public GeoSoup(V[] verts, E[] edges, F[] faces)
    {
        this.verts = verts;
        this.edges = edges;
        this.faces = faces;
    }
    
    public Stream<V> verts()
    {
        return Stream.of(verts);
    }
    
    public Stream<E> edges()
    {
        return Stream.of(edges);
    }
    
    public Stream<F> faces()
    {
        return Stream.of(faces);
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
        verts().forEach(v -> bounds.expand(v.p()));
        edges().forEach(e ->
        {
            bounds.expand(e.a().p());
            bounds.expand(e.b().p());
        });
        faces().forEach(f ->
        {
            bounds.expand(f.a().p());
            bounds.expand(f.b().p());
            bounds.expand(f.c().p());
        });
        
        boundsDirty = false;
    }
}
