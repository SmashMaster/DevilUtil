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
    
    public Pose(Armature armature)
    {
        bones = new PoseBone[armature.bones.length];
        for (int i=0; i<bones.length; i++)
            bones[i] = new PoseBone(armature.bones[i]);
    }
    
    void populate(Armature armature)
    {
        for (PoseBone bone : bones)
            bone.bone = armature.bones[bone.boneIndex];
    }
    
    public void apply()
    {
        for (PoseBone bone : bones)
            bone.bone.transform.set(bone.transform);
    }
    
    public class PoseBone
    {
        public final Transform transform;
        
        private final int boneIndex;
        
        private Bone bone;
        
        private PoseBone(DataInputStream in) throws IOException
        {
            boneIndex = in.readInt();
            transform = new Transform(in);
        }
        
        private PoseBone(Bone bone)
        {
            boneIndex = -1;
            transform = new Transform();
            this.bone = bone;
        }
        
        public Bone getBone()
        {
            return bone;
        }
    }
}
