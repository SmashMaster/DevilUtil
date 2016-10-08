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
        LIBRARY (Library::new),
        ACTION  (Action::new),
        ARMATURE(Armature::new),
        CURVE   (Curve::new),
        LAMP    (Lamp::new),
        MATERIAL(Material::new),
        MESH    (Mesh::new),
        OBJECT  (ModelObject::new),
        SCENE   (Scene::new),
        TEXTURE (Texture::new);
        
        private final ModelConstructor<?> constructor;
        
        private Type(ModelConstructor<?> constructor)
        {
            this.constructor = constructor;
        }
        
        ArrayMap<?> makeArrayMap(Model model, DataInputStream in) throws IOException
        {
            return new ArrayMap<>(model, in, constructor);
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
