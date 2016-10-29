package com.samrj.devil.geo3d;

import com.samrj.devil.math.Vec3;

/**
 * Interface for edge-like objects.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2016 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public interface Edge3 extends GeoPrimitive
{
    /**
     * Returns a reference to the position of the first vertex of this edge.
     * Changes to the edge are reflected in the vertex, and vice-versa.
     * 
     * @return The first vertex of this edge.
     */
    public Vec3 a();
    
    /**
     * Returns a reference to the position of the second vertex of this edge.
     * Changes to the edge are reflected in the vertex, and vice-versa.
     * 
     * @return The second vertex of this edge.
     */
    public Vec3 b();
    
    @Override
    public default Type getType()
    {
        return Type.EDGE;
    }
}
