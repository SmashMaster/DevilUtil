package com.samrj.devil.gl;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

public final class DGL
{
    private static enum State
    {
        IDLE, DRAW_MESH, DEFINE_MESH;
    }
    
    private final static Map<String, Attribute> attributes = new HashMap<>();
    private final static Map<Integer, Attribute> activeAttribs = new TreeMap<>();
    private static ShaderProgram shader = null;
    private static State state = State.IDLE;
    private static Mesh mesh = null;
    
    private static void ensureState(State... states)
    {
        for (State state : states) if (DGL.state == state) return;
        throw new IllegalStateException(
                "Current state " + DGL.state.name() +
                " is illegal for attempted operation.");
    }
    
    public static void use(ShaderProgram shader)
    {
        ensureState(State.IDLE);
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
    private static void refreshAttributes()
    {
        for (Attribute att : activeAttribs.values()) att.enable(shader);
    }
    
    public static int vertex()
    {
        ensureState(State.DEFINE_MESH, State.DRAW_MESH);
        return mesh.vertex();
    }
    
    public static void index(int index)
    {
        ensureState(State.DEFINE_MESH, State.DRAW_MESH);
        mesh.index(index);
    }
    
    public static Mesh define(Mesh.Type type, Mesh.RenderMode mode)
    {
        ensureState(State.IDLE);
        ensureShaderActive();
        state = State.DEFINE_MESH;
        mesh = new Mesh(type, Mesh.Usage.GL_STATIC_DRAW, mode, activeAttribs);
        return mesh;
    }
    
    public static Mesh define(Mesh.Type type)
    {
        return define(type, Mesh.RenderMode.TRIANGLES);
    }
    
    public static void draw(Mesh mesh)
    {
        ensureState(State.IDLE);
        ensureShaderActive();
        mesh.draw();
    }
    
    public static void draw(Mesh.Type type, Mesh.RenderMode mode)
    {
        ensureState(State.IDLE);
        ensureShaderActive();
        state = State.DRAW_MESH;
        mesh = new Mesh(type, Mesh.Usage.GL_STREAM_DRAW, mode, activeAttribs);
    }
    
    public static void draw(Mesh.Type type)
    {
        draw(type, Mesh.RenderMode.TRIANGLES);
    }
    
    public static void end()
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
    
    public static enum ScreenBuffer
    {
        COLOR  (GL11.GL_COLOR_BUFFER_BIT),
        DEPTH  (GL11.GL_DEPTH_BUFFER_BIT),
        ACCUM  (GL11.GL_ACCUM_BUFFER_BIT),
        STENCIL(GL11.GL_STENCIL_BUFFER_BIT);
        
        private final int glEnum;
        
        private ScreenBuffer(int glEnum)
        {
            this.glEnum = glEnum;
        }
    }
    
    public static void clearColor(float r, float g, float b, float a)
    {
        GL11.glClearColor(r, g, b, a);
    }
    
    public static void clear(ScreenBuffer... buffers)
    {
        int bufferBits = 0;
        for (ScreenBuffer buffer : buffers) bufferBits |= buffer.glEnum;
        GL11.glClear(bufferBits);
    }
    
    private DGL() {}
}
