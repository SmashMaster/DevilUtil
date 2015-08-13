package com.samrj.devilgl;

import com.samrj.devil.math.Vec3;
import org.lwjgl.opengl.GL15;

public class VertexData
{
    private static enum State
    {
        EMPTY, INCOMPLETE, COMPLETE;
    }
    
    private State state;
    
    public VertexData(int expectedSize)
    {
        state = State.EMPTY;
    }
    
    private void regAtt(String name, int size)
    {
    }
    
    /**
     * If one doesn't already exist, registers a new Vec3 attribute with the
     * given name. Then returns the attribute object corresponding with that
     * name.
     * 
     * @param name The name of the attribute to register.
     * @return The attribute object corresponding with the given name.
     * @throws java.lang.IllegalStateException If any vertices have been emitted
     *         by this.
     */
    public Vec3 vec3(String name)
    {
        if (state != State.EMPTY) throw new IllegalStateException();
        
        return null;
    }
    
    /**
     * Emits a new vertex with all currently active attributes, and returns its
     * index.
     * 
     * @return The index of the emitted vertex.
     */
    public int vertex()
    {
        if (state == State.COMPLETE) throw new IllegalStateException();
        else if (state == State.EMPTY) state = State.INCOMPLETE;
        
        return -1;
    }
    
    /**
     * Emits an index.
     * 
     * @param v The index to emit.
     */
    public void index(int v)
    {
    }
    
    /**
     * Completes this vertex data, sending it to the GPU so that it is ready to
     * be rendered.
     */
    public void complete()
    {
        if (state != State.INCOMPLETE) throw new IllegalStateException();
        state = State.COMPLETE;
        
        
    }
}
