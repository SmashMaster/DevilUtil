package com.samrj.devil.geo3d;

import com.samrj.devil.math.Vec3;

/**
 * Contact class for edges.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class EdgeContact extends Contact<Edge>
{
    /**
     * The edge of the contact.
     */
    public final Edge edge;
    
    /**
     * The edge contact interpolant.
     */
    public final float et;
    
    EdgeContact(float t, float d, Vec3 p, Vec3 n, Edge edge, float et)
    {
        super(Type.EDGE, t, d, p, n);
        this.edge = edge;
        this.et = et;
    }

    @Override
    public Edge contact()
    {
        return edge;
    }
}
