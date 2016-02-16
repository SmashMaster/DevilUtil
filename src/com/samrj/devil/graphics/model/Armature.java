package com.samrj.devil.graphics.model;

import com.samrj.devil.graphics.model.Transform.Property;
import com.samrj.devil.io.IOUtil;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Armature implements DataBlock
{
    public final String name;
    public final Bone[] bones;
    private final Map<String, Bone> nameMap;
    
    Armature(DataInputStream in) throws IOException
    {
        name = IOUtil.readPaddedUTF(in);
        bones = IOUtil.arrayFromStream(in, Bone.class, Bone::new);
        for (Bone bone : bones) bone.populate(bones);
        
        nameMap = new HashMap<>(bones.length);
        for (Bone bone : bones) nameMap.put(bone.name, bone);
    }
    
    public Bone getBone(String name)
    {
        return nameMap.get(name);
    }
    
    public void setBoneProperty(String name, Property property, int index, float value)
    {
        Bone bone = nameMap.get(name);
        if (bone == null) return;
        bone.poseTransform.setProperty(property, index, value);
    }
    
    @Override
    public String getName()
    {
        return name;
    }
}
