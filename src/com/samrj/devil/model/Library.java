package com.samrj.devil.model;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class Library extends DataBlock
{
    Library(Model model, int modelIndex, DataInputStream in) throws IOException
    {
        super(model, modelIndex, in);
    }
}
