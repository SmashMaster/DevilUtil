package com.samrj.devil.al;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.openal.EXTEfx.*;

/**
 * Wrapper for an OpenAL EFX auxiliary effect slot object.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2019 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class EffectSlot extends DALObj
{
    final int id;
    
    EffectSlot()
    {
        id = alGenAuxiliaryEffectSlots();
        DAL.checkError();
    }
    
    public void setEffect(Effect effect)
    {
        int effectID = effect != null ? effect.id : AL_NONE;
        alAuxiliaryEffectSloti(id, AL_EFFECTSLOT_EFFECT, effectID);
        DAL.checkError();
    }
    
    @Override
    void delete()
    {
        alDeleteAuxiliaryEffectSlots(id);
        DAL.checkError();
    }
}
