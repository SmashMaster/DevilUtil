package com.samrj.devil.util;

import com.samrj.devil.math.Util;
import com.samrj.devil.math.Vec3;

public class FPSCamera extends Camera3D
{
    public final float sensitivity;
    public float pitch, yaw;
    
    public FPSCamera(float zNear, float zFar, float fov, float aspectRatio, float sensitivity)
    {
        super(zNear, zFar, fov, aspectRatio);
        this.sensitivity = sensitivity;
    }
    
    public void setAnglesFromRotation()
    {
        Vec3 angles = angles();
        pitch = angles.x;
        yaw = angles.y;
    }
    
    public void onMouseMoved(float x, float y, float dx, float dy)
    {
        yaw -= sensitivity*dx;
        pitch += sensitivity*dy;
        if (dx != 0.0f || dy != 0.0f)
        {
            yaw = Util.reduceAngle(yaw);
            pitch = Util.clamp(pitch, -Util.PId2, Util.PId2);
            
            dir.setRotation(new Vec3(0, 1, 0), yaw);
            dir.rotate(new Vec3(1, 0, 0), pitch);
        }
    }
}
