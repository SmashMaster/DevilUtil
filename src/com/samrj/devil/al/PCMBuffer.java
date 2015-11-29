package com.samrj.devil.al;

import com.samrj.devil.io.DynamicBuffer;
import com.samrj.devil.io.Memory;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
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
    
    Memory close()
    {
        return buffer.close();
    }
}
