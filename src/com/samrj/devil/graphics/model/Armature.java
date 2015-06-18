package com.samrj.devil.graphics.model;

import java.io.DataInputStream;
import java.io.IOException;

public class Armature
{
    public final Bone[] bones;
    
    public Armature(DataInputStream in) throws IOException
    {
        int numBones = in.readInt();
        bones = new Bone[numBones];
        for (int i=0; i<numBones; i++)
            bones[i] = new Bone(in);
    }
}
