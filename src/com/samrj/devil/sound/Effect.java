package com.samrj.devil.sound;

import org.lwjgl.openal.EFX10;

public final class Effect
{
    private int id;
    
    public Effect(EffectType type)
    {
        if (type == null) throw new IllegalArgumentException();
        
        id = EFX10.alGenEffects();
        setType(type);
    }
    
    public void setType(EffectType def)
    {
        EFX10.alEffecti(id, EFX10.AL_EFFECT_TYPE, def.type);
        def.setProps(id);
    }
    
    public int id()
    {
        return id;
    }
    
    public void glDelete()
    {
        EFX10.alDeleteEffects(id);
        id = -1;
    }
}