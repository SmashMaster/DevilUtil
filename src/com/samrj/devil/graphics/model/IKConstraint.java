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
    public final String boneName;
    public final String targetName;
    public final String poleName;
    public final float poleAngle;
    
    private Bone end, middle, start;
    private Bone target, pole;
    
    IKConstraint(DataInputStream in) throws IOException
    {
        boneName = IOUtil.readPaddedUTF(in);
        targetName = IOUtil.readPaddedUTF(in);
        poleName = IOUtil.readPaddedUTF(in);
        poleAngle = in.readFloat();
    }
    
    void populate(Armature armature)
    {
        end = armature.getBone(boneName);
        middle = end.getParent();
        start = middle.getParent();
        target = armature.getBone(targetName);
        pole = armature.getBone(poleName);
    }
}
