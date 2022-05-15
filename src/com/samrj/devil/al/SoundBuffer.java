package com.samrj.devil.al;

import com.samrj.devil.math.Util.PrimType;

import java.nio.ByteBuffer;

import static org.lwjgl.system.MemoryUtil.memAddress0;
import static org.lwjgl.system.MemoryUtil.memFree;

/**
 * Sound buffer. Stores raw sound data.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2019 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class SoundBuffer extends DALObj
{
    public final int samples;
    public final int channels;
    public final PrimType type;
    public final int rate;
    public final int size;
    
    public final ByteBuffer buffer;
    
    SoundBuffer(PCMBuffer pcm)
    {
        try
        {
            channels = pcm.channels;
            if (channels < 1 || channels > 2)
                throw new IllegalArgumentException("Sound must be mono or stereo.");

            if (pcm.bits == 8) type = PrimType.BYTE;
            else if (pcm.bits == 16) type = PrimType.SHORT;
            else throw new IllegalArgumentException("Sound must be 8 or 16 bits.");

            rate = pcm.rate;
            if (rate <= 0)
                throw new IllegalArgumentException("Illegal sound rate specified.");
        }
        catch (Throwable t)
        {
            memFree(pcm.close());
            throw t;
        }
        
        buffer = pcm.close();
        size = buffer.remaining();
        samples = size/(channels*type.size);
    }
    
    /**
     * @return The native memory location for this image buffer. Unsafe!
     */
    public long address()
    {
        return memAddress0(buffer);
    }
    
    @Override
    void delete()
    {
        memFree(buffer);
    }
}
