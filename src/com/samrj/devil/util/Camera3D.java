package com.samrj.devil.util;

import com.samrj.devil.graphics.GraphicsUtil;
import com.samrj.devil.math.*;
import org.lwjgl.opengl.GL11;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Camera3D
{
    public static void glLoadIdentity()
    {
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();
    }
    
    public final Vec3 pos;
    public final Quat rot;
    private int resX, resY;
    private Mat4 proj;
    private Vec2i res;
    
    public Camera3D(int resX, int resY, float fov, float near, float far)
    {
        if (fov <= 0f || fov >= Util.PI) throw new IllegalArgumentException();
        if (far <= near || near <= 0f) throw new IllegalArgumentException();
        this.resX = resX;
        this.resY = resY;
        pos = new Vec3();
        rot = new Quat();
        res = new Vec2i(resX, resY);
        
        proj = Mat4.perspective(fov, res.y/(float)res.x, near, far);
    }
    
    public float getAspectRatio()
    {
        return res.y/(float)res.x;
    }
    
    public Mat4 getProj()
    {
        return new Mat4(proj);
    }
    
    public void glLoadProj()
    {
        GraphicsUtil.glLoadMatrix(proj, GL11.GL_PROJECTION);
    }
    
    public void glLoadView()
    {
        GraphicsUtil.glLoadMatrix(getView(), GL11.GL_MODELVIEW);
    }
    
    public void glLoad()
    {
        glLoadProj();
        glLoadView();
    }
    
    public Mat4 getView()
    {
        Mat4 out = Mat4.rotation(Quat.invert(rot));
        out.translate(Vec3.negate(pos));
        return out;
    }
    
    public Mat4 getViewProj()
    {
        return Mat4.mult(proj, getView());
    }
    
    /**
     * Converts the given world position to screen coordinates.
     * 
     * @param pos A world position.
     * @return pos in screen coordinates.
     */
    public Vec3 toScreen(Vec3 pos)
    {
        float midx = resX*.5f;
        float midy = resY*.5f;
        pos = new Vec3(pos);
        pos.mult(getView());
        pos.mult(proj);
        pos.x = pos.x*midx/pos.z + midx;
        pos.y = pos.y*midy/pos.z + midy;
        return pos;
    }
    
    /**
     * Converts the given direction from camera to screen coordinates.
     * 
     * @param dir A world position.
     * @return dir in screen coordinates.
     */
    public Vec3 dirToScreen(Vec3 dir)
    {
        float midx = resX*.5f;
        float midy = resY*.5f;
        dir = new Vec3(dir);
        dir.mult(new Mat3(getView()));
        dir.mult(new Mat3(proj));
        dir.x = dir.x*midx/dir.z + midx;
        dir.y = dir.y*midy/dir.z + midy;
        return dir;
    }
}
