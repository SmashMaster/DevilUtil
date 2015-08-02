package com.samrj.devil.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Locale;

/**
 * Configuration file loader. Keeps track of registered fields, composed of a
 * name, type, and default value.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class CfgLoader
{
    private final HashMap<String, CfgField> fields;
    
    public CfgLoader()
    {
        fields = new HashMap<>();
    }
    
    /**
     * Registers the given field under the given name. The default value of the
     * field, if a config file does not explicitly override it, will be the
     * value of the given field.
     * 
     * @param name The name of the field to register.
     * @param field The field type and value to register.
     */
    public void registerField(String name, CfgField field)
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
     * Loads a configuration file from the given input stream. Ignores comments,
     * and silently fails on malformed configuration lines. Any field not
     * explicitly overriden by the given file will have its default value.
     * 
     * @param in The input stream to read the config file from.
     * @return A new configuration object.
     */
    public Configuration load(InputStream in) throws IOException
    {
        Configuration config = new Configuration(fields);
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
                CfgField field = config.getFieldSilent(split[0]);
                if (field != null) field.load(split[1]);
            }
            catch (NumberFormatException e) {}
        }
        
        reader.close();
        return config;
    }
    
    /**
     * @return The default configuration for this loader.
     */
    public Configuration loadDefault()
    {
        return new Configuration(fields);
    }
}
