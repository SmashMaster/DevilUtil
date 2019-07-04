package com.samrj.devil.model;

import java.io.IOException;
import org.blender.dna.Tex;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2019 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Texture extends DataBlock
{
    public final String filepath;
    
    Texture(Model model, Tex bTex) throws IOException
    {
        super(model, bTex.getId());
        
        org.blender.dna.Image bImage = bTex.getIma().get();
        filepath = bImage != null ? bImage.getName().asString() : null;
    }
}
