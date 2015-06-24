package com.samrj.devil.graphics.model;

import com.samrj.devil.math.Matrix4f;
import com.samrj.devil.math.Quat4f;
import com.samrj.devil.math.Vector3f;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class Bone implements Solvable
{
    public static enum Property
    {
        LOCATION, ROTATION;
    }
    
    //Constants
    public final String name;
    public final boolean inheritRotation, localLocation;
    public final int parentIndex;
    public final Vector3f head, tail;
    public final Matrix4f baseMatrix;
    public final Matrix4f inverseBaseMatrix;
    
    private final Collection<Bone> children = new ArrayList<>();
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
        
        int flagBits = in.readInt();
        inheritRotation = (flagBits & 1) == 1;
        localLocation = (flagBits & 2) == 2;
        head = DevilModel.readVector3f(in);
        tail = DevilModel.readVector3f(in);
        headOffset.set(head);
        tailOffset.set(tail).sub(head);
        
        Quat4f boneDir = Quat4f.fromDir(tailOffset);
        baseMatrix = boneDir.toMatrix4f();
        inverseBaseMatrix = boneDir.invert().toMatrix4f();
    }
    
    @Override
    public void solve()
    {
        Matrix4f relativeRotMat = rotation.copy().normalize().toMatrix4f();
        
        if (parent == null)
        {
            rotMatrix.set(relativeRotMat);
            headFinal.set(location);
            if (localLocation) headFinal.mult(baseMatrix);
            headFinal.add(headOffset);
            tailFinal.set(tailOffset).mult(relativeRotMat).add(headFinal);
        }
        else if (inheritRotation)
        {
            rotMatrix.set(parent.baseMatrix).mult(parent.rotMatrix).mult(relativeRotMat).mult(parent.inverseBaseMatrix);
            headFinal.set(location);
            if (localLocation) headFinal.mult(baseMatrix);
            headFinal.add(headOffset).mult(parent.rotMatrix).add(parent.tailFinal);
            tailFinal.set(tailOffset).mult(rotMatrix).add(headFinal);
        }
        else
        {
            rotMatrix.set(parent.baseMatrix).mult(relativeRotMat).mult(parent.inverseBaseMatrix);;
            headFinal.set(location);
            if (localLocation) headFinal.mult(baseMatrix);
            headFinal.add(headOffset).add(parent.tailFinal);
            tailFinal.set(tailOffset).mult(rotMatrix).add(headFinal);
        }
        
        matrix.set();
        matrix.mult(Matrix4f.translate(headFinal));
        matrix.mult(rotMatrix);
        matrix.mult(Matrix4f.translate(head.cnegate()));
    }
    
    void setParent(Bone parent)
    {
        this.parent = parent;
        parent.children.add(this);
        headOffset.set(head).sub(parent.tail);
    }
    
    public Bone getParent()
    {
        return parent;
    }
    
    public Collection<Bone> getChildren()
    {
        return Collections.unmodifiableCollection(children);
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
