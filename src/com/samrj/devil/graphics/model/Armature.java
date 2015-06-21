package com.samrj.devil.graphics.model;

import com.samrj.devil.math.topo.DAG;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.List;

public class Armature
{
    public final Bone[] bones;
    private final DAG<Bone> boneGraph = new DAG();
    private final List<Bone> boneOrder;
    
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
        
        boneOrder = boneGraph.sort();
    }
    
    public void updateBoneMatrices()
    {
        for (Bone bone : boneOrder) bone.updateMatrices();
    }
}
