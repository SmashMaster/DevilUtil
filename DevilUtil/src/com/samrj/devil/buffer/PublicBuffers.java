package com.samrj.devil.buffer;

/**
 * This class contains public utility buffers, which can be used for repetitive
 * or small operations without having to instantiate new buffers, wasting memory
 * and CPU cycles.
 * 
 * @author Samuel Johnson (SmashMaster)
 */

public class PublicBuffers
{
    public static final IntBuffer   ibuffer = new IntBuffer(16);
    public static final FloatBuffer fbuffer = new FloatBuffer(16);
    
    /**
     * Don't let anyone instantiate this.
     */
    private PublicBuffers() {}
}