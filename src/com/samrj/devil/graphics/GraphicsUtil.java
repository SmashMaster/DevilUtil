package com.samrj.devil.graphics;

import com.samrj.devil.io.BufferUtil;
import com.samrj.devil.math.Mat3;
import com.samrj.devil.math.Mat4;
import com.samrj.devil.math.Util;
import com.samrj.devil.math.Vec2;
import com.samrj.devil.math.Vec3;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import org.lwjgl.opengl.GL11;

/**
 * Graphics utility class.
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
    
    private static FloatBuffer bMat3(ByteBuffer byteBuffer, Mat3 m)
    {
        FloatBuffer b = byteBuffer.asFloatBuffer();
        b.clear();
        b.put(m.a); b.put(m.d); b.put(m.g); b.put(0.0f);
        b.put(m.b); b.put(m.e); b.put(m.h); b.put(0.0f);
        b.put(m.c); b.put(m.f); b.put(m.i); b.put(0.0f);
        b.put(0.0f); b.put(0.0f); b.put(0.0f); b.put(1.0f);
        b.rewind();
        return b;
    }
    
    public static void glLoadMatrix(Mat3 m, int mode)
    {
        GL11.glMatrixMode(mode);
        GL11.glLoadMatrixf(bMat3(BufferUtil.pubBufA, m));
    }
    
    public static void glMultMatrix(Mat3 m, int mode)
    {
        GL11.glMatrixMode(mode);
        GL11.glMultMatrixf(bMat3(BufferUtil.pubBufA, m));
    }
    
    public static void glLoadMatrix(Mat4 m, int mode)
    {
        GL11.glMatrixMode(mode);
        GL11.glLoadMatrixf(BufferUtil.fBuffer(m));
    }
    
    public static void glMultMatrix(Mat4 m, int mode)
    {
        GL11.glMatrixMode(mode);
        GL11.glMultMatrixf(BufferUtil.fBuffer(m));
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
    
    private GraphicsUtil()
    {
    }
}