package com.samrj.devil.geo3d;

import com.samrj.devil.math.Vec3;

import java.util.Iterator;
import java.util.Objects;

/**
 * Basic geometry class which stores unordered, unstructured mesh data.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2022 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class GeoMesh implements Geometry
{
    private final Iterable<? extends Vec3> verts;
    private final Iterable<? extends Edge3> edges;
    private final Iterable<? extends Triangle3> faces;
    
    private final Box3 bounds = Box3.infinite();
    private boolean boundsDirty = true;

    public GeoMesh(Iterable<? extends Vec3> verts, Iterable<? extends Edge3> edges, Iterable<? extends Triangle3> faces)
    {
        this.verts = Objects.requireNonNull(verts);
        this.edges = Objects.requireNonNull(edges);
        this.faces = Objects.requireNonNull(faces);
    }

    @Override
    public Query<Ray> ray(Vec3 p0, Vec3 dp, boolean terminated)
    {
        return new RayQuery(p0, dp, terminated);
    }

    @Override
    public Query<Isect> isect(ConvexShape shape)
    {
        return new IsectQuery(shape);
    }

    @Override
    public Query<Sweep> sweep(ConvexShape shape, Vec3 dp)
    {
        return new SweepQuery(shape, dp);
    }

    @Override
    public void getBounds(Box3 result)
    {
        if (boundsDirty) updateBounds();
        result.set(bounds);
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
        for (Triangle3 face : faces) bounds.expand(face);
        for (Edge3 edge : edges) bounds.expand(edge);
        for (Vec3 vert : verts) bounds.expand(vert);
        boundsDirty = false;
    }

    private class RayQuery implements Query<Ray>
    {
        private final Vec3 p0, dp;
        private final boolean terminated;
        private final Iterator<? extends Triangle3> fit;

        private RayQuery(Vec3 p0, Vec3 dp, boolean terminated)
        {
            this.p0 = p0;
            this.dp = dp;
            this.terminated = terminated;
            fit = faces.iterator();
        }

        @Override
        public boolean hasNext()
        {
            return fit.hasNext();
        }

        @Override
        public boolean next(Ray result)
        {
            return Geo3DUtil.raycast(fit.next(), p0, dp, terminated, result);
        }
    }

    private class IsectQuery implements Query<Isect>
    {
        private final ConvexShape shape;
        private final Iterator<? extends Triangle3> fit;
        private final Iterator<? extends Edge3> eit;
        private final Iterator<? extends Vec3> vit;

        private IsectQuery(ConvexShape shape)
        {
            this.shape = shape;
            fit = faces.iterator();
            eit = edges.iterator();
            vit = verts.iterator();
        }

        @Override
        public boolean hasNext()
        {
            return fit.hasNext() || eit.hasNext() || vit.hasNext();
        }

        @Override
        public boolean next(Isect result)
        {
            if (fit.hasNext()) return shape.isect(fit.next(), result);
            else if (eit.hasNext()) return shape.isect(eit.next(), result);
            else return shape.isect(vit.next(), result);
        }
    }

    private class SweepQuery implements Query<Sweep>
    {
        private final ConvexShape shape;
        private final Vec3 dp;
        private final Iterator<? extends Triangle3> fit;
        private final Iterator<? extends Edge3> eit;
        private final Iterator<? extends Vec3> vit;

        private SweepQuery(ConvexShape shape, Vec3 dp)
        {
            this.shape = shape;
            this.dp = dp;
            fit = faces.iterator();
            eit = edges.iterator();
            vit = verts.iterator();
        }

        @Override
        public boolean hasNext()
        {
            return fit.hasNext() || eit.hasNext() || vit.hasNext();
        }

        @Override
        public boolean next(Sweep result)
        {
            if (fit.hasNext()) return shape.sweep(dp, fit.next(), result);
            else if (eit.hasNext()) return shape.sweep(dp, eit.next(), result);
            else return shape.sweep(dp, vit.next(), result);
        }
    }
}
