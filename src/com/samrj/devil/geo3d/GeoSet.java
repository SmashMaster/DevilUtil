package com.samrj.devil.geo3d;

import com.samrj.devil.math.Vec3;

import java.util.Iterator;
import java.util.function.Function;

/**
 * Class for sets of geometry, which handles bounding box culling. Can contain other GeoSets to act as a bounding
 * volume hierarchy.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2022 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class GeoSet implements Geometry
{
    private final Iterable<? extends Geometry> geoms;
    private final Box3 bounds = Box3.infinite();

    public GeoSet(Iterable<? extends Geometry> geoms)
    {
        this.geoms = geoms;
    }

    @Override
    public Query<Ray> ray(Vec3 p0, Vec3 dp, boolean terminated)
    {
        return new SetQuery<>(geom -> geom.boundsTouchingRay(p0, dp, terminated) ? geom.ray(p0, dp, terminated) : null);
    }

    @Override
    public Query<Isect> isect(ConvexShape shape)
    {
        Box3 shapeBounds = shape.getBounds();
        return new SetQuery<>(geom -> geom.boundsTouchingBox(shapeBounds) ? geom.isect(shape) : null);
    }

    @Override
    public Query<Sweep> sweep(ConvexShape shape, Vec3 dp)
    {
        Box3 shapeBounds = shape.getBounds().sweep(dp);
        return new SetQuery<>(geom -> geom.boundsTouchingBox(shapeBounds) ? geom.sweep(shape, dp) : null);
    }

    @Override
    public void getBounds(Box3 result)
    {
        if (areBoundsDirty()) updateBounds();
        result.set(bounds);
    }

    @Override
    public boolean areBoundsDirty()
    {
        for (Geometry geom : geoms) if (geom.areBoundsDirty()) return true;
        return false;
    }
    
    @Override
    public void markBoundsDirty()
    {
        for (Geometry geom : geoms) geom.markBoundsDirty();
    }
    
    @Override
    public void updateBounds()
    {
        bounds.setEmpty();
        for (Geometry geom : geoms) if (geom.areBoundsDirty()) geom.updateBounds();
        Box3 temp = new Box3();
        for (Geometry geom : geoms)
        {
            geom.getBounds(temp);
            bounds.expand(temp);
        }
    }

    private class SetQuery<T> implements Query<T>
    {
        private final Function<Geometry, Query<T>> func;
        private final Iterator<? extends Geometry> git = geoms.iterator();
        private Query<T> rit;

        private SetQuery(Function<Geometry, Query<T>> func)
        {
            this.func = func;
            while (rit == null && git.hasNext())
                rit = func.apply(git.next());
        }

        @Override
        public boolean hasNext()
        {
            return rit != null && rit.hasNext();
        }

        @Override
        public boolean next(T result)
        {
            boolean hit = rit.next(result);
            while (!hasNext() && git.hasNext())
                rit = func.apply(git.next());
            return hit;
        }
    }
}
