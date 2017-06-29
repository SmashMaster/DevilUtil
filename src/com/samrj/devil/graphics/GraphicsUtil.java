package com.samrj.devil.graphics;

import com.samrj.devil.io.MemStack;
import com.samrj.devil.math.Mat3;
import com.samrj.devil.math.Mat4;
import com.samrj.devil.math.Transform;
import com.samrj.devil.math.Util;
import com.samrj.devil.math.Vec2;
import com.samrj.devil.math.Vec3;
import com.samrj.devil.math.Vec4;
import org.lwjgl.opengl.GL11;

/**
 * Graphics utility class. Mostly for deprecated OpenGL.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class GraphicsUtil
{
    public static void drawCircle(Vec2 pos, float radius, int segments, int mode)
    {
        float dt = 8.0f/segments;
        GL11.glBegin(mode);
        for (float t=0.0f; t<8.0f; t+=dt)
            glVertex(Util.squareDir(t).normalize().mult(radius).add(pos));
        GL11.glEnd();
    }
    
    public static void drawCircle(Vec2 pos, float radius, int segments)
    {
        drawCircle(pos, radius, segments, GL11.GL_LINE_LOOP);
    }
    
    public static void drawFilledCircle(Vec2 pos, float radius, int segments)
    {
        drawCircle(pos, radius, segments, GL11.GL_TRIANGLE_FAN);
    }
    
    private static long mat3As4(Mat3 m)
    {
        return MemStack.wrapf(m.a,  m.d,  m.g,  0.0f,
                              m.b,  m.e,  m.h,  0.0f,
                              m.c,  m.f,  m.i,  0.0f,
                              0.0f, 0.0f, 0.0f, 1.0f);
    }
    
    public static void glLoadMatrix(Mat3 m)
    {
        long address = mat3As4(m);
        GL11.nglLoadMatrixf(address);
        MemStack.pop();
    }
    
    public static void glLoadMatrix(Mat3 m, int mode)
    {
        GL11.glMatrixMode(mode);
        glLoadMatrix(m);
    }
    
    public static void glMultMatrix(Mat3 m)
    {
        long address = mat3As4(m);
        GL11.nglMultMatrixf(address);
        MemStack.pop();
    }
    
    public static void glMultMatrix(Mat3 m, int mode)
    {
        GL11.glMatrixMode(mode);
        glMultMatrix(m);
    }
    
    public static void glLoadMatrix(Mat4 m)
    {
        long address = MemStack.wrap(m);
        GL11.nglLoadMatrixf(address);
        MemStack.pop();
    }
    
    public static void glLoadMatrix(Mat4 m, int mode)
    {
        GL11.glMatrixMode(mode);
        glLoadMatrix(m);
    }
    
    public static void glMultMatrix(Mat4 m)
    {
        long address = MemStack.wrap(m);
        GL11.nglMultMatrixf(address);
        MemStack.pop();
    }
    
    public static void glMultMatrix(Mat4 m, int mode)
    {
        GL11.glMatrixMode(mode);
        glMultMatrix(m);
    }
    
    public static void glLoadMatrix(Transform t)
    {
        glLoadMatrix(Mat4.transform(t));
    }
    
    public static void glLoadMatrix(Transform t, int mode)
    {
        glLoadMatrix(Mat4.transform(t), mode);
    }
    
    public static void glMultMatrix(Transform t)
    {
        glMultMatrix(Mat4.transform(t));
    }
    
    public static void glMultMatrix(Transform t, int mode)
    {
        glMultMatrix(Mat4.transform(t), mode);
    }
    
    public static void glVertex(Vec2 v)
    {
        GL11.glVertex2f(v.x, v.y);
    }
    
    public static void glVertex(Vec2... vecs)
    {
        for (Vec2 v : vecs) glVertex(v);
    }
    
    public static void glVertex(Vec3 v)
    {
        GL11.glVertex3f(v.x, v.y, v.z);
    }
    
    public static void glVertex(Vec3... vecs)
    {
        for (Vec3 v : vecs) glVertex(v);
    }
    
    public static void glClearColor(Vec3 v)
    {
        GL11.glClearColor(v.x, v.y, v.z, 1.0f);
    }
    
    public static void glClearColor(Vec4 v)
    {
        GL11.glClearColor(v.x, v.y, v.z, v.w);
    }
    
    public static void glColor(Vec3 v)
    {
        GL11.glColor3f(v.x, v.y, v.z);
    }
    
    public static void glColor(Vec4 v)
    {
        GL11.glColor4f(v.x, v.y, v.z, v.w);
    }
    
    private GraphicsUtil()
    {
    }
}
