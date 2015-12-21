package com.samrj.devil.graphics.model;

import com.samrj.devil.io.IOUtil;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class IKConstraint
{
    public final String bone;
    public final String target;
    public final String pole;
    public final float poleAngle;
    
    IKConstraint(DataInputStream in) throws IOException
    {
        bone = IOUtil.readPaddedUTF(in);
        target = IOUtil.readPaddedUTF(in);
        pole = IOUtil.readPaddedUTF(in);
        poleAngle = in.readFloat();
    }
}
