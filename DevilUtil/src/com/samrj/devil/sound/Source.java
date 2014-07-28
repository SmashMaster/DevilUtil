package com.samrj.devil.sound;

import com.samrj.devil.math.Vector3f;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.AL11;
import org.lwjgl.openal.EFX10;

public class Source
{
    private int id;
    
    public Source(Sound sound)
    {
        id = AL10.alGenSources();
        AL10.alSourcei(id, AL10.AL_BUFFER, sound.id());
        AL10.alSourcef(id, AL10.AL_PITCH, 1f);
        AL10.alSourcef(id, AL10.AL_GAIN, 1f);
        AL10.alSource3f(id, AL10.AL_POSITION, 0f, 0f, 0f);
        AL10.alSource3f(id, AL10.AL_VELOCITY, 0f, 0f, 0f);
        AL10.alSourcei(id, AL10.AL_LOOPING, AL10.AL_TRUE);
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
    
    public void rewind()
    {
        AL10.alSourceRewind(id);
    }
    
    public void setSound(Sound sound)
    {
        AL10.alSourcei(id, AL10.AL_BUFFER, sound.id());
    }
    
    public void setEffectSlot(EffectSlot slot)
    {
        AL11.alSource3i(id, EFX10.AL_AUXILIARY_SEND_FILTER, slot.id(), 0, EFX10.AL_FILTER_NULL);
    }
    
    public void removeEffectSlot()
    {
        AL11.alSource3i(id, EFX10.AL_AUXILIARY_SEND_FILTER, EFX10.AL_EFFECTSLOT_NULL, 0, EFX10.AL_FILTER_NULL);
    }
    
    public void setPos(Vector3f pos)
    {
        AL10.alSource3f(id, AL10.AL_POSITION, pos.x, pos.y, pos.z);
    }
    
    public void delete()
    {
        AL10.alDeleteSources(id);
        id = -1;
    }
}