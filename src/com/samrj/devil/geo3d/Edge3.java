package com.samrj.devil.geo3d;

import com.samrj.devil.math.Vec3;

/**
 * Interface for edge-like objects.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @param <V> The type of vertex this edge stores.
 * @copyright 2016 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public interface Edge3<V extends Vertex3> extends GeoPrimitive
{
    public static Edge3 from(Vertex3 a, Vertex3 b)
    {
        return new Edge3()
        {
            @Override
            public Vertex3 a()
            {
                return a;
            }

            @Override
            public Vertex3 b()
            {
                return b;
            }
        };
    }
    
    public static Edge3 from(Vec3 a, Vec3 b)
    {
        return from(Vertex3.from(a), Vertex3.from(b));
    }
    
    /**
     * Returns the first vertex of this edge.
     * 
     * @return The first vertex of this edge.
     */
    public V a();
    
    /**
     * Returns the second vertex of this edge.
     * 
     * @return The second vertex of this edge.
     */
    public V b();
    
    @Override
    public default Type getGeoPrimitiveType()
    {
        return Type.EDGE;
    }
}
