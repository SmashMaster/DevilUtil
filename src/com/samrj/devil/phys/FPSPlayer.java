package com.samrj.devil.phys;

import com.samrj.devil.game.Keyboard;
import com.samrj.devil.geo3d.GeoMesh;
import com.samrj.devil.math.Vec3;
import com.samrj.devil.phys.FPSPlayer.Settings;
import com.samrj.devil.util.FPSCamera;
import org.lwjgl.glfw.GLFW;

/**
 * First person shooter player with collision and movement.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class FPSPlayer extends Actor<Settings>
{
    protected final Keyboard keyboard;
    protected final FPSCamera camera;
    
    public Runnable stepCallback;
    private float stepAccum;
    
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
        super(settings, level);
        if (settings == null || keyboard == null || camera == null || level == null)
            throw new NullPointerException();
        
        settings.calcValues();
        this.keyboard = keyboard;
        shape.radii.set(settings.width, settings.height, settings.width).mult(0.5f);
        this.camera = camera;
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
    @Override
    public void onMouseMoved(float x, float y, float dx, float dy)
    {
        camera.onMouseMoved(x, y, dx, dy);
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
            float sin = (float)Math.sin(camera.yaw);
            float cos = (float)Math.cos(camera.yaw);

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
    public static class Settings extends Actor.Settings
    {
        public float cameraHeight = 1.640625f;
        public float strideLength = 1.25f;
        
        protected float cameraOffset;
        
        @Override
        protected void calcValues()
        {
            groundNormalMinY = (float)Math.cos(groundMaxIncline);
            cameraOffset = cameraHeight - height*0.5f;
        }
    }
}
