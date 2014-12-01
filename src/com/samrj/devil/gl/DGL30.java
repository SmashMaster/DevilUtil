package com.samrj.devil.gl;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

class DGL30 implements DGLImpl
{
    private static enum State
    {
        IDLE, DRAW_MESH, DEFINE_MESH;
    }
    
    private final Map<String, Attribute> attributes = new HashMap<>();
    private final Map<Integer, Attribute> activeAttribs = new TreeMap<>();
    private ShaderProgram shader = null;
    private State state = State.IDLE;
    private Mesh mesh = null;
    
    DGL30()
    {
    }
    
    private void ensureState(State... states)
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
    
    private void ensureShaderActive()
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
    private void refreshAttributes()
    {
        for (Attribute att : activeAttribs.values()) att.enable(shader);
    }
    
    public int vertex()
    {
        ensureState(State.DEFINE_MESH, State.DRAW_MESH);
        return mesh.vertex();
    }
    
    public void index(int index)
    {
        ensureState(State.DEFINE_MESH, State.DRAW_MESH);
        mesh.index(index);
    }
    
    public Mesh define(Mesh.Type type, Mesh.RenderMode mode)
    {
        ensureState(State.IDLE);
        ensureShaderActive();
        state = State.DEFINE_MESH;
        mesh = new Mesh(type, Mesh.Usage.GL_STATIC_DRAW, mode, activeAttribs);
        return mesh;
    }
    
    public Mesh define(Mesh.Type type)
    {
        return define(type, Mesh.RenderMode.TRIANGLES);
    }
    
    public void draw(Mesh mesh)
    {
        ensureState(State.IDLE);
        ensureShaderActive();
        mesh.draw();
    }
    
    public void draw(Mesh.Type type, Mesh.RenderMode mode)
    {
        ensureState(State.IDLE);
        ensureShaderActive();
        state = State.DRAW_MESH;
        mesh = new Mesh(type, Mesh.Usage.GL_STREAM_DRAW, mode, activeAttribs);
    }
    
    public void draw(Mesh.Type type)
    {
        draw(type, Mesh.RenderMode.TRIANGLES);
    }
    
    public void end()
    {
        ensureState(State.DEFINE_MESH, State.DRAW_MESH);
        boolean drawMesh = state == State.DRAW_MESH;
        mesh.complete();
        state = State.IDLE;
        if (drawMesh)
        {
            draw(mesh);
            mesh.destroy();
        }
        mesh = null;
    }
    
    public void clearColor(float r, float g, float b, float a)
    {
        GL11.glClearColor(r, g, b, a);
    }
    
    public void clear(DGL.ScreenBuffer... buffers)
    {
        int bufferBits = 0;
        for (DGL.ScreenBuffer buffer : buffers) bufferBits |= buffer.glEnum;
        GL11.glClear(bufferBits);
    }
}
