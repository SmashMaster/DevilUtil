package com.samrj.devil.gl;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import org.lwjgl.opengl.GL20;

public final class DGL
{
    private final static Map<String, Attribute> attributes = new HashMap<>();
    private final static Map<Integer, Attribute> activeAtts = new TreeMap<>();
    private static ShaderProgram shader = null;
    
    public static void use(ShaderProgram shader)
    {
        if (shader == DGL.shader) return;
        DGL.shader = shader;
        GL20.glUseProgram(shader.getID());
        refreshAttributes();
    }
    
    private static void ensureShaderActive()
    {
        if (shader == null)
            throw new IllegalStateException("No shader is currently bound!");
    }
    
    public static Uniform getUniform(String name)
    {
        return null;
    }
    
    /**
     * All attribute names are related to exactly one Attribute object.
     */
    public static Attribute getAttribute(String name)
    {
        Attribute att = attributes.get(name);
        
        if (att == null)
        {
            att = new Attribute(name);
            attributes.put(name, att);
        }
        
        return att;
    }
    
    public static void use(Attribute... atts)
    {
        ensureShaderActive();
        for (Attribute att : activeAtts.values()) att.disable();
        activeAtts.clear();
        
        for (Attribute att : atts)
        {
            att.enable(shader);
            activeAtts.put(att.getIndex(), att);
        }
    }
    
    /**
     * Keep current attributes active in case active shader is changed.
     */
    private static void refreshAttributes()
    {
        for (Attribute att : activeAtts.values()) att.enable(shader);
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
