package com.samrj.devil.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Loaded configuration file class.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class Configuration
{
    private final HashMap<String, CfgField> fields;
    
    Configuration(Map<String, CfgField> fields)
    {
        this.fields = new HashMap<>(fields.size());
        
        for (Entry<String, CfgField> entry : fields.entrySet())
            this.fields.put(entry.getKey(), entry.getValue().copy());
    }
    
    /**
     * Returns the field corresponding with the given name, and automatically
     * casts to the requested type. Throws an exception if the name is not
     * registered, or the field cannot be cast to the requested type.
     * 
     * The returned field object is unique, and backs this configuration, so any
     * changes to it will propagate.
     * 
     * @param <T> The type of config field to cast to.
     * @param name The name of the field.
     * @return The field corresponding with the given name.
     */
    public <T extends CfgField> T getField(String name)
    {
        CfgField field = fields.get(name);
        if (field == null) throw new IllegalArgumentException("No such field '" + name + '\'');
        return (T)field;
    }
    
    CfgField getFieldSilent(String name)
    {
        return fields.get(name);
    }
}
