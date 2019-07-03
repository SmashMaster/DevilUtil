package com.samrj.devil.model;

import java.io.IOException;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2019 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class Library extends DataBlock
{
    Library(Model model, org.blender.dna.Library bLib) throws IOException
    {
        super(model, bLib.getId());
        
        //Placeholder
    }
}
