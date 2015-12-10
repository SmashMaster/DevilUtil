package com.samrj.devil.graphics.model;

import com.samrj.devil.io.IOUtil;
import java.io.DataInputStream;
import java.io.IOException;

public class Armature implements DataBlock
{
    public final String name;
    public final Bone[] bones;
    
    Armature(DataInputStream in) throws IOException
    {
        name = IOUtil.readPaddedUTF(in);
        bones = IOUtil.arrayFromStream(in, Bone.class, Bone::new);
        for (Bone bone : bones) bone.populate(bones);
    }

    @Override
    public Type getType()
    {
        return Type.ARMATURE;
    }
}
