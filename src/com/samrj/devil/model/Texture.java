package com.samrj.devil.model;

import java.io.IOException;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2019 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Texture extends DataBlock
{
    public final String filepath;
    
    Texture(Model model, BlendFile.Pointer bTex) throws IOException
    {
        super(model, bTex);
        
        BlendFile.Pointer bImage = bTex.getField("ima").dereference();
        filepath = bImage != null ? bImage.getField("name").asString() : null;
    }
}
