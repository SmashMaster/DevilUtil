package com.samrj.devil.graphics.model;

import java.io.DataInputStream;
import java.io.IOException;

public class IKConstraint
{
    public final Bone end, start, target, poleTarget;
    
    public IKConstraint(DataInputStream in, Bone[] bones) throws IOException
    {
        int boneIndex = in.readInt();
        int targetIndex = in.readInt();
        int poleTargetIndex = in.readInt();
        
        end = bones[boneIndex];
        start = end.getParent();
        target = bones[targetIndex];
        poleTarget = bones[poleTargetIndex];
    }
}
