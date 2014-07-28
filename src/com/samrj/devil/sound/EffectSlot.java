package com.samrj.devil.sound;

import org.lwjgl.openal.EFX10;

public class EffectSlot
{
    private int id;
    
    public EffectSlot()
    {
        id = EFX10.alGenAuxiliaryEffectSlots();
    }
    
    public int id()
    {
        return id;
    }
    
    public void setEffect(Effect effect)
    {
        EFX10.alAuxiliaryEffectSloti(id, EFX10.AL_EFFECTSLOT_EFFECT, effect.id());
    }
    
    public void removeEffect()
    {
        EFX10.alAuxiliaryEffectSloti(id, EFX10.AL_EFFECTSLOT_EFFECT, EFX10.AL_EFFECT_NULL);
    }
    
    public void delete()
    {
        EFX10.alDeleteAuxiliaryEffectSlots(id);
        id = -1;
    }
}