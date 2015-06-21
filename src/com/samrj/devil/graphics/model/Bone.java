package com.samrj.devil.graphics.model;

import com.samrj.devil.math.Matrix4f;
import com.samrj.devil.math.Quat4f;
import com.samrj.devil.math.Vector3f;
import java.io.DataInputStream;
import java.io.IOException;

public class Bone
{
    public static enum Property
    {
        LOCATION, ROTATION;
    }
    
    //Constants
    public final String name;
    public final boolean connect, inheritRotation, localLocation, relativeParent;
    public final int parentIndex;
    public final Vector3f head, tail;
    public final Matrix4f baseMatrix;
    
    private Bone parent = null;
    
    //Live properties
    public final Vector3f location = new Vector3f();
    public final Quat4f rotation = new Quat4f();
    public final Matrix4f matrix = new Matrix4f();
    
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
        baseMatrix = DevilModel.readMatrix3f(in).toMatrix4f();
    }
    
    void setParent(Bone parent)
    {
        this.parent = parent;
    }
    
    void updateMatrices()
    {
        if (parent != null) matrix.set(parent.matrix);
        else matrix.set();
        matrix.multTranslate(location);
        matrix.multRotate(rotation);
    }
    
    public Bone getParent()
    {
        return parent;
    }
    
    public void set(Property property, int index, float value)
    {
        switch(property)
        {
            case LOCATION:
                switch (index)
                {
                    case 0: location.x = value; break;
                    case 1: location.y = value; break;
                    case 2: location.z = value; break;
                    default: throw new ArrayIndexOutOfBoundsException();
                }
                break;
            case ROTATION:
                switch (index)
                {
                    case 0: rotation.w = value; break;
                    case 1: rotation.x = value; break;
                    case 2: rotation.y = value; break;
                    case 3: rotation.z = value; break;
                    default: throw new ArrayIndexOutOfBoundsException();
                }
                break;
            default: throw new IllegalArgumentException("Illegal property specified.");
        }
    }
}
