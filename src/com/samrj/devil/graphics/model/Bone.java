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
    
    /**
     * Resting properties: head and tail define the resting positions of the
     * bone. matrix transforms from local directions to global resting
     * directions.
     */
    public final Vec3 head, tail;
    public final Mat3 matrix, invMat;
    
    public final Transform transform;
    public final Mat4 skinMatrix;
    
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
    }
    
    void populate(Bone[] bones)
    {
        if (parentIndex >= 0) parent = bones[parentIndex];
    }
    
    public Vec3 getHeadPos()
    {
        return parent != null ? Vec3.mult(head, parent.skinMatrix) : new Vec3(head);
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
    }
    
    @Override
    public Bone getParent()
    {
        return parent;
    }
}
