package com.samrj.devil.geo3d;

import com.samrj.devil.math.Vec3;

/**
 * Interface for any kind of geometry which accepts collision tests.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2020 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public interface Geometry
{
    /**
     * Performs an unsorted ray query on this geometry.
     *
     * @param p0 The starting point of the ray.
     * @param dp The direction of the ray.
     * @param terminated Whether the ray ends at p0 + dp.
     * @return A new query iterator.
     */
    Query<Ray> ray(Vec3 p0, Vec3 dp, boolean terminated);

    /**
     * Performs an unsorted intersection query on this geometry.
     *
     * @param shape The shape to intersect with.
     * @return A new query iterator.
     */
    Query<Isect> isect(ConvexShape shape);

    /**
     * Performs an unsorted sweeep test on this geometry.
     *
     * @param shape The shape to sweep against.
     * @param dp The displacement to sweep by.
     * @return A new query iterator.
     */
    Query<Sweep> sweep(ConvexShape shape, Vec3 dp);

    default boolean rayFirst(Vec3 p0, Vec3 dp, boolean terminated, Ray result)
    {
        boolean hit = false;
        Ray temp = new Ray();
        Query<Ray> it = ray(p0, dp, terminated);
        while (it.hasNext()) if (it.next(temp))
        {
            hit = true;
            if (temp.time < result.time) result.set(temp);
        }
        return hit;
    }

    default boolean isectDeepest(ConvexShape shape, Isect result)
    {
        boolean hit = false;
        Isect temp = new Isect();
        Query<Isect> it = isect(shape);
        while (it.hasNext()) if (it.next(temp))
        {
            hit = true;
            if (temp.depth > result.depth) result.set(temp);
        }
        return hit;
    }

    default boolean sweepFirst(ConvexShape shape, Vec3 dp, Sweep result)
    {
        boolean hit = false;
        Sweep temp = new Sweep();
        Query<Sweep> it = sweep(shape, dp);
        while (it.hasNext()) if (it.next(temp))
        {
            hit = true;
            if (temp.time < result.time) result.set(temp);
        }
        return hit;
    }

    default Ray rayFirst(Vec3 p0, Vec3 dp, boolean terminated)
    {
        Ray result = new Ray();
        return rayFirst(p0, dp, terminated, result) ? result : null;
    }

    default Isect isectDeepest(ConvexShape shape)
    {
        Isect result = new Isect();
        return isectDeepest(shape, result) ? result : null;
    }

    default Sweep sweepFirst(ConvexShape shape, Vec3 dp)
    {
        Sweep result = new Sweep();
        return sweepFirst(shape, dp, result) ? result : null;
    }

    //Bounds-related methods
    default boolean areBoundsDirty()
    {
        return false;
    }

    default void markBoundsDirty() {}

    default void updateBounds() {}

    default void getBounds(Box3 result)
    {
        Box3.infinite(result);
    }

    default Box3 getBounds()
    {
        Box3 result = new Box3();
        getBounds(result);
        return result;
    }

    default boolean boundsTouchingRay(Vec3 p0, Vec3 dp, boolean terminated)
    {
        return Box3.touchingRay(getBounds(), p0, dp, terminated);
    }

    default boolean boundsTouchingBox(Box3 box)
    {
        return getBounds().touching(box);
    }

    /**
     * Returns true if a ray between the two given points does not intersect this geometry.
     */
    default boolean areVisible(Vec3 a, Vec3 b)
    {
        Vec3 dp = Vec3.sub(b, a);
        Query<Ray> it = ray(a, dp, true);
        Ray temp = new Ray();
        while (it.hasNext()) if (it.next(temp)) return false;
        return true;
    }

    interface Query<T>
    {
        boolean hasNext();
        boolean next(T result);
    }
}
