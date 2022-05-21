package com.samrj.devil.model.nodes;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles variable names for textures and expressions.
 */
class VarNames
{
    private int nameCount;
    Map<String, String> imageNames = new HashMap<>(4);

    String newVarName()
    {
        return "v" + Integer.toString(nameCount++, Character.MAX_RADIX);
    }

    String getImageName(String string)
    {
        String name = imageNames.get(string);
        if (name == null)
        {
            name = "u_image" + Integer.toString(imageNames.size(), Character.MAX_RADIX);
            imageNames.put(string, name);
        }
        return name;
    }
}
