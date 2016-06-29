package com.samrj.devil.model;

import com.samrj.devil.io.IOUtil;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2016 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public abstract class DataBlock
{
    public enum Type
    {
        LIBRARY (1112276993, Library::new),
        ACTION  (1112276994, Action::new),
        ARMATURE(1112276995, Armature::new),
        CURVE   (1112276996, Curve::new),
        LAMP    (1112276997, Lamp::new),
        MATERIAL(1112276998, Material::new),
        MESH    (1112276999, Mesh::new),
        OBJECT  (1112277000, ModelObject::new),
        SCENE   (1112277001, Scene::new);
        
        private final int magic;
        private final ModelConstructor<? extends DataBlock> constructor;
        
        private Type(int magic, ModelConstructor<? extends DataBlock> constructor)
        {
            this.magic = magic;
            this.constructor = constructor;
        }
        
        ArrayMap<?> makeArrayMap(Model model, DataInputStream in) throws IOException
        {
            return new ArrayMap<>(model, in, magic, constructor);
        }
    }
    
    public static Type getType(int index)
    {
        return index >= 0 ? Type.values()[index] : null;
    }
    
    public final Model model;
    public final String name;
    
    DataBlock(Model model, DataInputStream in) throws IOException
    {
        this.model = model;
        name = IOUtil.readPaddedUTF(in);
    }
    
    void destroy()
    {
    }
}
