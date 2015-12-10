package com.samrj.devil.graphics.model;

import com.samrj.devil.io.IOUtil;
import java.io.DataInputStream;
import java.io.IOException;

public class Pose
{
    public final PoseBone[] bones;
    
    Pose(DataInputStream in) throws IOException
    {
        bones = IOUtil.arrayFromStream(in, PoseBone.class, PoseBone::new);
    }
    
    void populate(Armature armature)
    {
        for (PoseBone bone : bones) bone.populate(armature.bones);
    }
    
    public class PoseBone
    {
        public final Transform transform;
        
        public Bone bone;
        
        private final int boneIndex;
        
        PoseBone(DataInputStream in) throws IOException
        {
            boneIndex = in.readInt();
            transform = new Transform(in);
        }
        
        private void populate(Bone[] bones)
        {
            bone = bones[boneIndex];
        }
    }
}
