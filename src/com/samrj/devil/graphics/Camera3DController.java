package com.samrj.devil.graphics;

import com.samrj.devil.geo3d.Ellipsoid;
import com.samrj.devil.geo3d.Geometry;
import com.samrj.devil.geo3d.SweepResult;
import com.samrj.devil.math.Util;
import com.samrj.devil.math.Vec2;
import com.samrj.devil.math.Vec3;

/**
 * Allows for input and control of 3D cameras, for third-person and first-person
 * views.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class Camera3DController
{
    public final Vec3 target = new Vec3();
    
    private Camera3D camera;
    
    /**
     * The mouse sensitivity of this camera, in radians per pixel.
     */
    public float sensitivity = Util.toRadians(1.0f/8.0f);
    
    /**
     * The distance to float from the target.
     */
    public float distance;

    /**
     * The vertical offset for the target.
     */
    public float height;
    
    /**
     * The local offset applied to this camera's position.
     */
    public final Vec3 offset = new Vec3();
    
    private float pitch, yaw;
    private Geometry blockGeom;
    private Ellipsoid blockShape;
    
    public Camera3DController(Camera3D camera)
    {
        setCamera(camera);
    }
    
    public void setCamera(Camera3D camera)
    {
        if (camera == null) throw new NullPointerException();
        this.camera = camera;
    }
    
    /**
     * Allows this camera to clip with world geometry, in the case of a third-
     * person game.
     */
    public void enableBlocking(Geometry geom, float radius)
    {
        this.blockGeom = geom;
        blockShape = new Ellipsoid();
        blockShape.radii.set(radius);
    }
    
    /**
     * Disables world geometry clipping for this camera.
     */
    public void disableBlocking()
    {
        blockGeom = null;
        blockShape = null;
    }
    
    /**
     * Returns the pitch of this camera.
     */
    public float getPitch()
    {
        return pitch;
    }
    
    /**
     * Sets the pitch of this camera.
     */
    public void setPitch(float pitch)
    {
        this.pitch = Util.clamp(pitch, -Util.PId2, Util.PId2);
    }
    
    /**
     * Returns the yaw of this camera.
     */
    public float getYaw()
    {
        return yaw;
    }
    
    /**
     * Sets the yaw of this camera.
     */
    public void setYaw(float yaw)
    {
        this.yaw = Util.reduceAngle(yaw);
    }
    
    /**
     * Sets the angles of this camera.
     */
    public void setAngles(float pitch, float yaw)
    {
        this.pitch = Util.clamp(pitch, -Util.PId2, Util.PId2);
        this.yaw = Util.reduceAngle(yaw);
    }
    
    /**
     * Sets the angles of this controller using the rotation of the underlying
     * camera. Note that any camera roll will be lost.
     */
    public void setAnglesFromCamera()
    {
        Vec3 angles = camera.dir.angles();
        setAngles(angles.x, angles.y);
    }
    
    /**
     * Points this camera at the given position.
     */
    public void pointAt(Vec3 pos)
    {
        Vec3 dir = Vec3.sub(pos, target);
        dir.y -= height;
        
        setAngles((float)Math.atan2(dir.y, Math.sqrt(dir.x*dir.x + dir.z*dir.z)),
                  (float)Math.atan2(-dir.x, -dir.z));
    }
    
    /**
     * Applies a change in rotation to this camera as a given pitch and yaw.
     */
    public void deltaAngles(float dPitch, float dYaw)
    {
        setAngles(pitch + dPitch, yaw - dYaw);
    }
    
    /**
     * Applies a change in rotation to this camera as a given pitch and yaw.
     */
    public void deltaAngles(Vec2 delta)
    {
        deltaAngles(delta.x, delta.y);
    }
    
    /**
     * Call to provide this camera with mouse input.
     */
    public void mouseDelta(float dx, float dy)
    {
        deltaAngles(dy*sensitivity, dx*sensitivity);
    }
    
    /**
     * Updates this camera controller and the underlying camera.
     */
    public void update()
    {
        Vec3 temp = new Vec3();
        camera.dir.setRotation(temp.set(0, 1, 0), yaw);
        camera.dir.rotate(temp.set(1, 0, 0), pitch);
        
        camera.pos.set(target);
        camera.pos.y += height;
        Vec3 dp = new Vec3(0, 0, distance).mult(camera.dir);
        dp.add(temp.set(offset).mult(camera.dir));
        if (blockGeom != null && !dp.isZero())
        {
            blockShape.pos.set(camera.pos);
            SweepResult ray = blockGeom.sweepFirst(blockShape, dp).orElse(null);
            if (ray != null) camera.pos.madd(dp, ray.time);
            else camera.pos.add(dp);
        }
        else camera.pos.add(dp);
        
        camera.update();
    }
}
