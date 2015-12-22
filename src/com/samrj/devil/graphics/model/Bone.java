package com.samrj.devil.graphics.model;

import com.samrj.devil.io.IOUtil;
import com.samrj.devil.math.Mat3;
import com.samrj.devil.math.Mat4;
import com.samrj.devil.math.Vec3;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Bone implements BoneSolver.Solvable
{
    public final String name;
    
    public final Vec3 head, tail;
    public final Mat3 matrix; //bone direction -> object rest direction
    public final Mat3 invMat; //object rest direction -> bone direction
    
    public final Transform transform;
    public final Mat4 skinMatrix; //object rest position -> object pose position
    public final Mat3 rotMatrix; //object rest direction -> object pose direction
    public final Mat3 invRotMat; //object pose direction -> object rest direction
    
    private Bone parent;
    
    private final int parentIndex;
    
    Bone(DataInputStream in) throws IOException
    {
        name = IOUtil.readPaddedUTF(in);
        parentIndex = in.readInt();
        head = new Vec3(in);
        tail = new Vec3(in);
        matrix = new Mat3(in);
        invMat = Mat3.invert(matrix);
        transform = new Transform();
        skinMatrix = new Mat4();
        rotMatrix = new Mat3();
        invRotMat = new Mat3();
    }
    
    void populate(Bone[] bones)
    {
        if (parentIndex >= 0) parent = bones[parentIndex];
    }
    
    public Vec3 getHeadPos()
    {
        Vec3 out = new Vec3(transform.position);
        out.mult(matrix);
        out.add(head);
        if (parent != null) out.mult(parent.skinMatrix);
        return out;
    }
    
    @Override
    public void solve()
    {
        skinMatrix.setIdentity();
        if (parent != null) skinMatrix.mult(parent.skinMatrix);
        skinMatrix.translate(head);
        skinMatrix.mult(new Mat4(matrix));
        transform.apply(skinMatrix);
        skinMatrix.mult(new Mat4(invMat));
        skinMatrix.translate(Vec3.negate(head));
        
        rotMatrix.setIdentity();
        if (parent != null) rotMatrix.mult(parent.rotMatrix);
        rotMatrix.mult(matrix);
        transform.apply(rotMatrix);
        rotMatrix.mult(invMat);
        
        Mat3.invert(rotMatrix, invRotMat);
    }
    
    public Bone getParent()
    {
        return parent;
    }
}
