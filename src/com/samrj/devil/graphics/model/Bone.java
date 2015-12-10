package com.samrj.devil.graphics.model;

import com.samrj.devil.io.IOUtil;
import com.samrj.devil.math.Mat3;
import com.samrj.devil.math.Vec3;
import java.io.DataInputStream;
import java.io.IOException;

public class Bone
{
    public final String name;
    public final Vec3 head, tail;
    public final Mat3 matrix;
    
    public Bone parent;
    
    private final int parentIndex;
    
    Bone(DataInputStream in) throws IOException
    {
        name = IOUtil.readPaddedUTF(in);
        parentIndex = in.readInt();
        head = new Vec3(in);
        tail = new Vec3(in);
        matrix = new Mat3(in);
    }
    
    void populate(Bone[] bones)
    {
        if (parentIndex >= 0) parent = bones[parentIndex];
    }
}
