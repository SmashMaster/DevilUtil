package com.samrj.devil.al;

import com.samrj.devil.math.Vec3;

import static org.lwjgl.openal.EXTEfx.*;

/**
 * Wrapper for an OpenAL EFX effect object.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2019 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Effect extends DALObj
{
    final int id;
    
    Effect()
    {
        id = alGenEffects();
        DAL.checkError();
    }
    
    public void setType(int type)
    {
        alEffecti(id, AL_EFFECT_TYPE, type);
        DAL.checkError();
    }
    
    public void parami(int param, int value)
    {
        alEffecti(id, param, value);
        DAL.checkError();
    }
    
    public void paramiv(int param, int... values)
    {
        alEffectiv(id, param, values);
        DAL.checkError();
    }
    
    public void paramf(int param, float value)
    {
        alEffectf(id, param, value);
        DAL.checkError();
    }
    
    public void paramfv(int param, float... values)
    {
        alEffectfv(id, param, values);
        DAL.checkError();
    }
    
    public void paramVec3(int param, Vec3 vec)
    {
        paramfv(param, vec.x, vec.y, vec.z);
        DAL.checkError();
    }
    
    @Override
    void delete()
    {
        alDeleteEffects(id);
        DAL.checkError();
    }
}
