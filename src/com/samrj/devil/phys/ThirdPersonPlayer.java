package com.samrj.devil.phys;

import com.samrj.devil.game.Keyboard;
import com.samrj.devil.geo3d.GeoMesh;
import com.samrj.devil.math.Vec3;
import com.samrj.devil.phys.Actor.Settings;
import com.samrj.devil.util.ThirdPersonCamera;
import org.lwjgl.glfw.GLFW;
import java.util.function.Consumer;

/**
 * Third person player with collision and movement.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2016 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class ThirdPersonPlayer extends Actor<Settings>
{
    protected final Keyboard keyboard;
    protected final ThirdPersonCamera camera;
    public Consumer<Vec3> moveFunction;
    
    /**
     * Creates a new FPSPlayer using the given parameters, keyboard, camera, and
     * level.
     * 
     * @param settings The FPSPlayer parameters to use.
     * @param keyboard A keyboard.
     * @param camera An FPSCamera.
     * @param level The level to collide with.
     */
    public ThirdPersonPlayer(Settings settings, Keyboard keyboard,
            ThirdPersonCamera camera, GeoMesh level)
    {
        super(settings, level);
        if (settings == null || keyboard == null || camera == null || level == null)
            throw new NullPointerException();
        
        settings.calcValues();
        this.keyboard = keyboard;
        shape.radii.set(settings.width, settings.height, settings.width).mult(0.5f);
        this.camera = camera;
    }

    protected float cameraOffset;

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
        else if (moveFunction == null)//Walking/falling
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
        else moveFunction.accept(moveDir);
        
        super.step(dt);
        
        camera.target.set(getFeetPos());
        camera.update();
    }
}
