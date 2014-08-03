package com.samrj.devil.util;

import com.samrj.devil.math.*;
import org.lwjgl.opengl.Display;
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
    private Matrix4f proj;
    private Vector2i res;
    private Vector2f mid;
    
    public Camera3D(float fov, float near, float far)
    {
        if (fov <= 0f || fov >= 180f) throw new IllegalArgumentException();
        if (far <= near || near <= 0f) throw new IllegalArgumentException();
        pos = new Vector3f();
        rot = new Quat4f();
        res = new Vector2i(Display.getWidth(), Display.getHeight());
        mid = res.as2f().div(2f);
        
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
}