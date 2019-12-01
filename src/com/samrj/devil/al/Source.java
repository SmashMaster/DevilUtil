package com.samrj.devil.al;

import com.samrj.devil.math.Vec3;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.openal.AL11.*;
import static org.lwjgl.openal.EXTEfx.*;

/**
 * An OpenAL sound source.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2019 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Source extends DALObj
{
    final int id;
    
    Source()
    {
        id = alGenSources();
        DAL.checkError();
    }
    
    public void setSound(Sound sound)
    {
        int bufferID = sound != null ? sound.id : AL_NONE;
        alSourcei(id, AL_BUFFER, bufferID);
        DAL.checkError();
    }
    
    public void setPitch(float f)
    {
        alSourcef(id, AL_PITCH, f);
        DAL.checkError();
    }
    
    public void setGain(float f)
    {
        alSourcef(id, AL_GAIN, f);
        DAL.checkError();
    }
    
    public void setPos(Vec3 v)
    {
        alSource3f(id, AL_POSITION, v.x, v.y, v.z);
        DAL.checkError();
    }
    
    public void setVel(Vec3 v)
    {
        alSource3f(id, AL_VELOCITY, v.x, v.y, v.z);
        DAL.checkError();
    }
    
    public void setLooping(boolean looping)
    {
        alSourcei(id, AL_LOOPING, looping ? AL_TRUE : AL_FALSE);
        DAL.checkError();
    }
    
    public void paramf(int param, float value)
    {
        alSourcef(id, param, value);
        DAL.checkError();
    }
    
    public void parami(int param, int value)
    {
        alSourcei(id, param, value);
        DAL.checkError();
    }
    
    public void setDirectFilter(Filter filter)
    {
        int fid = filter != null ? filter.id : AL_FILTER_NULL;
        alSourcei(id, AL_DIRECT_FILTER, fid);
        DAL.checkError();
    }
    
    public void sendToEffectSlot(int localSend, Filter filter, EffectSlot slot)
    {
        int sid = slot != null ? slot.id :  AL_EFFECTSLOT_NULL;
        int fid = filter != null ? filter.id :  AL_FILTER_NULL;
        alSource3i(id, AL_AUXILIARY_SEND_FILTER, sid, localSend, fid);
        DAL.checkError();
    }
    
    public void sendToEffectSlot(int localSend, EffectSlot slot)
    {
        sendToEffectSlot(localSend, null, slot);
    }
    
    public void play()
    {
        alSourcePlay(id);
        DAL.checkError();
    }
    
    public void pause()
    {
        alSourcePause(id);
        DAL.checkError();
    }
    
    public void stop()
    {
        alSourceStop(id);
        DAL.checkError();
    }
    
    /**
     * Returns one of AL_INITIAL, AL_PLAYING, AL_PAUSED, and AL_STOPPED.
     */
    public int getState()
    {
        int state = alGetSourcei(id, AL_SOURCE_STATE);
        DAL.checkError();
        return state;
    }
    
    @Override
    void delete()
    {
        alDeleteSources(id);
        DAL.checkError();
    }
}
