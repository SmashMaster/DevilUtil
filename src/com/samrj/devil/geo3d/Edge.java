package com.samrj.devil.geo3d;

/**
 * 3D edge class.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Edge
{
    public Vertex a, b;
    
    public Edge(Vertex a, Vertex b)
    {
        this.a = a; this.b = b;
    }
    
    public Edge()
    {
    }
    
    public boolean equals(Vertex a, Vertex b)
    {
        return (this.a == a && this.b == b) ||
               (this.a == b && this.b == a);
    }
    
    public boolean equals(Edge edge)
    {
        return equals(edge.a, edge.b);
    }
    
    /**
     * Returns a new edge contact if the given ellipsoid cast hits this edge,
     * or null if it doesn't.
     * 
     * @param ellipsoid The ellipsoid cast to test against this edge.
     * @return A new edge contact if the given ellipsoid cast hits this edge,
     *         or null if it doesn't.
     */
    public EdgeContact cast(EllipsoidCast ellipsoid)
    {
        return null;
    }
}
