package com.samrj.devil.al;

import com.samrj.devil.math.Vec3;
import org.lwjgl.openal.EXTEfx;

/**
 * Wrapper for an OpenAL EFX effect object.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Effect extends DALObj
{
    final int id;
    
    Effect()
    {
        id = EXTEfx.alGenEffects();
    }
    
    public void setType(int type)
    {
        EXTEfx.alEffecti(id, EXTEfx.AL_EFFECT_TYPE, type);
    }
    
    public void parami(int param, int value)
    {
        EXTEfx.alEffecti(id, param, value);
    }
    
    public void paramiv(int param, int... values)
    {
        EXTEfx.alEffectiv(id, param, values);
    }
    
    public void paramf(int param, float value)
    {
        EXTEfx.alEffectf(id, param, value);
    }
    
    public void paramfv(int param, float... values)
    {
        EXTEfx.alEffectfv(id, param, values);
    }
    
    public void paramVec3(int param, Vec3 vec)
    {
        paramfv(param, vec.x, vec.y, vec.z);
    }
    
    @Override
    void delete()
    {
        EXTEfx.alDeleteEffects(id);
    }
}
