package com.samrj.devil.phys;

import com.samrj.devil.math.Mat3;
import com.samrj.devil.math.Mat4;
import com.samrj.devil.math.Quat;
import com.samrj.devil.math.Util;
import com.samrj.devil.math.Vec3;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class RigidBody3D
{
    public final Vec3 pos;
    public final Vec3 vel;
    public float mass;
    
    public final Quat orient;
    public final Vec3 angVel;
    public final MOI moi;
    
    public final Mat4 worldToLocal;
    public final Mat3 worldDirToLocal;
    public final Mat3 localDirToWorld;
    
    public RigidBody3D(float mass, MOI moi)
    {
        pos = new Vec3();
        vel = new Vec3();
        this.mass = mass;
        
        orient = Quat.identity();
        angVel = new Vec3();
        this.moi = new MOI(moi);
        
        worldToLocal = Mat4.identity();
        worldDirToLocal = Mat3.identity();
        localDirToWorld = Mat3.identity();
    }
    
    public void updateMatrices()
    {
        localDirToWorld.setRotation(orient);
        
        Mat3 invOrientMat3 = Mat3.rotation(Quat.invert(orient));
        Mat4 invOrientMat4 = new Mat4(invOrientMat3);
        
        worldDirToLocal.set(invOrientMat3);
        worldToLocal.setTranslation(Vec3.negate(pos));
        worldToLocal.mult(invOrientMat4);
    }
    
    public Vec3 getForce()
    {
        return new Vec3();
    }
    
    public Vec3 getTorque()
    {
        return new Vec3();
    }
    
    private Mat3 rotatedInvMOI()
    {
        return Mat3.mult(localDirToWorld, moi.inv).mult(worldDirToLocal);
    }
    
    public void step(float dt)
    {
        float hdt = dt*.5f;
        
        Vec3 acc0 = getForce().div(mass); //Initial acceleration
        Vec3 angAcc0 = getTorque().mult(rotatedInvMOI());
        
        Vec3 velh = Vec3.mult(acc0, hdt).add(vel); //Half-step velocity
        Vec3 angVelh = Vec3.mult(angAcc0, hdt).add(angVel);
        
        pos.add(Vec3.mult(velh, dt)); //Final position
        Vec3 axis = Vec3.mult(angVelh, dt);
        if (!axis.isZero(0.0f))
        {
            float angle = axis.length();
            orient.mult(Quat.rotation(axis.div(angle), angle));
        }
        
        vel.add(Vec3.mult(acc0, dt)); //Approximate final velocity
        angVel.add(Vec3.mult(angAcc0, dt));
        
        updateMatrices();
        
        Vec3 acc1 = getForce().div(mass); //Final acceleration
        Vec3 angAcc1 = getTorque().mult(rotatedInvMOI());
        
        vel.set(acc1).mult(hdt).add(velh); //Final velocity
        angVel.set(angAcc1).mult(hdt).add(angVelh);
        
        updateMatrices();
    }
    
    public void applyImpulse(Vec3 impulse)
    {
        vel.add(Vec3.div(impulse, mass));
    }
    
    public void applyImpulse(Vec3 impulse, Vec3 localPosition)
    {
        applyImpulse(impulse);
        applyAngularImpulse(Vec3.cross(impulse, localPosition));
    }
    
    public void applyAngularImpulse(Vec3 angImp)
    {
        angVel.add(Vec3.mult(angImp, moi.inv));
    }
    
    public Vec3 getPointVelocity(Vec3 point)
    {
        return Vec3.cross(angVel, point).add(vel);
    }
    
    public Vec3 getLocalPointVelocity(Vec3 localPoint)
    {
        return getPointVelocity(Vec3.mult(localPoint, localDirToWorld));
    }
}
