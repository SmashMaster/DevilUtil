package com.samrj.devil.sound.effects;

import com.samrj.devil.sound.EffectType;
import org.lwjgl.openal.EFX10;

public class Chorus extends EffectType
{
    public int   waveform = EFX10.AL_CHORUS_DEFAULT_WAVEFORM ,
                 phase    = EFX10.AL_CHORUS_DEFAULT_PHASE    ;
    public float rate     = EFX10.AL_CHORUS_DEFAULT_RATE     ,
                 depth    = EFX10.AL_CHORUS_DEFAULT_DEPTH    ,
                 feedback = EFX10.AL_CHORUS_DEFAULT_FEEDBACK ,
                 delay    = EFX10.AL_CHORUS_DEFAULT_DELAY    ;
    
    public Chorus()
    {
        super(EFX10.AL_EFFECT_CHORUS);
    }
    
    @Override
    public void setProps(int id)
    {
        EFX10.alEffecti(id, EFX10.AL_CHORUS_WAVEFORM , waveform );
        EFX10.alEffecti(id, EFX10.AL_CHORUS_PHASE    ,    phase );
        EFX10.alEffectf(id, EFX10.AL_CHORUS_RATE     ,     rate );
        EFX10.alEffectf(id, EFX10.AL_CHORUS_DEPTH    ,    depth );
        EFX10.alEffectf(id, EFX10.AL_CHORUS_FEEDBACK , feedback );
        EFX10.alEffectf(id, EFX10.AL_CHORUS_DELAY    ,    delay );
    }
}