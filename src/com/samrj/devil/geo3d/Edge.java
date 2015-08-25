package com.samrj.devil.geo3d;

import com.samrj.devil.math.Vec3;

/**
 * 3D edge class.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Edge
{
    public Vec3 a, b;
    
    public Edge(Vec3 a, Vec3 b)
    {
        this.a = a; this.b = b;
    }
    
    public Edge()
    {
    }
    
    public boolean equals(Vec3 a, Vec3 b)
    {
        return (this.a == a && this.b == b) ||
               (this.a == b && this.b == a);
    }
    
    public boolean equals(Edge edge)
    {
        return equals(edge.a, edge.b);
    }
}
