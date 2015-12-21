package com.samrj.devil.graphics.model;

import com.samrj.devil.io.IOUtil;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Lamp implements DataBlock
{
    public final String name;
    
    Lamp(DataInputStream in) throws IOException
    {
        name = IOUtil.readPaddedUTF(in);
    }

    @Override
    public Type getType()
    {
        return Type.LAMP;
    }
}