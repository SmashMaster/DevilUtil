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
public class Bone
{
    public final String name;
    public final Vec3 head, tail;
    public final Mat4 matrix, invMat;
    
    public final Transform transform;
    public final Mat4 poseMatrix;
    
    private Bone parent;
    
    private final int parentIndex;
    
    Bone(DataInputStream in) throws IOException
    {
        name = IOUtil.readPaddedUTF(in);
        parentIndex = in.readInt();
        head = new Vec3(in);
        tail = new Vec3(in);
        Mat3 mat3 = new Mat3(in);
        matrix = new Mat4(mat3);
        invMat = new Mat4(Mat3.invert(mat3));
        transform = new Transform();
        poseMatrix = new Mat4();
    }
    
    public void updatePoseMatrix()
    {
        poseMatrix.setIdentity();
        if (parent != null) poseMatrix.mult(parent.poseMatrix);
        poseMatrix.translate(head);
        poseMatrix.mult(matrix);
        transform.apply(poseMatrix);
        poseMatrix.mult(invMat);
        poseMatrix.translate(Vec3.negate(head));
    }
    
    public Bone getParent()
    {
        return parent;
    }
    
    void populate(Bone[] bones)
    {
        if (parentIndex >= 0) parent = bones[parentIndex];
    }
}
