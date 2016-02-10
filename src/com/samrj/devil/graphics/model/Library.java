package com.samrj.devil.graphics.model;

import com.samrj.devil.io.IOUtil;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Library implements DataBlock
{
    public final String filepath;
    
    Library(DataInputStream in) throws IOException
    {
        filepath = IOUtil.readPaddedUTF(in);
    }
    
    @Override
    public String getName()
    {
        return filepath;
    }
}
