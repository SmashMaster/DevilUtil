package com.samrj.devil.phys;

import com.samrj.devil.math.Mat3;
import com.samrj.devil.math.Mat4;
import com.samrj.devil.math.Quat;
import com.samrj.devil.math.Vec3;
import java.util.function.Function;

/**
 * Simple rigid body class. Handles force and torque integration, doesn't handle
 * collisions, gravity, etc.
 * 
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
    
    private Function<RigidBody3D, Vec3> forceFunc, torqueFunc;
    
    /**
     * Creates a new rigid body with the given properties. The moment of inertia
     * is not multiplied by the mass, you must pre-multiply it.
     * 
     * @param mass The mass of this rigid body.
     * @param moi The moment of inertia tensor of this rigid body.
     */
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
    
    /**
     * Updates the matrices of this rigid body. Call when externally modifying
     * the position or orientation of this body. Does not need to called when
     * externally modifying velocity or mass.
     */
    public void updateMatrices()
    {
        localDirToWorld.setRotation(orient);
        
        Mat3 invOrientMat3 = Mat3.rotation(Quat.invert(orient));
        Mat4 invOrientMat4 = new Mat4(invOrientMat3);
        
        worldDirToLocal.set(invOrientMat3);
        worldToLocal.setTranslation(Vec3.negate(pos));
        worldToLocal.mult(invOrientMat4);
    }
    
    /**
     * Sets the function to use for determining the current net force on this
     * rigid body.
     * 
     * @param func A function that takes this body as a parameter, and returns
     *        the net force on this body.
     */
    public void setForceFunction(Function<RigidBody3D, Vec3> func)
    {
        forceFunc = func;
    }
    
    /**
     * Sets the function to use for determining the current net torque on this
     * rigid body.
     * 
     * @param func A function that takes this body as a parameter, and returns
     *        the net torque on this body.
     */
    public void setTorqueFunction(Function<RigidBody3D, Vec3> func)
    {
        torqueFunc = func;
    }
    
    private Vec3 force()
    {
        if (forceFunc != null) return forceFunc.apply(this);
        return new Vec3();
    }
    
    private Vec3 torque()
    {
        if (torqueFunc != null) return torqueFunc.apply(this);
        return new Vec3();
    }
    
    private Mat3 rotatedInvMOI()
    {
        return Mat3.mult(localDirToWorld, moi.inv).mult(worldDirToLocal);
    }
    
    /**
     * Calculates the acceleration on this body and integrates it over the given
     * time. Integration will be symplectic as long as the force is not
     * dependant on the velocity (as with air resistance). Uses a modified form
     * of Verlet integration.
     * 
     * The properties of this integration lend themselves well to cylic systems
     * such as orbits and springs.
     * 
     * @param dt The time step to integrate over.
     */
    public void step(float dt)
    {
        float hdt = dt*.5f;
        
        Vec3 acc0 = force().div(mass); //Initial acceleration
        Vec3 angAcc0 = torque().mult(rotatedInvMOI());
        
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
        
        Vec3 acc1 = force().div(mass); //Final acceleration
        Vec3 angAcc1 = torque().mult(rotatedInvMOI());
        
        vel.set(acc1).mult(hdt).add(velh); //Final velocity
        angVel.set(angAcc1).mult(hdt).add(angVelh);
        
        updateMatrices();
    }
    
    /**
     * Applies a one-time linear impulse to the center of mass of this body.
     * 
     * @param impulse The impulse to apply.
     */
    public void applyImpulse(Vec3 impulse)
    {
        vel.add(Vec3.div(impulse, mass));
    }
    
    /**
     * Applies a one-time impulse to the given position on this body, in local
     * coordinates.
     * 
     * @param impulse The impulse to apply.
     * @param localPosition The local position to apply the impulse to.
     */
    public void applyImpulse(Vec3 impulse, Vec3 localPosition)
    {
        applyImpulse(impulse);
        applyAngularImpulse(Vec3.cross(impulse, localPosition));
    }
    
    /**
     * Applies a one-time angular impulse to this body.
     * 
     * @param angImp The angular impulse to apply.
     */
    public void applyAngularImpulse(Vec3 angImp)
    {
        angVel.add(Vec3.mult(angImp, moi.inv));
    }
    
    /**
     * Returns the velocity of this body at the given point. The point is in
     * world space, but is relative to the position of this body.
     * 
     * @param point The point to find the velocity at.
     * @return The velocity at the given point.
     */
    public Vec3 getPointVelocity(Vec3 point)
    {
        return Vec3.cross(angVel, point).add(vel);
    }
    
    /**
     * Returns the velocity of this body at the given local point.
     * 
     * @param localPoint The local point to find the velocity at.
     * @return The velocity at the given point.
     */
    public Vec3 getLocalPointVelocity(Vec3 localPoint)
    {
        return getPointVelocity(Vec3.mult(localPoint, localDirToWorld));
    }
}
