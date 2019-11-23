package com.samrj.devil.geo3d;

import com.samrj.devil.math.Util;
import com.samrj.devil.math.Vec3;
import java.util.function.Consumer;

/**
 * Basic class handling collision and movement of a character in a 3D space.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2019 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class ActorDriver
{
    public static final GeoPrimitive VIRTUAL_GROUND = () -> GeoPrimitive.Type.VIRTUAL;
    
    public final Vec3 pos, vel = new Vec3();
    
    /**
     * The current desired movement direction for this driver.
     */
    public final Vec3 moveDir = new Vec3();
    
    //The geometry that this driver will use for collision detection. Defaults
    //to null, with collision disabled.
    public Geometry<?, ?, ?> geom;
    
    //The downward acceleration that this driver will experience at all times.
    public float gravity = 9.80665f;
    
    //The distance this driver can climb over steep surfaces like stairs.
    public float climbHeight = 0.25f;
    
    //The maximum y-component of the normal vector for walkable ground.
    //Determines the steepness of hills this driver can walk up. Use
    //setMaxGroundIncline() to set this as an angle.
    public float groundNormalMinY = (float)Math.cos(Util.toRadians(46.0f));
    
    //An epsilon value for determining how close this driver needs to be to the
    //ground to be considered touching the ground. Is a fraction of the step
    //height.
    public float groundThreshold = 1.0f/64.0f;
    
    //An exponential rate of decay determining how quickly this driver will
    //move downward towards the ground when not perfectly touching it.
    public float groundFloatDecay = 32.0f;
    
    //An exponential rate of decay determining how quickly this driver will be
    //nudged out of geometry it is interescting with.
    public float intersectionDecay = 128.0f;
    
    //The maximum speed at which this driver can move.
    public float maxSpeed = 3.0f;
    
    //The rate of acceleration for this driver while on the ground.
    public float acceleration = 16.0f;
    
    //The rate of acceleration for this driver while falling. Set to zero for
    //'realistic' lack of air control.
    public float airAcceleration = 4.0f;
    
    //The vertical speed this driver will have upon jumping.
    public float jumpSpeed = 4.0f;
    
    //Settable callback functions for jumping, falling, and landing.
    public Runnable jumpCallback, fallCallback;
    public Consumer<Float> landCallback;
    
    private final Ellipsoid shape = new Ellipsoid();
    private final Vec3 displacement = new Vec3();
    private final Vec3 groundNormal = new Vec3(0.0f, 1.0f, 0.0f);
    private GeoPrimitive groundObject;
    private boolean applyGravity;
    
    /**
     * Creates a new default physics actor.
     */
    public ActorDriver()
    {
        pos = shape.pos;
        shape.radii.set(0.5f, 0.875f, 0.5f);
    }
    
    private boolean isValidGround(Vec3 normal)
    {
        return normal.y >= groundNormalMinY;
    }
    
    private void applyAcc(Vec3 desiredVel, float acc)
    {
        if (acc == 0.0f) return;
        
        Vec3 dv = Vec3.sub(desiredVel, vel);
        float dvLen = dv.length();
        
        if (dvLen > acc) vel.madd(dv, acc/dvLen);
        else vel.set(desiredVel);
    }
    
    /**
     * Sets the maximum angle of surfaces this driver can walk up.
     * 
     * @param angle Any positive angle.
     */
    public void setMaxGroundIncline(float angle)
    {
        if (angle <= 0.0f) throw new IllegalArgumentException();
        groundNormalMinY = (float)Math.cos(angle);
    }
    
    /**
     * Sets the horizontal and vertical size of this driver's collision volume.
     */
    public void setRadii(float horizontal, float vertical)
    {
        if (horizontal <= 0.0f || vertical <= 0.0f)
            throw new IllegalArgumentException();
        shape.radii.set(horizontal, vertical, horizontal);
    }
    
    /**
     * @return Whether or not the player is currently on walkable ground.
     */
    public boolean onGround()
    {
        return groundObject != null;
    }
    
    /**
     * Makes the actor jump. May only jump when standing on something.
     */
    public void jump()
    {
        if (!onGround()) return;
        vel.y = jumpSpeed;
        groundObject = null;
        if (jumpCallback != null) jumpCallback.run();
    }
    
    /**
     * Sets the ground of this player to a virtual, flat surface.
     */
    public void setFlatGround()
    {
        groundObject = VIRTUAL_GROUND;
        groundNormal.set(0.0f, 1.0f, 0.0f);
    }
    
    /**
     * Returns whatever geometry object this actor is currently standing on.
     */
    public GeoPrimitive getGroundObject()
    {
        return groundObject;
    }
    
    /**
     * Returns a new vector containing the current ground normal, or null if not
     * on ground.
     */
    public Vec3 getGroundNormal()
    {
        return onGround() ? new Vec3(groundNormal) : null;
    }
    
    /**
     * Returns the position of this driver's feet.
     */
    public Vec3 getFeetPos()
    {
        Vec3 out = new Vec3(pos);
        out.y -= shape.radii.y;
        return out;
    }
    
//    private void getNormal(GeomObject obj, Vec3 result)
//    {
//        obj.getFaces()
//                .map(Face::getNormal)
//                .peek(n -> {if (n.y < 0.0f) n.negate();})
//                .reduce((a, b) -> a.y > b.y ? a : b)
//                .filter(n -> n.y > result.y)
//                .ifPresent(result::set);
//    }
    
    /**
     * Steps the player's simulation forward by the given time-step.
     * 
     * @param dt The time to step forward by.
     */
    public void step(float dt)
    {
        boolean startOnGround = onGround();
        Vec3 avgVel = new Vec3(vel);
        float vertVel = vel.y;
        
        boolean wantToMove = !moveDir.isZero(0.0f);
        Vec3 adjMoveDir = new Vec3(moveDir);

        if (startOnGround) //Walking
        {
            if (wantToMove)
            {
                adjMoveDir.y = -groundNormal.dot(adjMoveDir)*adjMoveDir.length()/groundNormal.y;
                float moveSpeed = adjMoveDir.length();
                if (moveSpeed > 1.0f) adjMoveDir.div(moveSpeed);
                adjMoveDir.mult(maxSpeed);
            }

            //Lock to ground
            applyAcc(adjMoveDir, acceleration*dt);
        }
        else //Falling
        {
            if (wantToMove)
            {
                float moveSpeed = adjMoveDir.length();
                if (moveSpeed > 1.0f) adjMoveDir.div(moveSpeed);
                adjMoveDir.mult(maxSpeed);
                adjMoveDir.y = vel.y;
                applyAcc(adjMoveDir, airAcceleration*dt);
            }
        }

        if (applyGravity) vel.y -= gravity*dt;
        
        //Integrate
        avgVel.add(vel).mult(0.5f);
        Vec3.mult(avgVel, dt, displacement);
        pos.add(displacement);
        
        applyGravity = true;
        groundObject = null;
        
        //Find the ground
        if (geom != null && startOnGround)
        {
            float oldY = pos.y;
            pos.y += climbHeight;
            
            Vec3 step = new Vec3(0.0f, -2.0f*climbHeight, 0.0f);
            SweepResult sweep = geom.sweepUnsorted(shape, step)
                    .filter(e -> isValidGround(e.normal))
                    .reduce((a, b) -> a.time < b.time ? a : b)
                    .orElse(null);

            pos.y = oldY;
            
            if (sweep != null)
            {
                float groundDist = (sweep.time*2.0f - 1.0f)*climbHeight;
                pos.y -= groundDist*(1.0f - (float)Math.pow(0.5f, dt*groundFloatDecay));
                applyGravity = (sweep.time - 0.5f)*2.0f > groundThreshold;
                
                groundObject = sweep.object;
                groundNormal.set(sweep.normal);
            }
        }
        
        //Clip against the level
        if (geom != null)
        {
            Vec3 nudge = new Vec3();
            
            geom.intersectUnsorted(shape).forEach(isect ->
            {
                nudge.add(Vec3.sub(isect.point, isect.surface));

                if (isValidGround(isect.normal) && (!onGround() || isect.normal.y > groundNormal.y))
                {
                    groundObject = isect.object;
                    groundNormal.set(isect.normal);
                }
            });
            
            pos.madd(nudge, 1.0f - (float)Math.pow(0.5f, dt*intersectionDecay));
        }
        
        boolean endOnGround = onGround();
        if (endOnGround) vel.y = Geo3DUtil.restrain(vel, groundNormal).y;

        //Check for landing
        if (landCallback != null && !startOnGround && endOnGround)
            landCallback.accept(vel.y - vertVel);

        //Check for falling
        if (fallCallback != null && startOnGround && !endOnGround)
            fallCallback.run();
    }
}
