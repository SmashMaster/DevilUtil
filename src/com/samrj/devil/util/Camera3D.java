package com.samrj.devil.util;

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
    
    public final Vector3f pos;
    public final Quat4f rot;
    private int resX, resY;
    private Matrix4f proj;
    private Vector2i res;
    
    public Camera3D(int resX, int resY, float fov, float near, float far)
    {
        if (fov <= 0f || fov >= Util.PI) throw new IllegalArgumentException();
        if (far <= near || near <= 0f) throw new IllegalArgumentException();
        this.resX = resX;
        this.resY = resY;
        pos = new Vector3f();
        rot = new Quat4f();
        res = new Vector2i(resX, resY);
        
        proj = Matrix4f.perspective(fov, res.y/(float)res.x, near, far);
    }
    
    public float getAspectRatio()
    {
        return res.y/(float)res.x;
    }
    
    public Matrix4f getProj()
    {
        return proj.clone();
    }
    
    public void glLoadProj()
    {
        proj.glLoad(GL11.GL_PROJECTION);
    }
    
    public void glLoadView()
    {
        getView().glLoad(GL11.GL_MODELVIEW);
    }
    
    public void glLoad()
    {
        glLoadProj();
        glLoadView();
    }
    
    public Matrix4f getView()
    {
        Matrix4f out = rot.clone().invert().toMatrix4f();
        out.multTranslate(pos.cnegate());
        return out;
    }
    
    public Matrix4f getViewProj()
    {
        return proj.clone().mult(getView());
    }
    
    /**
     * Converts the given world position to screen coordinates.
     * 
     * @param pos A world position.
     * @return pos in screen coordinates.
     */
    public Vector3f toScreen(Vector3f pos)
    {
        float midx = resX*.5f;
        float midy = resY*.5f;
        pos = pos.clone();
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
    public Vector3f dirToScreen(Vector3f dir)
    {
        float midx = resX*.5f;
        float midy = resY*.5f;
        dir = dir.clone();
        dir.mult(getView().toMatrix3f());
        dir.mult(proj.toMatrix3f());
        dir.x = dir.x*midx/dir.z + midx;
        dir.y = dir.y*midy/dir.z + midy;
        return dir;
    }
}
