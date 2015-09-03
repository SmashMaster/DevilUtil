package com.samrj.devil.graphics.model;

import com.samrj.devil.io.Memory;
import com.samrj.devil.math.topo.DAG;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

/**
 * DevilModel armature.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Armature
{
    public final Bone[] bones;
    public final IKConstraint[] ikConstraints;
    private final DAG<Solvable> solveGraph;
    private final List<Solvable> solveOrder;
    private Memory boneBlock;
    private ByteBuffer boneMatrixBuffer;
    
    Armature(DataInputStream in) throws IOException
    {
        solveGraph = new DAG<>();
        
        int numBones = in.readInt();
        bones = new Bone[numBones];
        for (int i=0; i<numBones; i++)
        {
            Bone bone = new Bone(in);
            bones[i] = bone;
            solveGraph.add(bone);
        }
        
        for (Bone bone : bones)
        {
            if (bone.parentIndex < 0) continue;
            Bone parent = bones[bone.parentIndex];
            bone.setParent(parent);
            solveGraph.addEdge(parent, bone);
        }
        
        int numIKConstraints = in.readInt();
        ikConstraints = new IKConstraint[numIKConstraints];
        for (int i=0; i<numIKConstraints; i++)
        {
            IKConstraint ikConstraint = new IKConstraint(in, bones);
            ikConstraints[i] = ikConstraint;
            solveGraph.remove(ikConstraint.start);
            solveGraph.remove(ikConstraint.end);
            solveGraph.add(ikConstraint);
            
            Bone parent = ikConstraint.start.getParent();
            if (parent != null)
                solveGraph.addEdge(parent, ikConstraint);
            
            for (Bone child : ikConstraint.end.getChildren())
                solveGraph.addEdge(ikConstraint, child);
            
            solveGraph.addEdge(ikConstraint.target, ikConstraint);
            solveGraph.addEdge(ikConstraint.pole, ikConstraint);
        }
        
        solveOrder = solveGraph.sort();
        
        boneBlock = new Memory(numBones*16*4);
        boneMatrixBuffer = boneBlock.buffer;
    }
    
    public void solve()
    {
        for (Solvable solvable : solveOrder) solvable.solve();
    }
    
    public ByteBuffer bufferBoneMatrices()
    {
        boneMatrixBuffer.rewind();
        for (Bone bone : bones) bone.matrix.write(boneMatrixBuffer);
        boneMatrixBuffer.rewind();
        return boneMatrixBuffer;
    }
    
    void destroy()
    {
        Arrays.fill(bones, null);
        Arrays.fill(ikConstraints, null);
        boneBlock.free();
        boneBlock = null;
        boneMatrixBuffer = null;
    }
}
