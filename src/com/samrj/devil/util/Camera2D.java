package com.samrj.devil.util;

import com.samrj.devil.geo2d.AAB;
import com.samrj.devil.graphics.GraphicsUtil;
import com.samrj.devil.math.Mat3;
import com.samrj.devil.math.Mat4;
import com.samrj.devil.math.Vec2;
import com.samrj.devil.math.Vec2i;
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
    
    public final Vec2 pos;
    public float scale;
    private Mat4 proj;
    private Vec2 mid;
    private Vec2i res;
    
    public Camera2D(int resX, int resY, Vec2 pos, float height)
    {
        if (height <= 0f) throw new IllegalArgumentException();
        this.pos = new Vec2(pos);
        
        res = new Vec2i(resX, resY);
        mid = new Vec2(res.x, res.y).div(2.0f);
        
        proj = Mat4.orthographic(mid.x, mid.y, -1f, 1f);
        scale = res.y/height;
    }
    
    public void setHeight(float height)
    {
        scale = res.y/height;
    }
    
    public Vec2 getMid()
    {
        return new Vec2(mid);
    }
    
    public Vec2i getRes()
    {
        return new Vec2i(res);
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
    
    public Mat3 getView()
    {
        Mat3 out = Mat3.scaling(scale);
        out.translate(Vec2.negate(pos));
        return out;
    }
    
    public Mat3 toScreen()
    {
        Mat3 out = Mat3.translation(mid);
        out.mult(scale);
        out.translate(Vec2.negate(pos));
        return out;
    }
    
    public Mat3 toWorld()
    {
        Mat3 out = Mat3.translation(pos);
        out.div(scale);
        out.translate(Vec2.negate(mid));
        return out;
    }
    
    public AAB getViewBounds()
    {
        Mat3 toWorld = toWorld();
        return AAB.bounds(new Vec2().mult(toWorld),
                          new Vec2(res.x, res.y).mult(toWorld));
    }
    
    public void zoom(Vec2 pos, float factor)
    {
        Vec2 newPos = Vec2.mult(pos, toScreen());
        scale *= factor;
        newPos.mult(toWorld());
        this.pos.sub(newPos).add(pos);
    }
}
