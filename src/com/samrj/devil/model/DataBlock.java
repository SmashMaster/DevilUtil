package com.samrj.devil.model;

import com.samrj.devil.model.BlendFile.Pointer;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import org.blender.dna.ID;
import org.blender.dna.IDProperty;

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
    public final List<Property> properties;
    
    DataBlock(Model model, ID bID) throws IOException
    {
        if (model == null || bID == null) throw new NullPointerException();
        this.model = model;
        name = bID.getName().asString().substring(2);
        
        IDProperty bProp = bID.getProperties().get();
        properties = bProp != null ? new Property(bProp).properties : Collections.emptyList();
    }
    
    DataBlock(Model model, BlendFile.Pointer pointer) throws IOException
    {
        if (model == null || pointer == null) throw new NullPointerException();
        this.model = model;
        
        Pointer id = pointer.getField(0);
        name = id.getField("name").asString().substring(2);
        
        BlendFile.Pointer bProp = id.getField("properties").dereference();
        properties = bProp != null ? new Property(bProp).properties : Collections.emptyList();
    }
    
    public final Property getProperty(String name)
    {
        for (Property property : properties)
            if (name.equals(property.name))
                return property;
        return null;
    }
    
    public final List<Property> getSubproperties(String name)
    {
        Property prop = getProperty(name);
        return prop != null ? prop.properties : Collections.emptyList();
    }
    
    void destroy()
    {
    }
}
