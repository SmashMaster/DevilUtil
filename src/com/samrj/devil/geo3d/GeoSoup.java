package com.samrj.devil.geo3d;

import com.samrj.devil.math.Vec3;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Basic geometry class which stores unordered, unstructured mesh data.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @param <V> The type of vertex this soup contains.
 * @param <E> The type of edge this soup contains.
 * @param <T> The type of triangle this soup contains.
 * @copyright 2016 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class GeoSoup<V extends Vertex3, E extends Edge3, T extends Triangle3> implements Geometry
{
    public final List<V> verts = new ArrayList<>();
    public final List<E> edges = new ArrayList<>();
    public final List<T> faces = new ArrayList<>();
    
    private final Box3 bounds = Box3.infinite();
    private boolean boundsDirty = true;

    @Override
    public Stream<RaycastResult> raycastUnsorted(Vec3 p0, Vec3 dp, boolean terminated)
    {
        return faces.stream()
                .map(f -> Geo3DUtil.raycast(f, p0, dp, terminated))
                .filter(e -> e != null);
    }

    @Override
    public Stream<IsectResult> intersectUnsorted(ConvexShape shape)
    {
        return Stream.concat(Stream.concat(
                faces.stream().map(f -> shape.isect(f)),
                edges.stream().map(e -> shape.isect(e))),
                verts.stream().map(v -> shape.isect(v)))
                    .filter(e -> e != null);
    }

    @Override
    public Stream<SweepResult> sweepUnsorted(ConvexShape shape, Vec3 dp)
    {
        return Stream.concat(Stream.concat(
                faces.stream().map(f -> shape.sweep(dp, f)),
                edges.stream().map(e -> shape.sweep(dp, e))),
                verts.stream().map(v -> shape.sweep(dp, v)))
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
        for (Vertex3 v : verts) bounds.expand(v.p());
        for (Edge3 e : edges)
        {
            bounds.expand(e.a().p());
            bounds.expand(e.b().p());
        }
        for (Triangle3 f : faces)
        {
            bounds.expand(f.a().p());
            bounds.expand(f.b().p());
            bounds.expand(f.c().p());
        }
        
        boundsDirty = false;
    }
}
