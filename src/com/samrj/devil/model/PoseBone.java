package com.samrj.devil.model;

import com.samrj.devil.io.IOUtil;
import com.samrj.devil.math.Mat3;
import com.samrj.devil.math.Mat4;
import com.samrj.devil.math.Vec3;
import com.samrj.devil.math.topo.DAG;
import com.samrj.devil.model.Armature.Bone;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class PoseBone implements MeshSkinner.Solvable
{
    public final String name;
    public final Transform transform;
    
    public final Transform poseTransform, finalTransform;
    public final Mat4 skinMatrix; //object rest position -> object pose position
    public final Mat3 rotMatrix; //object rest direction -> object pose direction
    public final Mat3 invRotMat; //object pose direction -> object rest direction
    
    private Bone bone;
    private PoseBone parent;

    PoseBone(DataInputStream in) throws IOException
    {
        name = IOUtil.readPaddedUTF(in);
        transform = new Transform(in);
        poseTransform = new Transform();
        finalTransform = new Transform();
        skinMatrix = new Mat4();
        rotMatrix = new Mat3();
        invRotMat = new Mat3();
    }
    
    PoseBone(PoseBone bone)
    {
        name = bone.name;
        transform = new Transform(bone.transform);
        poseTransform = new Transform();
        finalTransform = new Transform();
        skinMatrix = new Mat4();
        rotMatrix = new Mat3();
        invRotMat = new Mat3();
        
        //Must still populate.
    }
    
    void populate(Armature armature, Pose pose)
    {
        bone = armature.getBone(name);
        parent = pose.getBone(bone.getParent().name);
    }
    
    public Bone getBone()
    {
        return bone;
    }
    
    public PoseBone getParent()
    {
        return parent;
    }

    public void set(PoseBone bone)
    {
        if (bone.bone != this.bone) throw new IllegalArgumentException();
        transform.set(bone.transform);
    }

    public void mix(PoseBone bone, float t)
    {
        if (bone.bone != this.bone) throw new IllegalArgumentException();
        transform.mix(bone.transform, t);
    }
    
    public Mat4 getModelMatrix()
    {
        Mat4 out = new Mat4(skinMatrix);
        out.mult(bone.tail);
        out.mult(new Mat4(bone.matrix));
        return out;
    }
    
    public Vec3 getHeadPos()
    {
        Vec3 out = new Vec3(finalTransform.position);
        out.mult(bone.matrix);
        out.add(bone.head);
        if (parent != null) out.mult(parent.skinMatrix);
        return out;
    }

    @Override
    public void populateSolveGraph(DAG<MeshSkinner.Solvable> graph)
    {
        if (parent != null) graph.addEdge(parent, this);
        else graph.add(this);
    }

    @Override
    public void solve()
    {
        skinMatrix.setIdentity();
        if (parent != null) skinMatrix.mult(parent.skinMatrix);
        skinMatrix.translate(bone.head);
        if (parent != null && !bone.inheritRotation) skinMatrix.mult(new Mat4(parent.invRotMat));
        skinMatrix.mult(new Mat4(bone.matrix));
        finalTransform.apply(skinMatrix);
        skinMatrix.mult(new Mat4(bone.invMat));
        skinMatrix.translate(Vec3.negate(bone.head));
        
        rotMatrix.setIdentity();
        if (parent != null && bone.inheritRotation) rotMatrix.mult(parent.rotMatrix);
        rotMatrix.mult(bone.matrix);
        finalTransform.apply(rotMatrix);
        rotMatrix.mult(bone.invMat);
        
        if (!finalTransform.scale.isZero(0.0f)) Mat3.invert(rotMatrix, invRotMat);
    }
}
