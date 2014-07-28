package com.samrj.devil.sound.effects;

import com.samrj.devil.sound.EffectType;
import org.lwjgl.openal.EFX10;

public class Reverb extends EffectType
{
    public float density             = EFX10.AL_REVERB_DEFAULT_DENSITY               ,
                 diffusion           = EFX10.AL_REVERB_DEFAULT_DIFFUSION             ,
                 gain                = EFX10.AL_REVERB_DEFAULT_GAIN                  ,
                 gainHF              = EFX10.AL_REVERB_DEFAULT_GAINHF                ,
                 decayTime           = EFX10.AL_REVERB_DEFAULT_DECAY_TIME            ,
                 decayHFRatio        = EFX10.AL_REVERB_DEFAULT_DECAY_HFRATIO         ,
                 reflectGain         = EFX10.AL_REVERB_DEFAULT_REFLECTIONS_GAIN      ,
                 reflectDelay        = EFX10.AL_REVERB_DEFAULT_REFLECTIONS_DELAY     ,
                 lateReverbGain      = EFX10.AL_REVERB_DEFAULT_LATE_REVERB_GAIN      ,
                 lateReverbDelay     = EFX10.AL_REVERB_DEFAULT_LATE_REVERB_DELAY     ,
                 airAbsorptionGainHF = EFX10.AL_REVERB_DEFAULT_AIR_ABSORPTION_GAINHF ,
                 roomRolloffFactor   = EFX10.AL_REVERB_DEFAULT_ROOM_ROLLOFF_FACTOR   ;
    public int   decayHFLimit        = EFX10.AL_REVERB_DEFAULT_DECAY_HFLIMIT         ;
    
    Reverb(int type)
    {
        super(type);
    }
    
    public Reverb()
    {
        super(EFX10.AL_EFFECT_REVERB);
    }
    
    @Override
    public void setProps(int id)
    {
        EFX10.alEffectf(id, EFX10.AL_REVERB_DENSITY               , density             );
        EFX10.alEffectf(id, EFX10.AL_REVERB_DIFFUSION             , diffusion           );
        EFX10.alEffectf(id, EFX10.AL_REVERB_GAIN                  , gain                );
        EFX10.alEffectf(id, EFX10.AL_REVERB_GAINHF                , gainHF              );
        EFX10.alEffectf(id, EFX10.AL_REVERB_DECAY_TIME            , decayTime           );
        EFX10.alEffectf(id, EFX10.AL_REVERB_DECAY_HFRATIO         , decayHFRatio        );
        EFX10.alEffectf(id, EFX10.AL_REVERB_REFLECTIONS_GAIN      , reflectGain         );
        EFX10.alEffectf(id, EFX10.AL_REVERB_REFLECTIONS_DELAY     , reflectDelay        );
        EFX10.alEffectf(id, EFX10.AL_REVERB_LATE_REVERB_GAIN      , lateReverbGain      );
        EFX10.alEffectf(id, EFX10.AL_REVERB_LATE_REVERB_DELAY     , lateReverbDelay     );
        EFX10.alEffectf(id, EFX10.AL_REVERB_AIR_ABSORPTION_GAINHF , airAbsorptionGainHF );
        EFX10.alEffectf(id, EFX10.AL_REVERB_ROOM_ROLLOFF_FACTOR   , roomRolloffFactor   );
        EFX10.alEffecti(id, EFX10.AL_REVERB_DECAY_HFLIMIT         , decayHFLimit        );
    }
}