package com.samrj.devil.gl;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

abstract class DGLImpl
{
    static enum State
    {
        IDLE, DRAW_MESH, DEFINE_MESH;
    }
    
    final Map<String, Attribute> attributes = new HashMap<>();
    final Map<Integer, Attribute> activeAttribs = new TreeMap<>();
    ShaderProgram shader = null;
    State state = State.IDLE;
    Mesh30 mesh = null;
    
    void ensureState(State... states)
    {
        for (State state : states) if (this.state == state) return;
        throw new IllegalStateException(
                "Current state " + this.state.name() +
                " is illegal for attempted operation.");
    }
    
    public void use(ShaderProgram shader)
    {
        ensureState(State.IDLE);
        if (shader == this.shader) return;
        this.shader = shader;
        GL20.glUseProgram(shader.getID());
        refreshAttributes();
    }
    
    void ensureShaderActive()
    {
        if (shader == null)
            throw new IllegalStateException("No shader is currently bound!");
    }
    
    public Uniform getUniform(String name)
    {
        return null;
    }
    
    /**
     * All attribute names are related to exactly one Attribute object.
     */
    public Attribute getAttribute(String name)
    {
        Attribute att = attributes.get(name);
        
        if (att == null)
        {
            att = new Attribute(name);
            attributes.put(name, att);
        }
        
        return att;
    }
    
    public void use(Attribute... atts)
    {
        ensureState(State.IDLE);
        ensureShaderActive();
        for (Attribute att : activeAttribs.values()) att.disable();
        activeAttribs.clear();
        
        for (Attribute att : atts)
        {
            att.enable(shader);
            activeAttribs.put(att.getIndex(), att);
        }
    }
    
    /**
     * Keep current attributes active in case active shader is changed.
     */
    void refreshAttributes()
    {
        for (Attribute att : activeAttribs.values()) att.enable(shader);
    }
    
    abstract int vertex();
    abstract void index(int index);
    abstract Mesh30 define(Mesh30.Type type, Mesh30.RenderMode mode);
    abstract Mesh30 define(Mesh30.Type type);
    abstract void draw(Mesh30 mesh);
    abstract void draw(Mesh30.Type type, Mesh30.RenderMode mode);
    abstract void draw(Mesh30.Type type);
    abstract void end();
    
    void clearColor(float r, float g, float b, float a)
    {
        GL11.glClearColor(r, g, b, a);
    }
    
    void clear(DGL.ScreenBuffer... buffers)
    {
        int bufferBits = 0;
        for (DGL.ScreenBuffer buffer : buffers) bufferBits |= buffer.glEnum;
        GL11.glClear(bufferBits);
    }
}
