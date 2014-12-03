package com.samrj.devil.gl;

import org.lwjgl.opengl.GL11;

public final class DGL
{
    public static enum ScreenBuffer
    {
        COLOR  (GL11.GL_COLOR_BUFFER_BIT),
        DEPTH  (GL11.GL_DEPTH_BUFFER_BIT),
        ACCUM  (GL11.GL_ACCUM_BUFFER_BIT),
        STENCIL(GL11.GL_STENCIL_BUFFER_BIT);
        
        final int glEnum;
        
        private ScreenBuffer(int glEnum)
        {
            this.glEnum = glEnum;
        }
    }
    
    private static DGLImpl impl;
    
    public static void init()
    {
        impl = new DGL30();
    }
    
    public static void use(ShaderProgram shader)
    {
        impl.use(shader);
    }
    
    public static Uniform getUniform(String name)
    {
        return impl.getUniform(name);
    }
    
    public static Attribute getAttribute(String name)
    {
        return impl.getAttribute(name);
    }
    
    public static void use(Attribute... atts)
    {
        impl.use(atts);
    }
    
    public static int vertex()
    {
        return impl.vertex();
    }
    
    public static void index(int index)
    {
        impl.index(index);
    }
    
    public static Mesh30 define(Mesh30.Type type, Mesh30.RenderMode mode)
    {
        return impl.define(type, mode);
    }
    
    public static Mesh30 define(Mesh30.Type type)
    {
        return impl.define(type);
    }
    
    public static void draw(Mesh30 mesh)
    {
        impl.draw(mesh);
    }
    
    public static void draw(Mesh30.Type type, Mesh30.RenderMode mode)
    {
        impl.draw(type, mode);
    }
    
    public static void draw(Mesh30.Type type)
    {
        impl.draw(type);
    }
    
    public static void end()
    {
        impl.end();
    }
    
    public static void clearColor(float r, float g, float b, float a)
    {
        impl.clearColor(r, g, b, a);
    }
    
    public static void clear(DGL.ScreenBuffer... buffers)
    {
        impl.clear(buffers);
    }
}
