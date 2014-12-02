package com.samrj.devil.gl;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL32;

public abstract class Mesh
{
    public static enum Type
    {
        RAW, INDEXED;
    }
    
    public static enum RenderMode
    {
        POINTS                  (GL11.GL_POINTS),
        LINE_STRIP              (GL11.GL_LINE_STRIP),
        LINE_LOOP               (GL11.GL_LINE_LOOP),
        LINES                   (GL11.GL_LINES),
        LINE_STRIP_ADJACENCY    (GL32.GL_LINE_STRIP_ADJACENCY),
        LINES_ADJACENCY         (GL32.GL_LINES_ADJACENCY),
        TRIANGLE_STRIP          (GL11.GL_TRIANGLE_STRIP),
        TRIANGLE_FAN            (GL11.GL_TRIANGLE_FAN),
        TRIANGLES               (GL11.GL_TRIANGLES),
        TRIANGLE_STRIP_ADJACENCY(GL32.GL_TRIANGLE_STRIP_ADJACENCY),
        TRIANGLES_ADJACENCY     (GL32.GL_TRIANGLES_ADJACENCY);
        
        public final int glEnum;
        
        private RenderMode(int glEnum)
        {
            this.glEnum = glEnum;
        }
    }
    
    public static enum Usage
    {
        GL_STREAM_DRAW (GL15.GL_STREAM_DRAW),
        GL_STATIC_DRAW (GL15.GL_STATIC_DRAW),
        GL_DYNAMIC_DRAW(GL15.GL_DYNAMIC_DRAW);
        
        public final int glEnum;
        
        private Usage(int glEnum)
        {
            this.glEnum = glEnum;
        }
    }
    
    abstract int vertex();
    abstract void index(int index);
    abstract void complete();
    abstract void draw();
    abstract public void destroy();
}
