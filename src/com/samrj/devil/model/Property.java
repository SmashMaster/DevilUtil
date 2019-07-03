package com.samrj.devil.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.blender.dna.IDProperty;
import org.blender.dna.IDPropertyData;

/**
 * Allows access to Blender's RNA properties, including custom properties.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2019 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class Property
{
    public static enum Type
    {
        STRING, INT, FLOAT, ARRAY, GROUP, ID, DOUBLE, IDPARRAY, NUMTYPES
    }
    
    public final String name;
    public final Type type;
    public final List<Property> properties = new ArrayList<>();
    
    private Object value;
    
    Property(IDProperty bProp) throws IOException
    {
        name = bProp.getName().asString();
        
        IDPropertyData data = bProp.getData();
        
        switch (bProp.getType())
        {
            case 0:
                type = Type.STRING;
                if (data.getPointer().isValid())
                    value = data.getPointer().cast(Byte.class).toCArrayFacade(bProp.getLen()).asString();
                break;
            case 1:
                type = Type.INT;
                value = data.getVal();
                break;
            case 2:
                type = Type.FLOAT;
                value = Float.intBitsToFloat(data.getVal());
                break;
            case 5:
                type = Type.ARRAY;
                break;
            case 6:
                type = Type.GROUP;
                for (IDProperty subBProp : Blender.blendList(data.getGroup(), IDProperty.class))
                    properties.add(new Property(subBProp));
                break;
            case 7:
                type = Type.ID;
                break;
            case 8:
                type = Type.DOUBLE;
                long bits = (data.getVal() & 0xFFFFFFFFL) | (((long)data.getVal2()) << 32L);
                value = Double.longBitsToDouble(bits);
                break;
            case 9:
                type = Type.IDPARRAY;
                if (data.getPointer().isValid())
                    for (IDProperty subBProp : data.getPointer().cast(IDProperty.class).toArray(bProp.getLen()))
                        properties.add(new Property(subBProp));
                break;
            case 10:
                    type = Type.NUMTYPES;
                break;
            default:
                type = null;
        }
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
