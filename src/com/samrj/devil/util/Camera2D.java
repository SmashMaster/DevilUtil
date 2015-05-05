package com.samrj.devil.util;

import com.samrj.devil.geo2d.AAB;
import com.samrj.devil.math.Matrix3f;
import com.samrj.devil.math.Matrix4f;
import com.samrj.devil.math.Vector2f;
import com.samrj.devil.math.Vector2i;
import org.lwjgl.opengl.GL11;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Camera2D
{
    public static void glLoadScreen(int resX, int resY)
    {
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GL11.glOrtho(0f, resX, 0f, resY, -1f, 1f);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();
    }
    
    public static void glLoadIdentity()
    {
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();
    }
    
    public final Vector2f pos;
    public float scale;
    private Matrix4f proj;
    private Vector2f mid;
    private Vector2i res;
    
    public Camera2D(int resX, int resY, Vector2f pos, float height)
    {
        if (height <= 0f) throw new IllegalArgumentException();
        this.pos = pos.clone();
        
        res = new Vector2i(resX, resY);
        mid = res.as2f().div(2f);
        
        proj = Matrix4f.ortho(mid.x, mid.y, -1f, 1f);
        scale = res.y/height;
    }
    
    public void setHeight(float height)
    {
        scale = res.y/height;
    }
    
    public Vector2f getMid()
    {
        return mid.clone();
    }
    
    public Vector2i getRes()
    {
        return res.clone();
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
        Matrix4f out = Matrix4f.scale(scale, scale, 0f);
        out.multTranslate(-pos.x, -pos.y, 0f);
        return out;
    }
    
    public Matrix3f toScreen()
    {
        Matrix3f out = Matrix3f.translate(mid.x, mid.y);
        out.multScale(scale, scale);
        out.multTranslate(-pos.x, -pos.y);
        return out;
    }
    
    public Matrix3f toWorld()
    {
        Matrix3f out = Matrix3f.translate(pos.x, pos.y);
        float invScale = 1f/scale;
        out.multScale(invScale, invScale);
        out.multTranslate(-mid.x, -mid.y);
        return out;
    }
    
    public AAB getViewBounds()
    {
        Matrix3f toWorld = toWorld();
        return AAB.bounds(new Vector2f().mult(toWorld),
                          res.as2f().mult(toWorld));
    }
    
    public void zoom(Vector2f pos, float factor)
    {
        Vector2f newPos = pos.cmult(toScreen());
        scale *= factor;
        newPos.mult(toWorld());
        this.pos.sub(newPos).add(pos);
    }
}
