package com.samrj.devil.graphics.model;

import com.samrj.devil.math.Quat;
import com.samrj.devil.math.Util;
import com.samrj.devil.math.Vec3;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * DevilModel inverse kinematics constraint.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class IKConstraint implements Solvable
{
    public final Bone parent, start, end, target, pole;
    public final float poleAngle;
    public final float startSqLen, endSqLen, startLen, endLen, length;
    
    public IKConstraint(DataInputStream in, Bone[] bones) throws IOException
    {
        int boneIndex = in.readInt();
        int targetIndex = in.readInt();
        int poleIndex = in.readInt();
        poleAngle = in.readFloat();
        
        end = bones[boneIndex];
        start = end.getParent();
        parent = start.getParent();
        target = bones[targetIndex];
        pole = bones[poleIndex];
        
        startSqLen = start.tail.squareDist(start.head);
        endSqLen = end.tail.squareDist(start.tail);
        startLen = (float)Math.sqrt(startSqLen);
        endLen = (float)Math.sqrt(endSqLen);
        length = startLen + endLen;
    }
    
    @Override
    public void solve()
    {
        
        
        
        start.solve();
        end.solve();
    }
}
