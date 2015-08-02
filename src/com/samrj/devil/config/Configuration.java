package com.samrj.devil.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Locale;
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
    
    public Configuration()
    {
        fields = new HashMap<>();
    }
    
    public Configuration(Configuration config)
    {
        fields = new HashMap<>(config.fields.size());
        
        for (Entry<String, CfgField> entry : config.fields.entrySet())
            fields.put(entry.getKey(), entry.getValue().copy());
    }
    
    /**
     * Registers the given field under the given name. The default value of the
     * field, if a config file does not explicitly override it, will be the
     * value of the given field.
     * 
     * @param name The name of the field to register.
     * @param field The field type and value to register.
     */
    public void addField(String name, CfgField field)
    {
        if (name == null || field == null) throw new NullPointerException();
        
        if (fields.containsKey(name)) throw new IllegalArgumentException(
                "Field name '" + name + "' already registered.");
        
        CfgField fieldCopy = field.copy();
        
        if (fieldCopy == field || fieldCopy.getClass() != field.getClass())
            throw new IllegalArgumentException(
                "Given field does not implement copy() correctly.");
        
        fields.put(name, fieldCopy);
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
    
    public void load(InputStream in) throws IOException
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        
        while (reader.ready())
        {
            String line = reader.readLine();
            int commentIndex = line.indexOf("//"); //Ignore comments
            if (commentIndex >= 0) line = line.substring(0, commentIndex);
            line = line.replaceAll("\\s+", ""); //Remove all whitespace
            line = line.toLowerCase(Locale.ENGLISH);
            
            String[] split = line.split(":");
            if (split.length != 2) continue;
            
            try
            {
                CfgField field = fields.get(split[0]);
                if (field != null) field.load(split[1]);
            }
            catch (NumberFormatException e) {}
        }
        
        reader.close();
    }
    
    public float getFloat(String name)
    {
        return ((CfgFloat)getField(name)).value;
    }
    
    public boolean getBoolean(String name)
    {
        return ((CfgBoolean)getField(name)).value;
    }
    
    public int getInt(String name)
    {
        return ((CfgInteger)getField(name)).value;
    }
}
