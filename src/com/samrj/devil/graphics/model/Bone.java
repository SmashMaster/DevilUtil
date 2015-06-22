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
    public final boolean inheritRotation;
    public final int parentIndex;
    public final Vector3f head, tail;
    public final Matrix4f baseMatrix;
    
    private Bone parent = null;
    
    //Head offset from parent's tail, and tail offset from head.
    public final Vector3f headOffset = new Vector3f();
    public final Vector3f tailOffset = new Vector3f();
    
    //Live properties
    public final Vector3f location = new Vector3f();
    public final Quat4f rotation = new Quat4f();
    public final Matrix4f matrix = new Matrix4f();
    public final Matrix4f rotMatrix = new Matrix4f();
    public final Vector3f headFinal = new Vector3f();
    public final Vector3f tailFinal = new Vector3f();
    
    public Bone(DataInputStream in) throws IOException
    {
        name = DevilModel.readPaddedUTF(in);
        parentIndex = in.readInt();
        inheritRotation = in.readInt() != 0;
        head = DevilModel.readVector3f(in);
        tail = DevilModel.readVector3f(in);
        baseMatrix = DevilModel.readMatrix3f(in).toMatrix4f();
        headOffset.set(head);
        tailOffset.set(tail).sub(head);
    }
    
    void setParent(Bone parent)
    {
        this.parent = parent;
        headOffset.set(head).sub(parent.tail);
    }
    
    void updateMatrices()
    {
        Matrix4f relativeRotMat = rotation.copy().normalize().toMatrix4f();
        
        if (parent == null)
        {
            rotMatrix.set(relativeRotMat);
            headFinal.set(headOffset).add(location);
            tailFinal.set(tailOffset).mult(relativeRotMat).add(headFinal);
        }
        else if (inheritRotation)
        {
            rotMatrix.set(parent.rotMatrix).mult(relativeRotMat);
            headFinal.set(headOffset).add(location).mult(parent.rotMatrix).add(parent.tailFinal);
            tailFinal.set(tailOffset).mult(rotMatrix).add(headFinal);
        }
        else
        {
            rotMatrix.set(relativeRotMat);
            headFinal.set(headOffset).add(location).add(parent.tailFinal);
            tailFinal.set(tailOffset).mult(rotMatrix).add(headFinal);
        }
        
        matrix.set();
        matrix.mult(Matrix4f.translate(headFinal));
        matrix.mult(rotMatrix);
        matrix.mult(Matrix4f.translate(head.cnegate()));
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
