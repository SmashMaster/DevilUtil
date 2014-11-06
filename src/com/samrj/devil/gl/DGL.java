package com.samrj.devil.gl;

public final class DGL
{
    public static enum Mesh
    {
        RAW, INDEXED;
    }
    
    public static void use(ShaderProgram shader)
    {
    }
    
    public static Uniform getUniform(String name)
    {
        return null;
    }
    
    public static void set(Uniform uniform, float x, float y, float z)
    {
    }
    
    public static void setUniform(String name, float x, float y, float z)
    {
        set(getUniform(name), x, y, z);
    }
    
    public static Attribute getAttribute(String name)
    {
        return null;
    }
    
    public static void set(Attribute attribute, float x, float y)
    {
    }
    
    public static void use(Attribute... attributes)
    {
    }
    
    public static int vertex()
    {
        return -1;
    }
    
    public static void index()
    {
    }
    
    public static com.samrj.devil.gl.Mesh define(DGL.Mesh mesh)
    {
        return null;
    }
    
    public static void draw(com.samrj.devil.gl.Mesh mesh)
    {
    }
    
    public static void draw(DGL.Mesh mesh)
    {
    }
    
    public static void end()
    {
    }
    
    private DGL() {}
}
