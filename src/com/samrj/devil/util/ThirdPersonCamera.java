package com.samrj.devil.util;

import com.samrj.devil.geo3d.Ellipsoid;
import com.samrj.devil.geo3d.GeoMesh;
import com.samrj.devil.geo3d.SweepResult;
import com.samrj.devil.math.Util;
import com.samrj.devil.math.Vec3;

/**
 * Camera class for third person games, where the camera is pointed on a moving
 * character at all times.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class ThirdPersonCamera extends Camera3D
{
    public final Vec3 target = new Vec3();
    public float sensitivity, distance, height, offset;
    public float pitch, yaw;
    private GeoMesh level;
    private Ellipsoid blockShape;
    
    public ThirdPersonCamera(float zNear, float zFar, float fov, float aspectRatio,
            float sensitivity, float distance, float height, float offset)
    {
        super(zNear, zFar, fov, aspectRatio);
        this.sensitivity = sensitivity;
        this.distance = distance;
        this.height = height;
        this.offset = offset;
    }
    
    public void enableLevelBlocking(GeoMesh level, float blockRadius)
    {
        this.level = level;
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
    }
    
    @Override
    public void update()
    {
        Vec3 temp = new Vec3();
        dir.setRotation(temp.set(0, 1, 0), yaw);
        dir.rotate(temp.set(1, 0, 0), pitch);
        
        pos.set(target);
        pos.y += height;
        pos.add(temp.set(1, 0, 0).mult(dir).mult(offset));
        Vec3 dp = new Vec3(0, 0, 1).mult(dir).mult(distance);
        
        if (level != null)
        {
            blockShape.pos.set(pos);
            SweepResult ray = level.sweepFirst(blockShape, dp);
            if (ray != null) pos.madd(dp, ray.time);
            else pos.add(dp);
        }
        else pos.add(dp);
        
        super.update();
    }
}
