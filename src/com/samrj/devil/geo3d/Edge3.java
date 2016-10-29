package com.samrj.devil.geo3d;

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
     * Returns the first vertex of this edge.
     * 
     * @return The first vertex of this edge.
     */
    public Vertex3 a();
    
    /**
     * Returns the second vertex of this edge.
     * 
     * @return The second vertex of this edge.
     */
    public Vertex3 b();
    
    @Override
    public default Type getType()
    {
        return Type.EDGE;
    }
}
