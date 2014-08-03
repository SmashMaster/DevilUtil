package com.samrj.devil.sound.effects;

import com.samrj.devil.sound.EffectType;
import org.lwjgl.openal.EFX10;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Flanger extends EffectType
{
    public int   waveform = EFX10.AL_FLANGER_DEFAULT_WAVEFORM ,
                 phase    = EFX10.AL_FLANGER_DEFAULT_PHASE    ;
    public float rate     = EFX10.AL_FLANGER_DEFAULT_RATE     ,
                 depth    = EFX10.AL_FLANGER_DEFAULT_DEPTH    ,
                 feedback = EFX10.AL_FLANGER_DEFAULT_FEEDBACK ,
                 delay    = EFX10.AL_FLANGER_DEFAULT_DELAY    ;
    
    public Flanger()
    {
        super(EFX10.AL_EFFECT_FLANGER);
    }
    
    @Override
    public void setProps(int id)
    {
        EFX10.alEffecti(id, EFX10.AL_FLANGER_WAVEFORM , waveform );
        EFX10.alEffecti(id, EFX10.AL_FLANGER_PHASE    , phase    );
        EFX10.alEffectf(id, EFX10.AL_FLANGER_RATE     , rate     );
        EFX10.alEffectf(id, EFX10.AL_FLANGER_DEPTH    , depth    );
        EFX10.alEffectf(id, EFX10.AL_FLANGER_FEEDBACK , feedback );
        EFX10.alEffectf(id, EFX10.AL_FLANGER_DELAY    , delay    );
    }
}