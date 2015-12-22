package com.samrj.devil.graphics.model;

import com.samrj.devil.io.Memory;
import com.samrj.devil.math.topo.DAG;
import java.nio.ByteBuffer;
import java.util.List;

public class BoneSolver
{
    public final Armature armature;
    public final IKConstraint[] ikConstraints;
    public final String[] vertexGroups;
    
    public final Memory matrixBlock;
    public final ByteBuffer matrixData;
    
    private final List<Solvable> solveOrder;
    
    public BoneSolver(ModelObject meshObject)
    {
        ModelObject armatureObject = meshObject.getParent();
        armature = (Armature)armatureObject.getData();
        ikConstraints = armatureObject.ikConstraints;
        vertexGroups = meshObject.vertexGroups;
        matrixBlock = new Memory(vertexGroups.length*16*4);
        matrixData = matrixBlock.buffer;
        
        DAG<Solvable> solveGraph = new DAG<>();
        for (Bone bone : armature.bones)
        {
            Bone parent = bone.getParent();
            if (parent == null) continue;
            solveGraph.addEdge(parent, bone);
        }
        for (IKConstraint ik : ikConstraints)
        {
            solveGraph.addEdge(ik.getParent(), ik);
            solveGraph.addEdge(ik.getTarget(), ik);
            solveGraph.addEdge(ik.getPole(), ik);
            solveGraph.addEdge(ik, ik.getStart());
        }
        solveOrder = solveGraph.sort();
    }
    
    public void solve()
    {
        matrixData.rewind();
        for (Solvable s : solveOrder) s.solve();
        
        for (String group : vertexGroups)
        {
            Bone bone = armature.getBone(group);
            if (bone == null) continue;
            bone.skinMatrix.write(matrixData);
        }
    }
    
    public final void destroy()
    {
        matrixBlock.free();
    }
    
    interface Solvable
    {
        void solve();
    }
}
