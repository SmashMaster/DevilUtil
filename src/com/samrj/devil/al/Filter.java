package com.samrj.devil.al;

import static org.lwjgl.openal.EXTEfx.*;

/**
 * Wrapper for an OpenAL EFX filter object.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Filter extends DALObj
{
    final int id;
    
    Filter()
    {
        id = alGenFilters();
    }
    
    public void setLowPass(float gain, float gainHF)
    {
        alFilteri(id, AL_FILTER_TYPE, AL_FILTER_LOWPASS);
        alFilterf(id, AL_LOWPASS_GAIN, gain);
        alFilterf(id, AL_LOWPASS_GAINHF, gainHF);
    }
    
    public void setBandPass(float gain, float gainLF, float gainHF)
    {
        alFilteri(id, AL_FILTER_TYPE, AL_FILTER_BANDPASS);
        alFilterf(id, AL_BANDPASS_GAIN, gain);
        alFilterf(id, AL_BANDPASS_GAINLF, gainLF);
        alFilterf(id, AL_BANDPASS_GAINHF, gainHF);
    }
    
    public void setHighPass(float gain, float gainLF)
    {
        alFilteri(id, AL_FILTER_TYPE, AL_FILTER_HIGHPASS);
        alFilterf(id, AL_HIGHPASS_GAIN, gain);
        alFilterf(id, AL_HIGHPASS_GAINLF, gainLF);
    }
    
    @Override
    void delete()
    {
        alDeleteFilters(id);
    }
}
