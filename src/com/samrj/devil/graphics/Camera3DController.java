package com.samrj.devil.graphics;

import com.samrj.devil.geo3d.Ellipsoid;
import com.samrj.devil.geo3d.Geometry;
import com.samrj.devil.geo3d.SweepResult;
import com.samrj.devil.math.Util;
import com.samrj.devil.math.Vec3;

/**
 * Allows for input and control of 3D cameras, for third-person and first-person
 * views.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Camera3DController
{
    public final Camera3D camera;
    public final Vec3 target = new Vec3();
    public final Vec3 offset = new Vec3();
    public float sensitivity, distance, height;
    public float pitch, yaw;
    private Geometry geom;
    private Ellipsoid blockShape;
    
    public Camera3DController(Camera3D camera, float sensitivity, float distance, float height, Vec3 offset)
    {
        this.camera = camera;
        this.sensitivity = sensitivity;
        this.distance = distance;
        this.height = height;
        this.offset.set(offset);
    }
    
    public Camera3DController(Camera3D camera, float sensitivity)
    {
        this.camera = camera;
        this.sensitivity = sensitivity;
    }
    
    public void setAnglesFromRotation()
    {
        Vec3 angles = camera.angles();
        pitch = angles.x;
        yaw = angles.y;
    }
    
    public void enableBlocking(Geometry geom, float blockRadius)
    {
        this.geom = geom;
        blockShape = new Ellipsoid();
        blockShape.radii.set(blockRadius);
    }
    
    public void onMouseMoved(float x, float y, float dx, float dy)
    {
        yaw -= sensitivity*dx;
        pitch += sensitivity*dy;
        if (dx != 0.0f || dy != 0.0f)
        {
            yaw = Util.reduceAngle(yaw);
            pitch = Util.clamp(pitch, -Util.PId2, Util.PId2);
        }
        
        update();
    }
    
    public void update()
    {
        Vec3 temp = new Vec3();
        camera.dir.setRotation(temp.set(0, 1, 0), yaw);
        camera.dir.rotate(temp.set(1, 0, 0), pitch);
        
        camera.pos.set(target);
        camera.pos.y += height;
        Vec3 dp = new Vec3(0, 0, distance).mult(camera.dir);
        dp.add(temp.set(offset).mult(camera.dir));
        if (geom != null && !dp.isZero(0.0f))
        {
            blockShape.pos.set(camera.pos);
            SweepResult ray = geom.sweepFirst(blockShape, dp);
            if (ray != null) camera.pos.madd(dp, ray.time);
            else camera.pos.add(dp);
        }
        else camera.pos.add(dp);
        
        camera.update();
    }
}
