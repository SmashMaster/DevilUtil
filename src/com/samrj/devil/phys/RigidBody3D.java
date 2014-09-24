package com.samrj.devil.phys;

import com.samrj.devil.math.Matrix3f;
import com.samrj.devil.math.Quat4f;
import com.samrj.devil.math.Vector3f;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class RigidBody3D
{
    private final Vector3f pos = new Vector3f();
    private final Vector3f mom = new Vector3f();
    private float mass = 0f;
    
    private final Quat4f orient = new Quat4f();
    private final Vector3f angMom = new Vector3f();
    private final Matrix3f moi = new Matrix3f();
    
    public void applyImpulse(Vector3f impulse)
    {
        mom.add(impulse.cmult(mass));
    }
    
    public void applyImpulse(Vector3f impulse, Vector3f localPosition)
    {
        applyImpulse(impulse);
        applyAngularImpulse(impulse.clone().cross(localPosition));
    }
    
    public void applyAngularImpulse(Vector3f angImp)
    {
        angMom.add(angImp.cmult(moi));
    }
}
