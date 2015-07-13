package com.samrj.devil.graphics;

import com.samrj.devil.io.BufferUtil;
import com.samrj.devil.math.Mat3;
import com.samrj.devil.math.Mat4;
import com.samrj.devil.math.Util;
import com.samrj.devil.math.Vec2;
import com.samrj.devil.math.Vec3;
import java.nio.FloatBuffer;
import org.lwjgl.opengl.GL11;

public class GraphicsUtil
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
    
    public static void glLoadMatrix(Mat3 m, int mode)
    {
        GL11.glMatrixMode(mode);
        
        FloatBuffer buffer = BufferUtil.pubBufA.asFloatBuffer();
        buffer.clear();
        new Mat4(m).write(buffer);
        buffer.rewind();
        GL11.glLoadMatrixf(buffer);
    }
    
    public static void glLoadMatrix(Mat4 m, int mode)
    {
        GL11.glMatrixMode(mode);
        
        FloatBuffer buffer = BufferUtil.pubBufA.asFloatBuffer();
        buffer.clear();
        m.write(buffer);
        buffer.rewind();
        GL11.glLoadMatrixf(buffer);
    }
    
    public static void glVertex(Vec2 v)
    {
        GL11.glVertex2f(v.x, v.y);
    }
    
    public static void glVertex(Vec3 v)
    {
        GL11.glVertex3f(v.x, v.y, v.z);
    }
    
    private GraphicsUtil()
    {
    }
}