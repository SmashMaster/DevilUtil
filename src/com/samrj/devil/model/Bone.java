package com.samrj.devil.model;

import com.samrj.devil.io.IOUtil;
import com.samrj.devil.math.Mat3;
import com.samrj.devil.math.Mat4;
import com.samrj.devil.math.Vec3;
import com.samrj.devil.math.topo.DAG;
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
    public final boolean inheritRotation;
    
    public final Vec3 head, tail;
    public final Mat3 matrix; //bone direction -> object rest direction
    public final Mat3 invMat; //object rest direction -> bone direction
    
    public final Transform poseTransform, finalTransform;
    public final Mat4 skinMatrix; //object rest position -> object pose position
    public final Mat3 rotMatrix; //object rest direction -> object pose direction
    public final Mat3 invRotMat; //object pose direction -> object rest direction
    
    private Bone parent;
    
    private final int parentIndex;
    
    Bone(DataInputStream in) throws IOException
    {
        name = IOUtil.readPaddedUTF(in);
        parentIndex = in.readInt();
        inheritRotation = in.readInt() != 0;
        head = new Vec3(in);
        tail = new Vec3(in);
        matrix = new Mat3(in);
        invMat = Mat3.invert(matrix);
        poseTransform = new Transform();
        finalTransform = new Transform();
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
        Vec3 out = new Vec3(finalTransform.position);
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
        if (parent != null && !inheritRotation) skinMatrix.mult(new Mat4(parent.invRotMat));
        skinMatrix.mult(new Mat4(matrix));
        finalTransform.apply(skinMatrix);
        skinMatrix.mult(new Mat4(invMat));
        skinMatrix.translate(Vec3.negate(head));
        
        rotMatrix.setIdentity();
        if (parent != null && inheritRotation) rotMatrix.mult(parent.rotMatrix);
        rotMatrix.mult(matrix);
        finalTransform.apply(rotMatrix);
        rotMatrix.mult(invMat);
        
        if (!finalTransform.scale.isZero(0.0f)) Mat3.invert(rotMatrix, invRotMat);
    }
    
    public Bone getParent()
    {
        return parent;
    }

    @Override
    public void populateSolveGraph(DAG<BoneSolver.Solvable> graph)
    {
        if (parent != null) graph.addEdge(parent, this);
        else graph.add(this);
    }
}
