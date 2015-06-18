package com.samrj.devil.graphics.model;

import com.samrj.devil.math.Matrix3f;
import com.samrj.devil.math.Vector3f;
import java.io.DataInputStream;
import java.io.IOException;

public class Bone
{
    public final String name;
    public final boolean connect, inheritRotation, localLocation, relativeParent;
    public final int parentIndex;
    public final Vector3f head, tail;
    public final Matrix3f matrix;
    
    public Bone(DataInputStream in) throws IOException
    {
        name = DevilModel.readPaddedUTF(in);
        
        int bitFlags = in.readInt();
        connect = (bitFlags & 1) == 1;
        inheritRotation = (bitFlags & 2) == 2;
        localLocation = (bitFlags & 4) == 4;
        relativeParent = (bitFlags & 8) == 8;
        
        parentIndex = in.readInt();
        head = DevilModel.readVector3f(in);
        tail = DevilModel.readVector3f(in);
        matrix = DevilModel.readMatrix3f(in);
    }
}
