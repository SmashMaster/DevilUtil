package com.samrj.devil.model;

import com.samrj.devil.model.BlendFile.Pointer;

import java.util.Collections;
import java.util.List;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2022 Samuel Johnson
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
        COLLECTION,
        SCENE,
        IMAGE,
        TEXTURE;
    }
    
    public static Type getType(int index)
    {
        return index >= 0 ? Type.values()[index] : null;
    }
    
    public final Model model;
    public final String name;
    public final List<Property> properties;
    
    DataBlock(Model model, BlendFile.Pointer pointer)
    {
        if (model == null || pointer == null) throw new NullPointerException();
        this.model = model;
        
        Pointer id = pointer.getField(0);
        name = id.getField("name").asString().substring(2);
        
        BlendFile.Pointer bProp = id.getField("properties").dereference();
        properties = bProp != null ? new Property(bProp).properties : Collections.emptyList();
    }

    public final List<Property> getProperties()
    {
        return Collections.unmodifiableList(properties);
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
