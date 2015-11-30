package com.samrj.devil.phys;

import com.samrj.devil.game.Keyboard;
import com.samrj.devil.geo3d.Ellipsoid;
import com.samrj.devil.geo3d.Geo3DUtil;
import com.samrj.devil.geo3d.GeoMesh;
import com.samrj.devil.geo3d.GeoMesh.Face;
import com.samrj.devil.geo3d.SweepResult;
import com.samrj.devil.math.Util;
import com.samrj.devil.math.Vec3;
import com.samrj.devil.util.FPSCamera;
import org.lwjgl.glfw.GLFW;

/**
 * First person shooter player with collision and movement.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class FPSPlayer
{
    public final Vec3 pos, vel = new Vec3();
    public boolean noclip;
    
    private final Settings settings;
    private final Keyboard keyboard;
    private final Ellipsoid shape;
    private final FPSCamera camera;
    private final GeoMesh level;
    private Vec3 ground;
    private int groundMaterial;
    private boolean applyGravity;
    private float stepAccum;
    
    public Runnable stepCallback, jumpCallback, landCallback;
    
    /**
     * Creates a new FPSPlayer using the given parameters, keyboard, camera, and
     * level.
     * 
     * @param settings The FPSPlayer parameters to use.
     * @param keyboard A keyboard.
     * @param camera An FPSCamera.
     * @param level The level to collide with.
     */
    public FPSPlayer(Settings settings, Keyboard keyboard, FPSCamera camera, GeoMesh level)
    {
        if (settings == null || keyboard == null || camera == null || level == null)
            throw new NullPointerException();
        
        settings.calcValues();
        this.settings = settings;
        this.keyboard = keyboard;
        shape = new Ellipsoid();
        shape.radii.set(settings.width, settings.height, settings.width).mult(0.5f);
        pos = shape.pos;
        this.camera = camera;
        this.level = level;
    }
    
    /**
     * Creates a new FPSPlayer using the default parameters and the given
     * keyboard, camera, and level.
     * 
     * @param keyboard A keyboard.
     * @param camera An FPSCamera.
     * @param level The level to collide with.
     */
    public FPSPlayer(Keyboard keyboard, FPSCamera camera, GeoMesh level)
    {
        this(new Settings(), keyboard, camera, level);
    }
    
    /**
     * Call to move the player's view around. You may alternatively call
     * camera.onMouseMoved().
     */
    public void onMouseMoved(float x, float y, float dx, float dy)
    {
        camera.onMouseMoved(x, y, dx, dy);
    }
    
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
     * Sets the given vector to this player's camera position.
     * 
     * @param result The vector in which to store the camera position.
     */
    public void getCameraPos(Vec3 result)
    {
        Vec3.copy(pos, result);
        result.y += settings.cameraOffset;
    }
    
    /**
     * @return The material index of the ground last walked on.
     */
    public int groundMaterial()
    {
        return groundMaterial;
    }
    
    private void applyAcc(Vec3 desiredVel, float acc)
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
            vel.set();
            if (keyboard.isKeyDown(GLFW.GLFW_KEY_W)) vel.add(camera.forward);
            if (keyboard.isKeyDown(GLFW.GLFW_KEY_S)) vel.sub(camera.forward);
            if (keyboard.isKeyDown(GLFW.GLFW_KEY_D)) vel.add(camera.right);
            if (keyboard.isKeyDown(GLFW.GLFW_KEY_A)) vel.sub(camera.right);
            if (keyboard.isKeyDown(GLFW.GLFW_KEY_SPACE)) vel.y += 1.0f;
            if (keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL)) vel.y -= 1.0f;

            if (!vel.isZero(0.0f))
            {
                vel.normalize();
                boolean turbo = keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT);
                vel.mult(turbo ? settings.noclipTurboSpeed : settings.noclipSpeed);
            }
        }
        else //Walking/falling
        {
            float sin = (float)Math.sin(camera.yaw);
            float cos = (float)Math.cos(camera.yaw);

            Vec3 flatForward = new Vec3(-sin, 0.0f, -cos);
            Vec3 flatRight   = new Vec3(cos, 0.0f, -sin);

            Vec3 moveDir = new Vec3();
            if (keyboard.isKeyDown(GLFW.GLFW_KEY_S)) moveDir.sub(flatForward);
            if (keyboard.isKeyDown(GLFW.GLFW_KEY_W)) moveDir.add(flatForward);
            if (keyboard.isKeyDown(GLFW.GLFW_KEY_A)) moveDir.sub(flatRight);
            if (keyboard.isKeyDown(GLFW.GLFW_KEY_D)) moveDir.add(flatRight);
            boolean wantToMove = !moveDir.isZero(0.0f);

            if (ground != null) //Walking
            {
                if (wantToMove)
                {
                    moveDir.y = -ground.dot(moveDir)/ground.y;
                    moveDir.normalize().mult(settings.maxSpeed);
                }

                //Lock to ground
                Geo3DUtil.restrain(vel, Vec3.negate(ground));
                applyAcc(moveDir, settings.acceleration*dt);
            }
            else //Falling
            {
                if (wantToMove)
                {
                    moveDir.normalize().mult(settings.maxSpeed);
                    moveDir.y = vel.y;
                    applyAcc(moveDir, settings.airAcceleration*dt);
                }
            }
            
            if (applyGravity) vel.y -= settings.gravity*dt;
        }
        
        avgVel.add(vel).mult(0.5f);
        Vec3 displacement = Vec3.mult(avgVel, dt);
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
                    if (object instanceof Face) groundMaterial = ((Face)object).material;
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
                    if (object instanceof Face) groundMaterial = ((Face)object).material;
                }
            });
            
            //Check for footsteps
            if (stepCallback != null && ground != null)
            {
                float dispLen = displacement.length();
                stepAccum += dispLen;

                if (stepAccum > settings.strideLength)
                {
                    stepAccum -= settings.strideLength;
                    stepCallback.run();
                }
            }
            else stepAccum = 0.0f;
            
            //Check for landing
            if (landCallback != null && !startGround && ground != null)
                landCallback.run();
        }
        
        camera.pos.set(pos);
        camera.pos.y += settings.cameraOffset;
        camera.update();
    }
    
    /**
     * A container for all of the parameters needed to define a player. Has good
     * defaults for each value.
     */
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
        public float cameraHeight = 1.640625f;
        public float strideLength = 1.25f;
        
        private float groundNormalMinY;
        private float cameraOffset;
        
        private void calcValues()
        {
            groundNormalMinY = (float)Math.cos(groundMaxIncline);
            cameraOffset = cameraHeight - height*0.5f;
        }
    }
}
