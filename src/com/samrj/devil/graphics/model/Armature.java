package com.samrj.devil.graphics.model;

import com.samrj.devil.buffer.BufferUtil;
import com.samrj.devil.math.topo.DAG;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.List;

public class Armature
{
    public final Bone[] bones;
    public final IKConstraint[] ikConstraints;
    private final DAG<Bone> boneGraph = new DAG();
    private final List<Bone> boneOrder;
    private final ByteBuffer boneMatrixBuffer;
    
    public Armature(DataInputStream in) throws IOException
    {
        int numBones = in.readInt();
        bones = new Bone[numBones];
        for (int i=0; i<numBones; i++)
        {
            Bone bone = new Bone(in);
            bones[i] = bone;
            boneGraph.add(bone);
        }
        
        for (Bone bone : bones)
        {
            if (bone.parentIndex < 0) continue;
            Bone parent = bones[bone.parentIndex];
            bone.setParent(parent);
            boneGraph.addEdge(parent, bone);
        }
        
        int numIKConstraints = in.readInt();
        ikConstraints = new IKConstraint[numIKConstraints];
        for (int i=0; i<numIKConstraints; i++)
            ikConstraints[i] = new IKConstraint(in, bones);
        
        boneOrder = boneGraph.sort();
        boneMatrixBuffer = BufferUtil.createByteBuffer(numBones*16*4);
    }
    
    public void updateBoneMatrices()
    {
        for (Bone bone : boneOrder) bone.updateMatrices();
    }
    
    public ByteBuffer bufferBoneMatrices()
    {
        boneMatrixBuffer.clear();
        FloatBuffer fBuffer = boneMatrixBuffer.asFloatBuffer();
        for (Bone bone : bones) bone.matrix.putIn(fBuffer);
        boneMatrixBuffer.rewind();
        return boneMatrixBuffer;
    }
}
