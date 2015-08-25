package com.samrj.devil.geo3d;

import com.samrj.devil.math.Vec3;

/**
 * Contact class for faces.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class FaceContact extends Contact<Face>
{
    /**
     * The contact face.
     */
    public final Face face;
    
    /**
     * The contact barycentric coordinates.
     */
    public final Vec3 fbc;
    
    FaceContact(float t, float d, Vec3 cp, Vec3 p, Vec3 n, Face face, Vec3 fbc)
    {
        super(Type.FACE, t, d, cp, p, n);
        this.face = face;
        this.fbc = fbc;
    }

    @Override
    public Face contact()
    {
        return face;
    }
}
