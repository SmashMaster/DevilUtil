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
    public static Vertex3 from(Vec3 v)
    {
        return () -> v;
    }
    
    /**
     * Returns a reference to the position of this vertex. Changes to the given
     * vector are reflected in the vertex, and vice-versa.
     * 
     * @return The position of this vertex.
     */
    public Vec3 p();
    
    @Override
    public default Type getType()
    {
        return Type.VERTEX;
    }
}
