package com.samrj.devil.graphics.model;

import com.samrj.devil.math.Mat3;
import com.samrj.devil.math.Mat4;
import com.samrj.devil.math.Quat;
import com.samrj.devil.math.Util;
import com.samrj.devil.math.Vec3;
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
    public final Vec3 head, tail;
    public final Mat4 baseMatrix; //Base matrices rotate from local to global
    public final Mat4 inverseBaseMatrix;
    
    private final Collection<Bone> children = new ArrayList<>();
    private Bone parent = null;
    
    //Head offset from parent's tail, and tail offset from head.
    public final Vec3 headOffset = new Vec3();
    public final Vec3 tailOffset = new Vec3();
    
    //Live properties
    public final Vec3 location = new Vec3();
    public final Quat rotation = new Quat();
    public final Mat4 matrix = new Mat4();
    
    //Rotation matrices rotate in local space.
    public final Mat4 rotMatrix = new Mat4();
    public final Mat4 inverseRotMatrix = new Mat4();
    public final Vec3 headFinal = new Vec3();
    public final Vec3 tailFinal = new Vec3();
    
    public Bone(DataInputStream in) throws IOException
    {
        name = DevilModel.readPaddedUTF(in);
        parentIndex = in.readInt();
        
        int flagBits = in.readInt();
        inheritRotation = (flagBits & 1) == 1;
        localLocation = (flagBits & 2) == 2;
        head = new Vec3();
        head.read(in);
        tail = new Vec3();
        tail.read(in);
        headOffset.set(head);
        tailOffset.set(tail).sub(head);
        
        Mat3 baseMatrix3f = new Mat3();
        baseMatrix3f.read(in);
        baseMatrix = new Mat4(baseMatrix3f);
        inverseBaseMatrix = new Mat4(baseMatrix3f.invert());
    }
    
    void solveRotationMatrix()
    {
        Mat4 relativeRotMat = Mat4.rotation(Quat.normalize(rotation));
        
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
        matrix.setIdentity();
        matrix.translate(headFinal);
        matrix.mult(rotMatrix);
        matrix.translate(Vec3.negate(head));
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
    
    public void reachTowards(Vec3 target)
    {
        Vec3 dir = Vec3.sub(target, headFinal); //Global
        dir.mult(inverseBaseMatrix); //Local
        
        Vec3 v = new Vec3(1.0f, 0.0f, 0.0f); //Local
        v.mult(baseMatrix); //Global
        if (inheritRotation) v.mult(parent.rotMatrix);
        v.mult(inverseBaseMatrix); //Local again
//        v.mult(parent.inverseBaseMatrix); //Local to parent
        
        rotation.setRotation(v, dir);
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
