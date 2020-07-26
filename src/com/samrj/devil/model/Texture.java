package com.samrj.devil.model;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2020 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Texture extends DataBlock
{
    public final Path relativePath;
    public final Path path;
    
    Texture(Model model, BlendFile.Pointer bTex) throws IOException
    {
        super(model, bTex);
        
        BlendFile.Pointer bImage = bTex.getField("ima").dereference();
        
        FIND_PATH: //Path must be null unless all conditions are correct.
        {
            if (bImage != null)
            {
                String str = bImage.getField("name").asString();
                if (str.startsWith("//")) //Only support relative paths.
                {
                    relativePath = Paths.get(str.substring(2)).normalize();
                    path = model.path.getParent().resolve(relativePath).normalize();
                    break FIND_PATH;
                }
            }
            
            relativePath = null;
            path = null;
        }
    }
}
