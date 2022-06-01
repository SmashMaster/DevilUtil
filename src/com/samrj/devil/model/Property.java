package com.samrj.devil.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Allows access to Blender's RNA properties, including custom properties.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2022 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class Property
{
    public enum Type
    {
        STRING, INT, FLOAT, ARRAY, GROUP, ID, DOUBLE, IDPARRAY, NUMTYPES
    }

    public final String name;
    public final Type type;
    public final List<Property> properties;
    
    private Object value;
    
    Property(BlendFile.Pointer bProp)
    {
        name = bProp.getField("name").asString();
        
        BlendFile.Pointer data = bProp.getField("data");
        BlendFile.Pointer pointer = data.getField("pointer").dereference();
        int val = data.getField("val").asInt();
        int val2 = data.getField("val2").asInt();

        ArrayList<Property> propsList = new ArrayList<>();
        
        switch (bProp.getField("type").asByte())
        {
            case 0:
                type = Type.STRING;
                if (pointer != null)
                    value = pointer.asString();
                break;
            case 1:
                type = Type.INT;
                value = val;
                break;
            case 2:
                type = Type.FLOAT;
                value = Float.intBitsToFloat(val);
                break;
            case 5:
                type = Type.ARRAY;
                break;
            case 6:
                type = Type.GROUP;
                for (BlendFile.Pointer subBProp : data.getField("group").asList("IDProperty"))
                    propsList.add(new Property(subBProp));
                break;
            case 7:
                type = Type.ID;
                break;
            case 8:
                type = Type.DOUBLE;
                long bits = (val & 0xFFFFFFFFL) | (((long)val2) << 32L);
                value = Double.longBitsToDouble(bits);
                break;
            case 9:
                type = Type.IDPARRAY;
                if (pointer != null)
                {
                    int len = bProp.getField("len").asInt();
                    pointer = pointer.cast("IDProperty");
                    for (int i=0; i<len; i++)
                        propsList.add(new Property(pointer.getElement(i)));
                }
                break;
            case 10:
                    type = Type.NUMTYPES;
                break;
            default:
                type = null;
        }

        properties = Collections.unmodifiableList(propsList);
    }
    
    public String getString()
    {
        return type == Type.STRING ? (String)value : null;
    }
    
    public Integer getInteger()
    {
        return type == Type.INT ? (Integer)value : null;
    }
    
    public Double getDouble()
    {
        if (value == null) return null;
        else if (type == Type.INT) return ((Integer)value).doubleValue();
        else if (type == Type.FLOAT) return ((Float)value).doubleValue();
        else if (type == Type.DOUBLE) return (Double)value;
        else return null;
    }
    
    public Property getProperty(String name)
    {
        for (Property property : properties)
            if (name.equals(property.name))
                return property;
        return null;
    }
    
    public List<Property> getSubproperties(String name)
    {
        Property prop = getProperty(name);
        return prop != null ? prop.properties : Collections.emptyList();
    }
    
    @Override
    public String toString()
    {
        return "[Name: '" + name + "', Type: " + type + ", Value: " + value + ", Children: " + properties.size() + "]";
    }
}
