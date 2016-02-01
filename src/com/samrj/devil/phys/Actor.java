package com.samrj.devil.phys;

import com.samrj.devil.geo3d.Ellipsoid;
import com.samrj.devil.geo3d.Geo3DUtil;
import com.samrj.devil.geo3d.GeoMesh;
import com.samrj.devil.geo3d.SweepResult;
import com.samrj.devil.math.Util;
import com.samrj.devil.math.Vec3;
import com.samrj.devil.phys.Actor.Settings;

/**
 * Basic class handling collision and movement.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @param <SETTINGS_TYPE> The type of settings this player uses.
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public abstract class Actor<SETTINGS_TYPE extends Settings>
{
    public final Vec3 pos, vel = new Vec3();
    public final Vec3 moveDir = new Vec3();
    public boolean noclip, noclipTurbo;
    
    protected final SETTINGS_TYPE settings;
    protected final Ellipsoid shape = new Ellipsoid();
    protected final GeoMesh level;
    protected final Vec3 displacement = new Vec3();
    protected Vec3 ground;
    protected int groundMaterial;
    protected boolean applyGravity;
    
    public Runnable jumpCallback, landCallback;
    
    /**
     * Creates a new FPSPlayer using the given parameters, keyboard, camera, and
     * level.
     * 
     * @param settings The FPSPlayer parameters to use.
     * @param level The level to collide with.
     */
    public Actor(SETTINGS_TYPE settings, GeoMesh level)
    {
        if (settings == null || level == null) throw new NullPointerException();
        
        settings.calcValues();
        this.settings = settings;
        shape.radii.set(settings.width, settings.height, settings.width).mult(0.5f);
        pos = shape.pos;
        this.level = level;
    }
    
    public abstract void onMouseMoved(float x, float y, float dx, float dy);
    
    
    /**
     * Makes the player jump. May only jump when standing on something.
     */
    public void jump()
    {
        if (ground == null) return;
        vel.y = settings.jumpSpeed;
        ground = null;
        if (jumpCallback != null) jumpCallback.run();
    }
    
    /**
     * @return Whether or not the player is currently on walkable ground.
     */
    public boolean onGround()
    {
        return ground != null;
    }
    
    /**
     * Sets the ground of this player to a virtual, flat surface.
     */
    public void setFlatGround()
    {
        ground = new Vec3(0.0f, 1.0f, 0.0f);
    }
    
    /**
     * @return The material index of the ground last walked on.
     */
    public int groundMaterial()
    {
        return groundMaterial;
    }
    
    public Vec3 getFeetPos()
    {
        Vec3 out = new Vec3(pos);
        out.y -= shape.radii.y;
        return out;
    }
    
    public Vec3 getRadii()
    {
        return new Vec3(shape.radii);
    }
    
    public void setSpeed(float maxSpeed)
    {
        settings.maxSpeed = maxSpeed;
    }
    
    public void setAcceleration(float acceleration)
    {
        settings.acceleration = acceleration;
    }
    
    protected void applyAcc(Vec3 desiredVel, float acc)
    {
        Vec3 dv = Vec3.sub(desiredVel, vel);
        float dvLen = dv.length();
        
        if (dvLen > acc) vel.madd(dv, acc/dvLen);
        else vel.set(desiredVel);
    }
    
    /**
     * Steps the player's simulation forward by the given time-step.
     * 
     * @param dt The time to step forward by.
     */
    public void step(float dt)
    {
        Vec3 avgVel = new Vec3(vel);
        
        if (noclip) //Noclipping
        {
            if (!vel.isZero(0.0f))
            {
                vel.normalize();
                vel.mult(noclipTurbo ? settings.noclipTurboSpeed : settings.noclipSpeed);
            }
        }
        else //Walking/falling
        {
            boolean wantToMove = !moveDir.isZero(0.0f);

            if (ground != null) //Walking
            {
                if (wantToMove)
                {
                    float moveSpeed = moveDir.length();
                    moveDir.y = -ground.dot(moveDir)*moveSpeed/ground.y;
                    if (moveSpeed > settings.maxSpeed) moveDir.normalize();
                    moveDir.mult(settings.maxSpeed);
                }

                //Lock to ground
                Geo3DUtil.restrain(vel, Vec3.negate(ground));
                applyAcc(moveDir, settings.acceleration*dt);
            }
            else //Falling
            {
                if (wantToMove)
                {
                    float moveSpeed = moveDir.length();
                     if (moveSpeed > settings.maxSpeed) moveDir.normalize();
                    moveDir.mult(settings.maxSpeed);
                    moveDir.y = vel.y;
                    applyAcc(moveDir, settings.airAcceleration*dt);
                }
            }
            
            if (applyGravity) vel.y -= settings.gravity*dt;
        }
        
        avgVel.add(vel).mult(0.5f);
        Vec3.mult(avgVel, dt, displacement);
        pos.add(displacement);
        
        applyGravity = true;
        
        if (!noclip)
        {
            boolean startGround = ground != null;
            
            //Find the ground
            if (startGround)
            {
                ground = null;
                float oldY = pos.y;
                pos.y += settings.stepHeight;
                Vec3 step = new Vec3(0.0f, -2.0f*settings.stepHeight, 0.0f);
                SweepResult sweep = level.sweepUnsorted(shape, step)
                        .filter(e -> e.normal.y >= settings.groundNormalMinY)
                        .reduce((a, b) -> a.time < b.time ? a : b)
                        .orElse(null);

                pos.y = oldY;
                if (sweep != null)
                {
                    ground = sweep.normal;
                    Object object = sweep.object;
                    if (object instanceof GeoMesh.Face) groundMaterial = ((GeoMesh.Face)object).material;
                    applyGravity = (sweep.time - 0.5f)*2.0f > settings.groundThreshold;
                }
            }

            //Clip against the level
            level.intersectUnsorted(shape).forEach(isect ->
            {
                pos.add(Vec3.sub(isect.point, isect.surface));
                Geo3DUtil.restrain(vel, isect.normal);
                
                if (isect.normal.y < settings.groundNormalMinY) return;
                if (ground == null || isect.normal.y > ground.y)
                {
                    ground = isect.normal;
                    Object object = isect.object;
                    if (object instanceof GeoMesh.Face) groundMaterial = ((GeoMesh.Face)object).material;
                }
            });
            
            //Check for landing
            if (landCallback != null && !startGround && ground != null)
                landCallback.run();
        }
    }
    
    public static class Settings
    {
        public float gravity = 9.80665f;
        public float width = 1.0f;
        public float height = 1.75f;
        public float stepHeight = 0.25f;
        public float groundMaxIncline = Util.toRadians(46.0f);
        public float groundThreshold = 1.0f/64.0f;
        public float maxSpeed = 3.0f;
        public float acceleration = 16.0f;
        public float airAcceleration = 4.0f;
        public float jumpSpeed = 4.0f;
        public float noclipSpeed = 4.0f;
        public float noclipTurboSpeed = 12.0f;
        
        protected float groundNormalMinY;
        
        protected void calcValues()
        {
            groundNormalMinY = (float)Math.cos(groundMaxIncline);
        }
    }
}
