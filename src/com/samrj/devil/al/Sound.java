package com.samrj.devil.al;

import com.samrj.devil.math.Util.PrimType;

import static org.lwjgl.openal.AL10.*;

/**
 * Contains sound data for use by OpenAL.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2019 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Sound extends DALObj
{
    static final int getFormat(int channels, PrimType type)
    {
        if (type == PrimType.BYTE)
        {
            if (channels == 1) return AL_FORMAT_MONO8;
            else if (channels == 2) return AL_FORMAT_STEREO8;
        }
        else if (type == PrimType.SHORT)
        {
            if (channels == 1) return AL_FORMAT_MONO16;
            else if (channels == 2) return AL_FORMAT_STEREO16;
        }
        
        return -1;
    }
    
    final int id;
    
    Sound()
    {
        id = alGenBuffers();
        DAL.checkError();
    }
    
    public void buffer(SoundBuffer buffer)
    {
        int format = getFormat(buffer.channels, buffer.type);
        if (format == -1) throw new IllegalArgumentException("Illegal sound format.");
        
        alBufferData(id, format, buffer.buffer, buffer.rate);
        DAL.checkError();
    }
    
    public int getFrequency()
    {
        int frequency = alGetBufferi(id, AL_FREQUENCY);
        DAL.checkError();
        return frequency;
    }
    
    public int getSize()
    {
        int size = alGetBufferi(id, AL_SIZE);
        DAL.checkError();
        return size;
    }
    
    public int getBits()
    {
        int bits = alGetBufferi(id, AL_BITS);
        DAL.checkError();
        return bits;
    }
    
    public int getChannels()
    {
        int channels = alGetBufferi(id, AL_CHANNELS);
        DAL.checkError();
        return channels;
    }
    
    @Override
    void delete()
    {
        alDeleteBuffers(id);
        DAL.checkError();
    }
}
