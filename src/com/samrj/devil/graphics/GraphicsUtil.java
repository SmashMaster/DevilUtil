package com.samrj.devil.graphics;

import com.samrj.devil.io.Memory;
import com.samrj.devil.math.Mat3;
import com.samrj.devil.math.Mat4;
import com.samrj.devil.math.Util;
import com.samrj.devil.math.Vec2;
import com.samrj.devil.math.Vec3;
import com.samrj.devil.math.Vec4;
import java.nio.ByteBuffer;
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
    
    private static Memory allocMat3as4(Mat3 m)
    {
        Memory block = new Memory(16*4);
        ByteBuffer b = block.buffer;
        b.putFloat(m.a);  b.putFloat(m.d);  b.putFloat(m.g);  b.putFloat(0.0f);
        b.putFloat(m.b);  b.putFloat(m.e);  b.putFloat(m.h);  b.putFloat(0.0f);
        b.putFloat(m.c);  b.putFloat(m.f);  b.putFloat(m.i);  b.putFloat(0.0f);
        b.putFloat(0.0f); b.putFloat(0.0f); b.putFloat(0.0f); b.putFloat(1.0f);
        return block;
    }
    
    public static void glLoadMatrix(Mat3 m, int mode)
    {
        Memory b = allocMat3as4(m);
        GL11.glMatrixMode(mode);
        GL11.nglLoadMatrixf(b.address);
        b.free();
    }
    
    public static void glMultMatrix(Mat3 m, int mode)
    {
        Memory b = allocMat3as4(m);
        GL11.glMatrixMode(mode);
        GL11.nglMultMatrixf(b.address);
        b.free();
    }
    
    public static void glLoadMatrix(Mat4 m, int mode)
    {
        Memory b = Memory.wrap(m);
        GL11.glMatrixMode(mode);
        GL11.nglLoadMatrixf(b.address);
        b.free();
    }
    
    public static void glMultMatrix(Mat4 m, int mode)
    {
        Memory b = Memory.wrap(m);
        GL11.glMatrixMode(mode);
        GL11.nglMultMatrixf(b.address);
        b.free();
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
