package com.samrj.devil.sound.effects;

import com.samrj.devil.sound.EffectType;
import org.lwjgl.openal.EFX10;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Echo extends EffectType
{
    public float damping  = EFX10.AL_ECHO_DEFAULT_DAMPING  ,
                 delay    = EFX10.AL_ECHO_DEFAULT_DELAY    ,
                 feedback = EFX10.AL_ECHO_DEFAULT_FEEDBACK ,
                 lrdelay  = EFX10.AL_ECHO_DEFAULT_LRDELAY  ,
                 spread   = EFX10.AL_ECHO_DEFAULT_SPREAD   ;
    
    public Echo()
    {
        super(EFX10.AL_EFFECT_ECHO);
    }
    
    @Override
    public void setProps(int id)
    {
        EFX10.alEffectf(id, EFX10.AL_ECHO_DAMPING  ,  damping );
        EFX10.alEffectf(id, EFX10.AL_ECHO_DELAY    ,    delay );
        EFX10.alEffectf(id, EFX10.AL_ECHO_FEEDBACK , feedback );
        EFX10.alEffectf(id, EFX10.AL_ECHO_LRDELAY  ,  lrdelay );
        EFX10.alEffectf(id, EFX10.AL_ECHO_SPREAD   ,   spread );
    }
}