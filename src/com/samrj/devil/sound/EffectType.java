package com.samrj.devil.sound;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public abstract class EffectType
{
    public final int type;

    public EffectType(int type)
    {
        this.type = type;
    }

    public abstract void setProps(int id);
}
