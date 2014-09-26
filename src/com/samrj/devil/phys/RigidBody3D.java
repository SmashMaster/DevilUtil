package com.samrj.devil.phys;

import com.samrj.devil.math.Matrix3f;
import com.samrj.devil.math.Matrix4f;
import com.samrj.devil.math.Quat4f;
import com.samrj.devil.math.Vector3f;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class RigidBody3D
{
    public final Vector3f pos = new Vector3f();
    public final Vector3f vel = new Vector3f();
    public float mass = 0f;
    
    public final Quat4f orient = new Quat4f();
    public final Vector3f angVel = new Vector3f();
    public final MOI moi = new MOI();
    
    public final Matrix4f worldToLocal = new Matrix4f();
    public final Matrix3f worldDirToLocal = new Matrix3f();
    public final Matrix3f localDirToWorld = new Matrix3f();
    
    public RigidBody3D(float mass, MOI moi)
    {
        this.mass = mass;
        this.moi.set(moi);
    }
    
    public void updateMatrices()
    {
        localDirToWorld.set(orient.toMatrix3f());
        
        Matrix3f invOrientMat3 = orient.clone().invert().toMatrix3f();
        Matrix4f invOrientMat4 = invOrientMat3.toMatrix4f();
        
        worldDirToLocal.set(invOrientMat3);
        worldToLocal.set(Matrix4f.translate(pos.cnegate()));
        worldToLocal.mult(invOrientMat4);
    }
    
    public Vector3f getForce()
    {
        return new Vector3f();
    }
    
    public Vector3f getTorque()
    {
        return new Vector3f();
    }
    
    private Matrix3f rotatedInvMOI()
    {
        return localDirToWorld.clone().mult(moi.inv).mult(worldDirToLocal);
    }
    
    public void step(float dt)
    {
        float hdt = dt*.5f;
        
        Vector3f acc0 = getForce().div(mass); //Initial acceleration
        Vector3f angAcc0 = getTorque().mult(rotatedInvMOI());
        
        Vector3f velh = acc0.cmult(hdt).add(vel); //Half-step velocity
        Vector3f angVelh = angAcc0.cmult(hdt).add(angVel);
        
        pos.add(velh.cmult(dt)); //Final position
        orient.mult(Quat4f.axisAngle(angVelh.cmult(dt)));
        
        vel.add(acc0.cmult(dt)); //Approximate final velocity
        angVel.add(angAcc0.cmult(dt));
        
        updateMatrices();
        
        Vector3f acc1 = getForce().div(mass); //Final acceleration
        Vector3f angAcc1 = getTorque().mult(rotatedInvMOI());
        
        vel.set(acc1).mult(hdt).add(velh); //Final velocity
        angVel.set(angAcc1).mult(hdt).add(angVelh);
        
        updateMatrices();
    }
    
    public void applyImpulse(Vector3f impulse)
    {
        vel.add(impulse.cdiv(mass));
    }
    
    public void applyImpulse(Vector3f impulse, Vector3f localPosition)
    {
        applyImpulse(impulse);
        applyAngularImpulse(impulse.clone().cross(localPosition));
    }
    
    public void applyAngularImpulse(Vector3f angImp)
    {
        angVel.add(angImp.cmult(moi.inv));
    }
}
