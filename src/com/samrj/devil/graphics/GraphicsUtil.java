package com.samrj.devil.graphics;

import com.samrj.devil.math.Util;
import com.samrj.devil.math.Vector2f;
import org.lwjgl.opengl.GL11;

public class GraphicsUtil
{
    public static void drawCircle(Vector2f pos, float radius, int segments, int mode)
    {
        float dt = 8.0f/segments;
        GL11.glBegin(mode);
        for (float t=0.0f; t<8.0f; t+=dt)
            Util.sqDir(t).setLength(radius).add(pos).glVertex();
        GL11.glEnd();
    }
    
    public static void drawCircle(Vector2f pos, float radius, int segments)
    {
        drawCircle(pos, radius, segments, GL11.GL_LINE_LOOP);
    }
    
    public static void drawFilledCircle(Vector2f pos, float radius, int segments)
    {
        drawCircle(pos, radius, segments, GL11.GL_TRIANGLE_FAN);
    }
    
    private GraphicsUtil()
    {
    }
}