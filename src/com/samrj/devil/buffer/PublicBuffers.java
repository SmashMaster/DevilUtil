package com.samrj.devil.buffer;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import org.lwjgl.BufferUtils;

/**
 * Publicly available utility buffers. Each has a capacity of 64 bytes. As such,
 * these should fit on an x86 cache line, and fbuffer should fit a Matrix4f.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class PublicBuffers
{
    public static final IntBuffer ibuffer = BufferUtils.createIntBuffer(16);
    public static final FloatBuffer fbuffer = BufferUtils.createFloatBuffer(16);
    
    private PublicBuffers()
    {
    }
}
