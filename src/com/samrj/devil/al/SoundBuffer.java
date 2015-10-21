package com.samrj.devil.al;

import com.samrj.devil.io.Memory;
import com.samrj.devil.math.Util.PrimType;
import java.nio.ByteBuffer;

/**
 * Sound buffer. Stores raw sound data.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class SoundBuffer extends DALObj
{
    public final int samples;
    public final int channels;
    public final PrimType type;
    public final int rate;
    public final int size;
    
    private final Memory mem;
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
        catch (IllegalArgumentException e)
        {
            pcm.close().free();
            throw e;
        }
        
        mem = pcm.close();
        buffer = mem.buffer;
        
        size = buffer.position();
        samples = size/(channels*type.size);
    }
    
    /**
     * @return The native memory location for this image buffer. Unsafe!
     */
    public long address()
    {
        return mem.address;
    }
    
    @Override
    void delete()
    {
        mem.free();
    }
}
