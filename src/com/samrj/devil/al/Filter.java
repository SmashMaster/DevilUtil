package com.samrj.devil.al;

import static org.lwjgl.openal.EXTEfx.*;

/**
 * Wrapper for an OpenAL EFX filter object.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2019 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Filter extends DALObj
{
    final int id;
    
    Filter()
    {
        id = alGenFilters();
        DAL.checkError();
    }
    
    public void setLowPass(float gain, float gainHF)
    {
        alFilteri(id, AL_FILTER_TYPE, AL_FILTER_LOWPASS);
        DAL.checkError();
        alFilterf(id, AL_LOWPASS_GAIN, gain);
        DAL.checkError();
        alFilterf(id, AL_LOWPASS_GAINHF, gainHF);
        DAL.checkError();
    }
    
    public void setBandPass(float gain, float gainLF, float gainHF)
    {
        alFilteri(id, AL_FILTER_TYPE, AL_FILTER_BANDPASS);
        DAL.checkError();
        alFilterf(id, AL_BANDPASS_GAIN, gain);
        DAL.checkError();
        alFilterf(id, AL_BANDPASS_GAINLF, gainLF);
        DAL.checkError();
        alFilterf(id, AL_BANDPASS_GAINHF, gainHF);
        DAL.checkError();
    }
    
    public void setHighPass(float gain, float gainLF)
    {
        alFilteri(id, AL_FILTER_TYPE, AL_FILTER_HIGHPASS);
        DAL.checkError();
        alFilterf(id, AL_HIGHPASS_GAIN, gain);
        DAL.checkError();
        alFilterf(id, AL_HIGHPASS_GAINLF, gainLF);
        DAL.checkError();
    }
    
    @Override
    void delete()
    {
        alDeleteFilters(id);
        DAL.checkError();
    }
}
