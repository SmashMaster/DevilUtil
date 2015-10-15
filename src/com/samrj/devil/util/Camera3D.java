package com.samrj.devil.util;

import com.samrj.devil.math.Mat3;
import com.samrj.devil.math.Mat4;
import com.samrj.devil.math.Quat;
import com.samrj.devil.math.Vec2;
import com.samrj.devil.math.Vec3;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Camera3D
{
    public final Vec3 pos;
    public final Quat dir;
    public final float zNear, zFar;
    public final float hSlope, vSlope;
    public final float slopeSq;
    public final Mat4 projMat, viewMat;
    public final Mat4 invViewMat;
    public final Mat3 invDirMat;
    public final Vec3 forward, right, up;
    public final Mat4 matrix;
    
    public Camera3D(float zNear, float zFar, float fov, float aspectRatio)
    {
        pos = new Vec3();
        dir = Quat.identity();
        this.zNear = zNear;
        this.zFar = zFar;
        float tanFov = (float)Math.tan(fov*0.5f);
        
        if (aspectRatio <= 1.0f) //Width is greater or equal to height.
        {
            hSlope = tanFov;
            vSlope = tanFov*aspectRatio;
        }
        else //Widgth is smaller than height.
        {
            hSlope = tanFov/aspectRatio;
            vSlope = tanFov;
        }
        
        slopeSq = hSlope*hSlope + vSlope*vSlope;
        projMat = Mat4.frustum(hSlope*zNear, vSlope*zNear, zNear, zFar);
        viewMat = Mat4.identity();
        invViewMat = Mat4.identity();
        invDirMat = Mat3.identity();
        forward = new Vec3(0.0f, 0.0f, -1.0f);
        right = new Vec3(1.0f, 0.0f, 0.0f);
        up = new Vec3(0.0f, 1.0f, 0.0f);
        matrix = Mat4.identity();
        
        Camera3D.this.update();
    }
    
    public Vec3 angles()
    {
        float pitch = (float)Math.atan2(2.0f*(dir.w*dir.x - dir.y*dir.z), 1.0f - 2.0f*(dir.z*dir.z + dir.x*dir.x));
        float yaw = (float)Math.atan2(2.0f*(dir.w*dir.y - dir.z*dir.x), 1.0f - 2.0f*(dir.y*dir.y + dir.z*dir.z));
        float roll = (float)Math.asin(2.0f*(dir.x*dir.y - dir.w*dir.z));
        
        return new Vec3(pitch, yaw, roll);
    }
    
    public void update()
    {
        viewMat.setRotation(Quat.invert(dir));
        viewMat.translate(Vec3.negate(pos));
        
        invViewMat.setTranslation(pos);
        invViewMat.rotate(dir);
        
        invDirMat.set(invViewMat);
        forward.set(0.0f, 0.0f, -1.0f).mult(invDirMat);
        right.set(1.0f, 0.0f, 0.0f).mult(invDirMat);
        up.set(0.0f, 1.0f, 0.0f).mult(invDirMat);
        
        matrix.set(projMat).mult(viewMat);
    }
    
    public Vec3[] getFrustum(float near, float far)
    {
        float wn = near*hSlope, wf = far*hSlope;
        float hn = near*vSlope, hf = far*vSlope;
        
        Vec3[] array = {
            new Vec3(-wn, -hn, -near),
            new Vec3(-wf, -hf, -far),
            new Vec3(-wn,  hn, -near),
            new Vec3(-wf,  hf, -far),
            new Vec3( wn, -hn, -near),
            new Vec3( wf, -hf, -far),
            new Vec3( wn,  hn, -near),
            new Vec3( wf,  hf, -far),
        };
        
        return array;
    }
    
    public Vec2 getFrustumCircumsphere(float near, float far)
    {
        float z = (near*(slopeSq - 1.0f) + far*(slopeSq + 1.0f))*0.5f;
        float r = (float)Math.sqrt(near*near*slopeSq + z*z);
        
        return new Vec2(-(near + z), r);
    }
}
