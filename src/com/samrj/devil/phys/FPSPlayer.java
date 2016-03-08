package com.samrj.devil.phys;

import com.samrj.devil.game.Keyboard;
import com.samrj.devil.geo3d.Geometry;
import com.samrj.devil.graphics.Camera3D;
import com.samrj.devil.graphics.Camera3DController;
import com.samrj.devil.math.Util;
import com.samrj.devil.math.Vec3;
import com.samrj.devil.phys.FPSPlayer.Settings;
import org.lwjgl.glfw.GLFW;

/**
 * First person shooter player with collision and movement.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2016 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class FPSPlayer extends ActorPhys<Settings>
{
    protected final Keyboard keyboard;
    protected final Camera3D camera;
    protected final Camera3DController camControl;
    
    public Runnable stepCallback;
    private float stepAccum;
    
    /**
     * Creates a new FPSPlayer using the given parameters, keyboard, camera, and
     * level.
     * 
     * @param settings The FPSPlayer parameters to use.
     * @param keyboard A keyboard.
     * @param camera An FPSCamera.
     * @param geom The geometry to collide with.
     */
    public FPSPlayer(Settings settings, Keyboard keyboard, Camera3D camera, Geometry geom)
    {
        super(settings, geom);
        if (settings == null || keyboard == null || camera == null || geom == null)
            throw new NullPointerException();
        
        settings.calcValues();
        this.keyboard = keyboard;
        shape.radii.set(settings.width, settings.height, settings.width).mult(0.5f);
        this.camera = camera;
        camControl = new Camera3DController(camera, settings.sensitivity);
    }
    
    /**
     * Creates a new FPSPlayer using the default parameters and the given
     * keyboard, camera, and level.
     * 
     * @param keyboard A keyboard.
     * @param camera An FPSCamera.
     * @param geom The geometry to collide with.
     */
    public FPSPlayer(Keyboard keyboard, Camera3D camera, Geometry geom)
    {
        this(new Settings(), keyboard, camera, geom);
    }
    
    /**
     * Call to move the player's view around. You may alternatively call
     * camera.onMouseMoved().
     */
    public void onMouseMoved(float x, float y, float dx, float dy)
    {
        camControl.onMouseMoved(x, y, dx, dy);
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
     * Steps the player's simulation forward by the given time-step.
     * 
     * @param dt The time to step forward by.
     */
    @Override
    public void step(float dt)
    {
        if (noclip) //Noclipping
        {
            vel.set();
            if (keyboard.isKeyDown(GLFW.GLFW_KEY_W)) vel.add(camera.forward);
            if (keyboard.isKeyDown(GLFW.GLFW_KEY_S)) vel.sub(camera.forward);
            if (keyboard.isKeyDown(GLFW.GLFW_KEY_D)) vel.add(camera.right);
            if (keyboard.isKeyDown(GLFW.GLFW_KEY_A)) vel.sub(camera.right);
            if (keyboard.isKeyDown(GLFW.GLFW_KEY_SPACE)) vel.y += 1.0f;
            if (keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL)) vel.y -= 1.0f;
            noclipTurbo = keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT);
        }
        else //Walking/falling
        {
            float sin = (float)Math.sin(camControl.yaw);
            float cos = (float)Math.cos(camControl.yaw);

            Vec3 flatForward = new Vec3(-sin, 0.0f, -cos);
            Vec3 flatRight   = new Vec3(cos, 0.0f, -sin);

            moveDir.set();
            if (keyboard.isKeyDown(GLFW.GLFW_KEY_S)) moveDir.sub(flatForward);
            if (keyboard.isKeyDown(GLFW.GLFW_KEY_W)) moveDir.add(flatForward);
            if (keyboard.isKeyDown(GLFW.GLFW_KEY_A)) moveDir.sub(flatRight);
            if (keyboard.isKeyDown(GLFW.GLFW_KEY_D)) moveDir.add(flatRight);
        }
        
        super.step(dt);
        
        if (!noclip)
        {
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
        }
        
        camera.pos.set(pos);
        camera.pos.y += settings.cameraOffset;
        camera.update();
    }
    
    /**
     * A container for all of the parameters needed to define a player. Has good
     * defaults for each value.
     */
    public static class Settings extends ActorPhys.Settings
    {
        public float sensitivity = Util.toRadians(1.0f/8.0f);
        public float cameraHeight = 1.640625f;
        public float strideLength = 1.25f;
        
        protected float cameraOffset;
        
        @Override
        public void calcValues()
        {
            groundNormalMinY = (float)Math.cos(groundMaxIncline);
            cameraOffset = cameraHeight - height*0.5f;
        }
    }
}
