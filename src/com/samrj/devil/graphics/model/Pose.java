package com.samrj.devil.graphics.model;

import com.samrj.devil.graphics.model.Transform.Property;
import com.samrj.devil.io.IOUtil;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
            bone.transform.set(bonePose.transform);
        }
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
    }
}
