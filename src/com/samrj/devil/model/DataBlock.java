package com.samrj.devil.model;

import com.samrj.devil.io.IOUtil;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public abstract class DataBlock
{
    public static Type getType(int id)
    {
        switch (id)
        {
            case 0: return Type.LIBRARY;
            case 1: return Type.ACTION;
            case 2: return Type.ARMATURE;
            case 3: return Type.CURVE;
            case 4: return Type.LAMP;
            case 5: return Type.MESH;
            case 6: return Type.SCENE;
            default: return null;
        }
    }
    
    public enum Type
    {
        LIBRARY(Library.class), ACTION(Action.class), ARMATURE(Armature.class),
        CURVE(Curve.class), LAMP(Lamp.class), MESH(Mesh.class), SCENE(Scene.class),
        OBJECT(ModelObject.class);
            
        public final Class<? extends DataBlock> type;
        
        private Type(Class<? extends DataBlock> type)
        {
            this.type = type;
        }
    }
    
    public final Model model;
    public final String name;
    
    DataBlock(Model model, DataInputStream in) throws IOException
    {
        this.model = model;
        name = IOUtil.readPaddedUTF(in);
    }
}
