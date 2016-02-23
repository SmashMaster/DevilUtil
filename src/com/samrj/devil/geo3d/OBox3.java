package com.samrj.devil.geo3d;

import com.samrj.devil.math.Mat3;
import com.samrj.devil.math.Vec3;

/**
 * Oriented bounding box class.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class OBox3
{
    private static final float EPSILON = 1.0f/65536.0f;
    
    public final Vec3 pos = new Vec3();
    public final Mat3 rot = new Mat3();
    public final Vec3 radii = new Vec3();
    
    public boolean touching(OBox3 b)
    {
        Mat3 m = Mat3.mult(rot, b.rot);
        Vec3 t = Vec3.sub(b.pos, pos).mult(rot);
        
        Mat3 absM = new Mat3();
        for (int i=0; i<3; i++) for (int j=0; j<3; j++)
            absM.setEntry(i, j, Math.abs(m.getEntry(i, j)) + EPSILON);
        
        for (int i=0; i<3; i++)
        {
            float ra = radii.x;
//            float rb = dot(radii, )
//            if (Math.abs(t.getComponent(i)) > ra + rb) return false;
        }
        
        return true;
    }
}
