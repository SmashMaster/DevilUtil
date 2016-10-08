package com.samrj.devil.model;

import com.samrj.devil.io.IOUtil;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2016 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Texture extends DataBlock
{
    public final String filepath;
    
    Texture(Model model, DataInputStream in) throws IOException
    {
        super(model, in);
        
        if (in.readInt() != 0) filepath = IOUtil.readPaddedUTF(in);
        else filepath = null;
    }
}
