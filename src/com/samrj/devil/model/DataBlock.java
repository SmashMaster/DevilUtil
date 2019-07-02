package com.samrj.devil.model;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2019 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public abstract class DataBlock
{
    public enum Type
    {
        LIBRARY,
        ACTION,
        ARMATURE,
        CURVE,
        LAMP,
        MATERIAL,
        MESH,
        OBJECT,
        SCENE,
        TEXTURE;
    }
    
    public static Type getType(int index)
    {
        return index >= 0 ? Type.values()[index] : null;
    }
    
    public final Model model;
    public final String name;
    
    DataBlock(Model model, String name)
    {
        if (model == null || name == null) throw new NullPointerException();
        this.model = model;
        this.name = name;
    }
    
    void destroy()
    {
    }
}
