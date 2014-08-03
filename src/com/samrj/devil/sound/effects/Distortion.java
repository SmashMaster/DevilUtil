package com.samrj.devil.sound.effects;

import com.samrj.devil.sound.EffectType;
import org.lwjgl.openal.EFX10;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Distortion extends EffectType
{
    public float edge          = EFX10.AL_DISTORTION_DEFAULT_EDGE           ,
                 gain          = EFX10.AL_DISTORTION_DEFAULT_GAIN           ,
                 lowpassCutoff = EFX10.AL_DISTORTION_DEFAULT_LOWPASS_CUTOFF ,
                 eqCenter      = EFX10.AL_DISTORTION_DEFAULT_EQCENTER       ,
                 eqBandwidth   = EFX10.AL_DISTORTION_DEFAULT_EQBANDWIDTH    ;

    public Distortion()
    {
        super(EFX10.AL_EFFECT_DISTORTION);
    }
    
    @Override
    public void setProps(int id)
    {
        EFX10.alEffectf(id, EFX10.AL_DISTORTION_EDGE           , edge          );
        EFX10.alEffectf(id, EFX10.AL_DISTORTION_GAIN           , gain          );
        EFX10.alEffectf(id, EFX10.AL_DISTORTION_LOWPASS_CUTOFF , lowpassCutoff );
        EFX10.alEffectf(id, EFX10.AL_DISTORTION_EQCENTER       , eqCenter      );
        EFX10.alEffectf(id, EFX10.AL_DISTORTION_EQBANDWIDTH    , eqBandwidth   );
    }
}