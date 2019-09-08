package com.samrj.devil.model;

import java.io.IOException;
import java.nio.file.Path;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2019 Samuel Johnson
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
        if (bImage != null)
        {
            relativePath = Path.of(bImage.getField("name").asString().substring(2)).normalize();
            path = model.path.getParent().resolve(relativePath).normalize();
        }
        else
        {
            relativePath = null;
            path = null;
        }
    }
}
