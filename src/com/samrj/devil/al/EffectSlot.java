package com.samrj.devil.al;

import org.lwjgl.openal.EXTEfx;

/**
 * Wrapper for an OpenAL EFX auxiliary effect slot object.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class EffectSlot extends DALObj
{
    final int id;
    
    EffectSlot()
    {
        id = EXTEfx.alGenAuxiliaryEffectSlots();
    }
    
    public void setEffect(Effect effect)
    {
        EXTEfx.alAuxiliaryEffectSloti(id, EXTEfx.AL_EFFECTSLOT_EFFECT, effect.id);
    }
    
    @Override
    void delete()
    {
        EXTEfx.alDeleteAuxiliaryEffectSlots(id);
    }
}
