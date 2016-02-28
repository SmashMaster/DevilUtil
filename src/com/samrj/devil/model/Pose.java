package com.samrj.devil.model;

import com.samrj.devil.model.Transform.Property;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Pose
{
    private final Map<String, PoseBone> bones;
    private Armature armature;
    
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
    
    public Pose(Pose pose)
    {
        bones = new HashMap<>(pose.bones.size());
        for (PoseBone bone : pose.bones.values())
            bones.put(bone.name, new PoseBone(bone));
        
        populate(pose.armature);
    }
    
    public Armature getArmature()
    {
        return armature;
    }
    
    public Collection<PoseBone> getBones()
    {
        return Collections.unmodifiableCollection(bones.values());
    }
    
    void populate(Armature armature)
    {
        this.armature = armature;
        for (PoseBone bone : bones.values()) bone.populate(armature, this);
    }
    
    public void setBoneProperty(String name, Property property, int index, float value)
    {
        PoseBone bone = bones.get(name);
        if (bone == null) throw new IllegalArgumentException("No such bone '" + name + "'");
        bone.transform.setProperty(property, index, value);
    }
    
    public PoseBone getBone(String name)
    {
        return bones.get(name);
    }
    
    public Pose clear()
    {
        bones.clear();
        return this;
    }
    
    public Pose mix(Pose pose, float t)
    {
        for (PoseBone target : pose.bones.values())
        {
            PoseBone bone = bones.get(target.name);
            if (target == null) throw new IllegalArgumentException("No such pose bone '" + bone.name + "'");
            bone.mix(target, t);
        }
        
        return this;
    }
}
