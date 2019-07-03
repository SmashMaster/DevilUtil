package com.samrj.devil.model;

import java.io.IOException;
import org.blender.dna.Tex;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2016 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Texture extends DataBlock
{
    Texture(Model model, Tex bTex) throws IOException
    {
        super(model, bTex.getId());
    }
}
