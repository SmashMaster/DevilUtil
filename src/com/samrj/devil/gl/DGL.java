package com.samrj.devil.gl;

import com.samrj.devil.util.IdentitySet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

public final class DGL
{
    private static enum State
    {
        IDLE, DRAW_MESH, DEFINE_MESH;
    }
    
    private static ShaderProgram shader = null;
    private static State state = State.IDLE;
    private static Mesh mesh = null;
    
    private static void ensureState(String error, State... states)
    {
        for (State state : states) if (DGL.state == state) return;
        throw new IllegalStateException(error);
    }
    
    private static void ensureShaderActive()
    {
        if (shader == null)
            throw new IllegalStateException("No shader is currently bound.");
    }
    
    /**
     * Uses the given shader for all draw operations.
     * 
     * @param shader the shader to bind.
     */
    public static void useShader(ShaderProgram shader)
    {
        ensureState("Cannot bind shaders in state: " + state, State.IDLE);
        if (shader == DGL.shader) return;
        DGL.shader = shader;
        GL20.glUseProgram(shader.getID());
        refreshAttributes();
        refreshUniforms();
    }
    
    //<editor-fold defaultstate="collapsed" desc="Attribute Methods">
    /**
     * All attribute names are guaranteed to have up to one Attribute object
     * associated with them. If that object is not in use, it will automatically
     * be garbage collected.
     */
    private static final Map<String, Attribute> attributes = new WeakHashMap<>();
    private static final Set<Attribute> activeAttributes = new IdentitySet<>();
    
    /**
     * Returns the Attribute object for the given shader variable name, or
     * creates one if it does not exist.
     * 
     * @param name the shader variable name of an attribute
     * @return the Attribute object corresponding to the given name.
     */
    public static Attribute getAttribute(String name)
    {
        ensureState("Cannot fetch attributes in state: " + state, State.IDLE);
        if (name == null) throw new NullPointerException();
        
        Attribute att = attributes.get(name);
        if (att == null)
        {
            att = new Attribute(name);
            attributes.put(name, att);
        }
        return att;
    }
    
    /**
     * Enables all given attributes.
     * 
     * @param atts each attribute to be enabled.
     */
    public static void enableAttributes(Attribute... atts)
    {
        ensureState("Cannot enable attributes in state: " + state, State.IDLE);
        for (Attribute att : atts)
        {
            att.enable(shader);
            activeAttributes.add(att);
        }
    }
    
    /**
     * Enables and returns the given attribute.
     * 
     * @param name the shader variable name of the given attribute.
     * @return the attribute corresponding to the given name.
     */
    public static Attribute enableAttribute(String name)
    {
        Attribute att = getAttribute(name);
        enableAttributes(att);
        return att;
    }
    
    /**
     * @return an array containing all enabled attributes.
     */
    public static Attribute[] getEnabledAttributes()
    {
        ensureState("Cannot enable attributes in state: " + state, State.IDLE);
        Attribute[] out = new Attribute[activeAttributes.size()];
        return activeAttributes.toArray(out);
    }
    
    /**
     * Disables all given attributes.
     * 
     * @param atts each attribute to disable.
     */
    public static void disableAttributes(Attribute... atts)
    {
        for (Attribute att : atts)
        {
            att.disable();
            activeAttributes.remove(att);
        }
    }
    
    /**
     * Disables all attributes.
     */
    public static void disableAttributes()
    {
        for (Attribute att : activeAttributes) att.disable();
        activeAttributes.clear();
    }
    
    /**
     * Re-enable all currently enabled attributes on the current shader. Any
     * attributes that cannot be enabled are ignored.
     */
    private static void refreshAttributes()
    {
        Iterator<Attribute> i = activeAttributes.iterator();
        for (Attribute att = i.next(); i.hasNext(); att = i.next())
            if (!att.enableSoft(shader)) i.remove();
    }
    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Uniform Methods">
    private static final Map<String, Uniform> uniforms = new WeakHashMap<>();
    
    /**
     * Returns the Uniform object for the given shader variable name, or
     * creates one if it does not exist.
     * 
     * @param name the shader variable name of an attribute
     * @return the Attribute object corresponding to the given name.
     */
    public static Uniform getUniform(String name)
    {
        ensureState("Cannot fetch uniforms in state: " + state, State.IDLE);
        if (name == null) throw new NullPointerException();
        
        Uniform uni = uniforms.get(name);
        if (uni == null)
        {
            uni = new Uniform(name);
            uni.enableSoft(shader);
            uniforms.put(name, uni);
        }
        return uni;
    }
    
    private static void refreshUniforms()
    {
        for (Uniform uniform : uniforms.values()) uniform.enableSoft(shader);
    }
    //</editor-fold>
    
    
    
    public static int vertex()
    {
        ensureState("Cannot define vertices in state: " + state, State.DEFINE_MESH, State.DRAW_MESH);
        return mesh.vertex();
    }
    
    public static void index(int index)
    {
        ensureState("Cannot define indices in state: " + state, State.DEFINE_MESH, State.DRAW_MESH);
        mesh.index(index);
    }
    
    public static Mesh define(Mesh.Type type, Mesh.RenderMode mode)
    {
        ensureState("Cannot define new meshes in state: " + State.IDLE);
        ensureShaderActive();
        state = State.DEFINE_MESH;
        mesh = new Mesh(type, Mesh.Usage.GL_STATIC_DRAW, mode, activeAttributes);
        return mesh;
    }
    
    public static Mesh define(Mesh.Type type)
    {
        return define(type, Mesh.RenderMode.TRIANGLES);
    }
    
    public static void draw(Mesh mesh)
    {
        ensureState("Cannot start drawing in state: " + State.IDLE);
        ensureShaderActive();
        mesh.draw();
    }
    
    public static void draw(Mesh.Type type, Mesh.RenderMode mode)
    {
        ensureState("Cannot start drawing in state: " + State.IDLE);
        ensureShaderActive();
        state = State.DRAW_MESH;
        mesh = new Mesh(type, Mesh.Usage.GL_STREAM_DRAW, mode, activeAttributes);
    }
    
    public static void draw(Mesh.Type type)
    {
        draw(type, Mesh.RenderMode.TRIANGLES);
    }
    
    public static void end()
    {
        ensureState("No state to end.", State.DEFINE_MESH, State.DRAW_MESH);
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
