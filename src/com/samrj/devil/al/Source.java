package com.samrj.devil.al;

import com.samrj.devil.math.Vec3;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.AL11;
import org.lwjgl.openal.EXTEfx;

/**
 * An OpenAL sound source.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Source extends DALObj
{
    final int id;
    
    Source()
    {
        id = AL10.alGenSources();
    }
    
    public void setSound(Sound sound)
    {
        AL10.alSourcei(id, AL10.AL_BUFFER, sound.id);
    }
    
    public void setPitch(float f)
    {
        AL10.alSourcef(id, AL10.AL_PITCH, f);
    }
    
    public void setGain(float f)
    {
        AL10.alSourcef(id, AL10.AL_GAIN, f);
    }
    
    public void setPos(Vec3 v)
    {
        AL10.alSource3f(id, AL10.AL_POSITION, v.x, v.y, v.z);
    }
    
    public void setVel(Vec3 v)
    {
        AL10.alSource3f(id, AL10.AL_VELOCITY, v.x, v.y, v.z);
    }
    
    public void setLooping(boolean looping)
    {
        AL10.alSourcei(id, AL10.AL_LOOPING, looping ? AL10.AL_TRUE : AL10.AL_FALSE);
    }
    
    public void paramf(int param, float value)
    {
        AL10.alSourcef(id, param, value);
    }
    
    public void parami(int param, int value)
    {
        AL10.alSourcei(id, param, value);
    }
    
    public void setDirectFilter(Filter filter)
    {
        int fid = filter != null ? filter.id : EXTEfx.AL_FILTER_NULL;
        AL10.alSourcei(id, EXTEfx.AL_DIRECT_FILTER, fid);
    }
    
    public void sendToEffectSlot(int localSend, Filter filter, EffectSlot slot)
    {
        int sid = slot != null ? slot.id :  EXTEfx.AL_EFFECTSLOT_NULL;
        int fid = filter != null ? filter.id :  EXTEfx.AL_FILTER_NULL;
        AL11.alSource3i(id, EXTEfx.AL_AUXILIARY_SEND_FILTER, sid, localSend, fid);
    }
    
    public void sendToEffectSlot(int localSend, EffectSlot slot)
    {
        sendToEffectSlot(localSend, null, slot);
    }
    
    public void play()
    {
        AL10.alSourcePlay(id);
    }
    
    public void pause()
    {
        AL10.alSourcePause(id);
    }
    
    public void stop()
    {
        AL10.alSourceStop(id);
    }
    
    /**
     * Returns one of AL_INITIAL, AL_PLAYING, AL_PAUSED, and AL_STOPPED.
     */
    public int getState()
    {
        return AL10.alGetSourcei(id, AL10.AL_SOURCE_STATE);
    }
    
    @Override
    void delete()
    {
        AL10.alDeleteSources(id);
    }
}
