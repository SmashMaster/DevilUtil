package com.samrj.devil.geo3d;

import com.samrj.devil.math.Vec3;

/**
 * Contact class for vertices.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class VertexContact extends Contact<Vertex>
{
    public final Vertex point;
    
    VertexContact(float t, float d, Vec3 cp, Vertex p, Vec3 n)
    {
        super(Type.POINT, t, d, cp, p, n);
        this.point = p;
    }

    @Override
    public Vertex contact()
    {
        return point;
    }
}
