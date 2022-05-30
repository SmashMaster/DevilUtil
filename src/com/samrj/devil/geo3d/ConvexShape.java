package com.samrj.devil.geo3d;

import com.samrj.devil.math.Vec3;

/**
 * Convex shape interface.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2022 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public interface ConvexShape
{
    /**
     * Intersects this shape against the given point.
     */
    boolean isect(Vec3 v, Isect result);

    default Isect isect(Vec3 v)
    {
        Isect result = new Isect();
        isect(v, result);
        return result;
    }
    
    /**
     * Intersects this shape against the given line segment.
     */
    boolean isect(Edge3 e, Isect result);

    default Isect isect(Edge3 e)
    {
        Isect result = new Isect();
        isect(e, result);
        return result;
    }
    
    /**
     * Intersects this shape against the given triangle.
     */
    boolean isect(Triangle3 f, Isect result);

    default Isect isect(Triangle3 f)
    {
        Isect result = new Isect();
        isect(f, result);
        return result;
    }

    /**
     * Sweeps this shape in the given direction, against the given point.
     */
    boolean sweep(Vec3 dp, Vec3 v, Sweep result);

    default Sweep sweep(Vec3 dp, Vec3 v)
    {
        Sweep result = new Sweep();
        sweep(dp, v, result);
        return result;
    }

    /**
     * Sweeps this shape in the given direction, against the given line segment.
     */
    boolean sweep(Vec3 dp, Edge3 e, Sweep result);

    default Sweep sweep(Vec3 dp, Edge3 e)
    {
        Sweep result = new Sweep();
        sweep(dp, e, result);
        return result;
    }

    /**
     * Sweeps this shape in the given direction, against the given triangle.
     */
    boolean sweep(Vec3 dp, Triangle3 f, Sweep result);

    default Sweep sweep(Vec3 dp, Triangle3 f)
    {
        Sweep result = new Sweep();
        sweep(dp, f, result);
        return result;
    }

    /**
     * Returns a bounding box that contains this convex shape. Should be as
     * small as possible.
     */
    void getBounds(Box3 result);

    default Box3 getBounds()
    {
        Box3 result = new Box3();
        getBounds(result);
        return result;
    }
}
