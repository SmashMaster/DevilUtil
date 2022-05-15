package com.samrj.devil.al;

import com.samrj.devil.math.Vec3;

import java.util.HashSet;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.openal.AL11.alSource3i;
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
    
    private final HashSet<Integer> filterSends = new HashSet<>();
    
    Source()
    {
        id = alGenSources();
        DAL.checkError();
    }
    
    public Source setSound(Sound sound)
    {
        int bufferID = sound != null ? sound.id : AL_NONE;
        alSourcei(id, AL_BUFFER, bufferID);
        DAL.checkError();
        return this;
    }
    
    public Source setRelative(boolean relative)
    {
        alSourcei(id, AL_SOURCE_RELATIVE, relative ? AL_TRUE : AL_FALSE);
        DAL.checkError();
        return this;
    }
    
    public Source setPitch(float f)
    {
        alSourcef(id, AL_PITCH, f);
        DAL.checkError();
        return this;
    }
    
    public Source setGain(float f)
    {
        alSourcef(id, AL_GAIN, f);
        DAL.checkError();
        return this;
    }
    
    public Source setPos(Vec3 v)
    {
        alSource3f(id, AL_POSITION, v.x, v.y, v.z);
        DAL.checkError();
        return this;
    }
    
    public Source setVel(Vec3 v)
    {
        alSource3f(id, AL_VELOCITY, v.x, v.y, v.z);
        DAL.checkError();
        return this;
    }
    
    public Source setLooping(boolean looping)
    {
        alSourcei(id, AL_LOOPING, looping ? AL_TRUE : AL_FALSE);
        DAL.checkError();
        return this;
    }
    
    public Source paramf(int param, float value)
    {
        alSourcef(id, param, value);
        DAL.checkError();
        return this;
    }
    
    public Source parami(int param, int value)
    {
        alSourcei(id, param, value);
        DAL.checkError();
        return this;
    }
    
    public Source setDirectFilter(Filter filter)
    {
        int fid = filter != null ? filter.id : AL_FILTER_NULL;
        alSourcei(id, AL_DIRECT_FILTER, fid);
        DAL.checkError();
        return this;
    }
    
    public Source sendToEffectSlot(int localSend, Filter filter, EffectSlot slot)
    {
        int sid = slot != null ? slot.id :  AL_EFFECTSLOT_NULL;
        int fid = filter != null ? filter.id :  AL_FILTER_NULL;
        
        if (slot == null && filter == null) filterSends.remove(localSend);
        else if (slot != null && filter != null) filterSends.add(localSend);
        
        alSource3i(id, AL_AUXILIARY_SEND_FILTER, sid, localSend, fid);
        DAL.checkError();
        return this;
    }
    
    public Source sendToEffectSlot(int localSend, EffectSlot slot)
    {
        return sendToEffectSlot(localSend, null, slot);
    }
    
    public Source play()
    {
        alSourcePlay(id);
        DAL.checkError();
        return this;
    }
    
    public Source pause()
    {
        alSourcePause(id);
        DAL.checkError();
        return this;
    }
    
    public Source stop()
    {
        alSourceStop(id);
        DAL.checkError();
        return this;
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
    
    void detatchAll()
    {
        setDirectFilter(null);
        for (int send : filterSends) sendToEffectSlot(send, null);
        setSound(null);
    }
    
    @Override
    void delete()
    {
        alDeleteSources(id);
        DAL.checkError();
    }
}
