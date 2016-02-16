package com.samrj.devil.graphics.model;

import com.samrj.devil.graphics.model.Transform.Property;
import com.samrj.devil.io.IOUtil;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Pose
{
    public final Map<String, PoseBone> bones;
    
    Pose(DataInputStream in) throws IOException
    {
        int numBones = in.readInt();
        bones = new HashMap<>(numBones);
        for (int i=0; i<numBones; i++)
        {
            PoseBone bone = new PoseBone(in);
            bones.put(bone.name, bone);
        }
    }
    
    Pose(Set<String> names)
    {
        bones = new HashMap<>(names.size());
        for (String name : names) bones.put(name, new PoseBone(name));
    }
    
    public Pose(Pose pose)
    {
        bones = new HashMap<>(pose.bones.size());
        for (PoseBone bone : pose.bones.values())
            bones.put(bone.name, new PoseBone(bone));
    }
    
    public void setBoneProperty(String name, Property property, int index, float value)
    {
        PoseBone bone = bones.get(name);
        if (bone == null) return;
        bone.transform.setProperty(property, index, value);
    }
    
    public void apply(Armature armature)
    {
        for (PoseBone bonePose : bones.values())
        {
            Bone bone = armature.getBone(bonePose.name);
            if (bone == null) continue;
            bone.poseTransform.set(bonePose.transform);
        }
    }
    
    public Pose mix(Pose pose, float t)
    {
        for (PoseBone bone : bones.values())
        {
            PoseBone target = pose.bones.get(bone.name);
            if (target == null) continue;
            bone.mix(target, t);
        }
        
        return this;
    }
    
    public class PoseBone
    {
        public final String name;
        public final Transform transform;
        
        private PoseBone(DataInputStream in) throws IOException
        {
            name = IOUtil.readPaddedUTF(in);
            transform = new Transform(in);
        }
        
        private PoseBone(String name)
        {
            this.name = name;
            transform = new Transform();
        }
        
        private PoseBone(PoseBone bone)
        {
            name = bone.name;
            transform = new Transform(bone.transform);
        }
        
        private void mix(PoseBone bone, float t)
        {
            transform.mix(bone.transform, t);
        }
    }
}
