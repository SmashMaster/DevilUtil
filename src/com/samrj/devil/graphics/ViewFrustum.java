package com.samrj.devil.graphics;

import com.samrj.devil.math.Mat4;
import com.samrj.devil.math.Vec2;
import com.samrj.devil.math.Vec2i;
import com.samrj.devil.math.Vec3;

/**
 * 3D projection matrix class.
 *
 * @author Samuel Johnson (SmashMaster)
 */
public class ViewFrustum
{
    /**
     * Returns an array of eight vectors, each one a vertex of the given frustum. Returned in local space.
     *
     * @param near The near plane of the frustum.
     * @param far The far plane of the frustum.
     * @return The corners of the given frustum.
     */
    public static Vec3[] getCorners(float near, float far, float hSlope, float vSlope)
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

    /**
     * There is a unique sphere whose surface touches each vertex of the given frustum. This method returns the distance
     * to and radius of that sphere, in the first and second components of a 2D vector.
     *
     * @param near The near plane to use.
     * @param far The far plane to use.
     * @return The circumsphere of the given frustum.
     */
    public static Vec2 getCircumsphere(float near, float far, float hSlope, float vSlope)
    {
        float slopeSq = hSlope*hSlope + vSlope*vSlope;
        float z = (near*(slopeSq - 1.0f) + far*(slopeSq + 1.0f))*0.5f;
        float r = (float)Math.sqrt(near*near*slopeSq + z*z);

        return new Vec2(-(near + z), r);
    }

    public final float zNear, zFar;
    public final Mat4 projMat = new Mat4();

    private float fov;
    private float hSlope, vSlope;

    public ViewFrustum(float zNear, float zFar, float fov, float aspectRatio)
    {
        this.zNear = zNear;
        this.zFar = zFar;

        setFOV(aspectRatio, fov);
    }

    public ViewFrustum(float zNear, float zFar, float fov, Vec2i resolution)
    {
        this(zNear, zFar, fov, resolution.y/(float)resolution.x);
    }

    /**
     * Sets the field of view of this camera in radians. If the camera frustum
     * is wider than it is tall, this sets the horizontal FOV. Otherwise, the
     * vertical FOV is set.
     */
    public void setFOV(float aspectRatio, float fov)
    {
        this.fov = fov;
        float tanFov = (float)Math.tan(fov*0.5f);

        if (aspectRatio <= 1.0f) //Width is greater or equal to height.
        {
            hSlope = tanFov;
            vSlope = tanFov*aspectRatio;
        }
        else //Widgth is less than height (unusual.)
        {
            hSlope = tanFov/aspectRatio;
            vSlope = tanFov;
        }

        if (Float.isInfinite(zFar))
        {
            //Infinite projection matrix from http://www.terathon.com/gdc07_lengyel.pdf
            float epsilon = (float)Math.pow(2.0, -22.0);
            projMat.setZero();
            projMat.a = 1.0f/hSlope;
            projMat.f = 1.0f/vSlope;
            projMat.k = epsilon - 1.0f;
            projMat.l = (epsilon - 2.0f)*zNear;
            projMat.o = -1.0f;
        }
        else Mat4.frustum(hSlope*zNear, vSlope*zNear, zNear, zFar, projMat);
    }

    public void setFOV(int width, int height, float fov)
    {
        setFOV(height/(float)width, fov);
    }

    public float getFOV()
    {
        return fov;
    }

    public float getHSlope()
    {
        return hSlope;
    }

    public float getVSlope()
    {
        return vSlope;
    }

    /**
     * Returns whether this camera frustum's width is smaller than its height.
     */
    public boolean isSkinny()
    {
        return hSlope < vSlope;
    }

    /**
     * Returns an array of eight vectors, each one a vertex of this camera's
     * frustum. Returned in local space, with actual.
     *
     * @return The frustum of this camera.
     */
    public Vec3[] getCorners()
    {
        return getCorners(zNear, zFar, hSlope, vSlope);
    }

    /**
     * There is a unique sphere whose surface touches each vertex of this
     * camera's frustum. This method returns the distance to and radius of that
     * sphere, in the first and second components of a 2D vector.
     *
     * @param near The near plane to use.
     * @param far The far plane to use.
     * @return The circumsphere of this camera.
     */
    public Vec2 getCircumsphere(float near, float far)
    {
        return getCircumsphere(near, far, hSlope, vSlope);
    }

    /**
     * There is a unique sphere whose surface touches each vertex of this
     * camera's frustum. This method returns the distance to and radius of that
     * sphere, in the first and second components of a 2D vector.
     *
     * @return The circumsphere of this camera.
     */
    public Vec2 getCircumsphere()
    {
        return getCircumsphere(zNear, zFar);
    }
}
