package com.samrj.devil.sound;

public abstract class EffectType
{
    public final int type;

    public EffectType(int type)
    {
        this.type = type;
    }

    public abstract void setProps(int id);
}