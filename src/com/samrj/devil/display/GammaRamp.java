package com.samrj.devil.display;

import com.samrj.devil.io.Memory.Block;
import static com.samrj.devil.io.Memory.memUtil;
import java.nio.ByteBuffer;
import org.lwjgl.system.MemoryUtil;

/**
 * Gamma ramp class.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
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
    
    Block[] allocate()
    {
        Block redBlock = memUtil.wraps(red);
        Block greenBlock = memUtil.wraps(green);
        Block blueBlock = memUtil.wraps(blue);
        Block pointerBlock = memUtil.alloc(4*8);
        
        ByteBuffer pointerBuffer = pointerBlock.read();
        pointerBuffer.putLong(redBlock.address());
        pointerBuffer.putLong(greenBlock.address());
        pointerBuffer.putLong(blueBlock.address());
        pointerBuffer.putLong(256);
        
        return new Block[]{redBlock, greenBlock, blueBlock, pointerBlock};
    }
}
