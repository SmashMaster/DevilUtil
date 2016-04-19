package com.samrj.devil.model;

import com.samrj.devil.io.IOUtil;
import com.samrj.devil.math.Vec3;
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
    public final Vec3 color;
    public final float radius;
    public final int type;
    
    Lamp(Model model, DataInputStream in) throws IOException
    {
        name = IOUtil.readPaddedUTF(in);
        color = new Vec3(in);
        radius = in.readFloat();
        type = in.readInt();
    }
    
    @Override
    public String getName()
    {
        return name;
    }
}
