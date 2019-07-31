package com.samrj.devil.al;

import com.samrj.devil.io.DynamicBuffer;
import java.nio.ByteBuffer;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2019 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
class PCMBuffer
{
    int rate, channels, bits;
    final DynamicBuffer buffer;
    
    PCMBuffer()
    {
        buffer = new DynamicBuffer();
    }
    
    ByteBuffer close()
    {
        return buffer.close();
    }
}
