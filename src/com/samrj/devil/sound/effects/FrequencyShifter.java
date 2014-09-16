package com.samrj.devil.sound.effects;

import com.samrj.devil.sound.EffectType;
import org.lwjgl.openal.EFX10;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class FrequencyShifter extends EffectType
{
    public float frequency      = EFX10.AL_FREQUENCY_SHIFTER_DEFAULT_FREQUENCY       ;
    public int   leftDirection  = EFX10.AL_FREQUENCY_SHIFTER_DEFAULT_LEFT_DIRECTION  ,
                 rightDirection = EFX10.AL_FREQUENCY_SHIFTER_DEFAULT_RIGHT_DIRECTION ;
    
    public FrequencyShifter()
    {
        super(EFX10.AL_EFFECT_FREQUENCY_SHIFTER);
    }
    
    @Override
    public void setProps(int id)
    {
        EFX10.alEffectf(id, EFX10.AL_FREQUENCY_SHIFTER_FREQUENCY       , frequency      );
        EFX10.alEffecti(id, EFX10.AL_FREQUENCY_SHIFTER_LEFT_DIRECTION  , leftDirection  );
        EFX10.alEffecti(id, EFX10.AL_FREQUENCY_SHIFTER_RIGHT_DIRECTION , rightDirection );
    }
}
