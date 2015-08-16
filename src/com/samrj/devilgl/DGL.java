package com.samrj.devilgl;

import org.lwjgl.opengl.ContextCapabilities;
import org.lwjgl.opengl.GLContext;

/**
 * DevilGL. A state-based, object-oriented, forward compatible OpenGL wrapper,
 * inspired by deprecated OpenGL.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilGL/blob/master/LICENSE
 */
public class DGL
{
    private static boolean init;
    private static Thread thread;
    private static GLContext context;
    private static ContextCapabilities capabilities;
    private static VertexArrayObject vao;
    
    /**
     * Initializes DevilGL. Must be called from a thread on which an OpenGL
     * context is current.
     */
    public static void init()
    {
        if (init) throw new IllegalStateException("DGL already initialized.");
        thread = Thread.currentThread();
        context = GLContext.createFromCurrent();
        capabilities = context.getCapabilities();
        init = true;
    }
    
    private static void checkState()
    {
        if (!init) throw new IllegalStateException("DGL not initialized.");
        if (Thread.currentThread() != thread)
            throw new IllegalThreadStateException("DGL not initalized on current thread.");
    }
    
    static VertexArrayObject getBoundVAO()
    {
        checkState();
        return vao;
    }
    
    static void setBoundVAO(VertexArrayObject vao)
    {
        checkState();
        DGL.vao = vao;
    }
    
    /**
     * @return The current OpenGL context's capabilities.
     */
    public static ContextCapabilities getCapabilities()
    {
        checkState();
        return capabilities;
    }
    
    /**
     * Destroys DevilGL and releases any associated resources.
     */
    public static void destroy()
    {
        checkState();
        init = false;
        thread = null;
        context = null;
        capabilities = null;
    }
    
    private DGL()
    {
    }
}
