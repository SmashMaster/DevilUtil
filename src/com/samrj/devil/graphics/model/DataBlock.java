package com.samrj.devil.graphics.model;

public interface DataBlock
{
    public enum Type
    {
        UNKNOWN, ACTION, ARMATURE, LAMP, MATERIAL, MESH, OBJECT, SCENE;
    }
    
    static Type getTypeFromID(int typeID)
    {
        switch (typeID)
        {
            case 0: return Type.ACTION;
            case 1: return Type.ARMATURE;
            case 2: return Type.LAMP;
            case 3: return Type.MATERIAL;
            case 4: return Type.MESH;
            default: return Type.UNKNOWN;
        }
    }
    
    Type getType();
}
