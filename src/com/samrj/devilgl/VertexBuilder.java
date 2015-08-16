package com.samrj.devilgl;

/**
 * Abstract class for generating forward-compatible vertex data in the classic
 * OpenGL fashion.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilGL/blob/master/LICENSE
 */
public abstract class VertexBuilder
{
    public static enum State
    {
        /**
         * The vertex builder may register attributes, but it is not ready to
         * emit vertices or indices, or be drawn.
         */
        NEW,
        
        /**
         * The vertex builder is ready to emit new vertices and indices. It may
         * or may not be ready to draw. It can no longer register attributes.
         */
        READY,
        
        /**
         * The vertex builder is ready to be drawn, but may not emit any new
         * vertices or indices.
         */
        COMPLETE,
        
        /**
         * The vertex builder may not register new attributes, emit vertices or
         * indices, or be drawn. All associated resources have been released.
         */
        DESTROYED;
    }
    
    State state;
    
    public VertexBuilder()
    {
        state = State.NEW;
    }
    
    public final State getState()
    {
        return state;
    }
    
    //Register attributes
    
    //Build vertices
    public abstract void begin();
    
    //Upload data
    public abstract void upload();
    
    //Bind to shader
    
    //Draw
    public abstract void draw();
    
    //Destroy
}
