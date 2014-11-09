package com.samrj.devil.gl;

import java.util.Map;
import java.util.TreeMap;
import org.lwjgl.opengl.GL20;

public final class DGL
{
    private final static Map<Integer, Attribute> attributes = new TreeMap<>();
    private static ShaderProgram shader = null;
    
    public static void use(ShaderProgram shader)
    {
        if (shader == DGL.shader) return;
        disableAttributes();
        DGL.shader = shader;
        GL20.glUseProgram(shader.getID());
    }
    
    public static Uniform getUniform(String name)
    {
        return null;
    }
    
    public static Attribute enableAttribute(String name)
    {
        if (shader == null)
            throw new IllegalStateException("No shader is active.");
        
        int shaderID = shader.getID();
        int index = GL20.glGetAttribLocation(shaderID, name);
        if (index == -1) throw new IllegalArgumentException(
                "No such attribute: '" + name + "'");
        
        Attribute att = attributes.get(index);
        
        if (att == null)
        {
            int size = GL20.glGetActiveAttribSize(shaderID, index);
            int type = GL20.glGetActiveAttribType(shaderID, index);
            int bytes = Attribute.typeBytesPerElem(type);
            
            att = new Attribute(index, size, bytes, type);
            attributes.put(index, att);
        }
        
        return att;
    }
    
    public static void disableAttributes()
    {
        for (Attribute att : attributes.values()) att.active = false;
        attributes.clear();
    }
    
    public static int vertex()
    {
        return -1;
    }
    
    public static void index(int index)
    {
    }
    
    public static com.samrj.devil.gl.Mesh define(Mesh.Type type)
    {
        return null;
    }
    
    public static void draw(Mesh mesh)
    {
    }
    
    public static void draw(Mesh.Type type)
    {
    }
    
    public static void end()
    {
    }
    
    private DGL() {}
}
