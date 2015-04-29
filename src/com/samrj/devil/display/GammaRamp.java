package com.samrj.devil.display;

import com.samrj.devil.buffer.BufferUtil;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import org.lwjgl.system.MemoryUtil;

/**
 * Gamma ramp class.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 */
public final class GammaRamp
{
    /**
     * An array of values describing the response of the red channel.
     */
    public final short[] red;
    
    /**
     * An array of values describing the response of the green channel.
     */
    public final short[] green;
    
    /**
     * An array of values describing the response of the blue channel.
     */
    public final short[] blue;
    
    /**
     * Creates a new gamma ramp, with all values initialized to zero. Each array
     * has a size of 256.
     */
    public GammaRamp()
    {
        red = new short[256];
        green = new short[256];
        blue = new short[256];
    }
    
    GammaRamp(ByteBuffer buffer)
    {
        this();
        
        long pRedArray = buffer.getLong();
        long pGreenArray = buffer.getLong();
        long pBlueArray = buffer.getLong();
        long size = buffer.getLong();
        
        if (size != 256) return;
        
        MemoryUtil.memByteBuffer(pRedArray, 2*256).asShortBuffer().get(red);
        MemoryUtil.memByteBuffer(pGreenArray, 2*256).asShortBuffer().get(green);
        MemoryUtil.memByteBuffer(pBlueArray, 2*256).asShortBuffer().get(blue);
    }
    
    ByteBuffer toBuffer()
    {
        ShortBuffer redBuffer = BufferUtil.wrapShorts(red);
        ShortBuffer greenBuffer = BufferUtil.wrapShorts(green);
        ShortBuffer blueBuffer = BufferUtil.wrapShorts(blue);
        
        ByteBuffer out = BufferUtil.createByteBuffer(4*8);
        out.putLong(MemoryUtil.memAddress(redBuffer));
        out.putLong(MemoryUtil.memAddress(greenBuffer));
        out.putLong(MemoryUtil.memAddress(blueBuffer));
        out.putLong(256);
        out.rewind();
        return out;
    }
}
