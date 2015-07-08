package com.samrj.devil.graphics.model;

import com.samrj.devil.math.Matrix3f;
import com.samrj.devil.math.Matrix4f;
import com.samrj.devil.math.Quat4f;
import com.samrj.devil.math.Util;
import com.samrj.devil.math.Vector3f;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * DevilModel armature bone.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
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
    public final Matrix4f baseMatrix; //Base matrices rotate from local to global
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
    
    //Rotation matrices rotate in local space.
    public final Matrix4f rotMatrix = new Matrix4f();
    public final Matrix4f inverseRotMatrix = new Matrix4f();
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
        
        Matrix3f baseMatrix3f = DevilModel.readMatrix3f(in);
        baseMatrix = baseMatrix3f.toMatrix4f();
        inverseBaseMatrix = baseMatrix3f.invert().toMatrix4f();
    }
    
    void solveRotationMatrix()
    {
        Matrix4f relativeRotMat = rotation.copy().normalize().toMatrix4f();
        
        //may be incorrect
        rotMatrix.set(baseMatrix);
        rotMatrix.mult(relativeRotMat);
        rotMatrix.mult(inverseBaseMatrix);
        
        if (parent != null && inheritRotation) rotMatrix.mult(parent.rotMatrix);
        
        inverseRotMatrix.set(rotMatrix).invert();
    }
    
    void solveHeadPosition()
    {
        headFinal.set(location);
        if (localLocation) headFinal.mult(baseMatrix);
        headFinal.add(headOffset);
        if (parent == null) return;
        if (inheritRotation) headFinal.mult(parent.rotMatrix);
        headFinal.add(parent.tailFinal);
    }
    
    void solveTailPosition()
    {
        tailFinal.set(tailOffset).mult(rotMatrix).add(headFinal);
    }
    
    void solveMatrix()
    {
        matrix.set();
        matrix.mult(Matrix4f.translate(headFinal));
        matrix.mult(rotMatrix);
        matrix.mult(Matrix4f.translate(head.cnegate()));
    }
    
    @Override
    public void solve()
    {
        solveRotationMatrix(); solveHeadPosition();
        solveTailPosition(); solveMatrix();
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
    
    public void reachTowards(Vector3f target)
    {
        Vector3f dir = target.csub(headFinal); //Global
        dir.mult(inverseBaseMatrix); //Local
        
        Vector3f v = Util.Axis.X.versor(); //Local
        v.mult(baseMatrix); //Global
        if (inheritRotation) v.mult(parent.rotMatrix);
        v.mult(inverseBaseMatrix); //Local again
//        v.mult(parent.inverseBaseMatrix); //Local to parent
        
        rotation.set(v.rotationTo(dir));
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
