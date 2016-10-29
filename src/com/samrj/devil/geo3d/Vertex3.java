package com.samrj.devil.geo3d;

import com.samrj.devil.math.Vec3;

/**
 * Interface for vertex-like objects.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2016 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public interface Vertex3 extends GeoPrimitive
{
    /**
     * Returns a reference to the position of this vertex. Changes to the given
     * vector are reflected in the vertex, and vice-versa.
     * 
     * @return The position of this vertex.
     */
    public Vec3 a();
    
    @Override
    public default Type getType()
    {
        return Type.VERTEX;
    }
}
