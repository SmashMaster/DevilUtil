package com.samrj.devil.model;

import com.samrj.devil.io.Memory;
import com.samrj.devil.math.topo.DAG;
import com.samrj.devil.util.IdentitySet;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class MeshSkinner
{
    public final Pose pose;
    public final IKConstraint[] ikConstraints;
    public final String[] vertexGroups;
    
    public final Memory matrixBlock;
    public final ByteBuffer matrixData;
    
    private final List<Solvable> extraSolvables;
    private final IdentitySet<PoseBone> independent;
    private List<Solvable> solveOrder;
    
    public MeshSkinner(ModelObject<Mesh> object)
    {
        ModelObject<Armature> armObj = (ModelObject<Armature>)object.parent.get();
        pose = armObj.pose;
        ikConstraints = armObj.ikConstraints;
        vertexGroups = object.vertexGroups;
        matrixBlock = new Memory(vertexGroups.length*16*4);
        matrixData = matrixBlock.buffer;
        extraSolvables = new LinkedList<>();
        independent = new IdentitySet<>();
        MeshSkinner.this.sortSolvables();
    }
    
    public void addSolvable(Solvable s)
    {
        solveOrder = null;
        extraSolvables.add(s);
    }
    
    public void clearSolvables()
    {
        solveOrder = null;
        extraSolvables.clear();
    }
    
    public void sortSolvables()
    {
        independent.clear();
        independent.addAll(pose.getBones());
        for (IKConstraint ik : ikConstraints) ik.removeSolved(independent);
        for (Solvable s : extraSolvables) s.removeSolved(independent);
        
        DAG<Solvable> solveGraph = new DAG<>();
        for (PoseBone bone : pose.getBones()) bone.populateSolveGraph(solveGraph);
        for (IKConstraint ik : ikConstraints) ik.populateSolveGraph(solveGraph);
        for (Solvable s : extraSolvables) s.populateSolveGraph(solveGraph);
        solveOrder = solveGraph.sort();
    }
    
    public void solve()
    {
        if (solveOrder == null) throw new IllegalStateException("Unsorted. Call sortSolvables() first.");
        
        for (PoseBone bone : independent)
        {
            bone.finalTransform.set(bone.poseTransform);
            bone.finalTransform.rotation.normalize();
        }
        
        matrixData.rewind();
        for (Solvable s : solveOrder) s.solve();
        
        for (String group : vertexGroups)
        {
            PoseBone bone = pose.getBone(group);
            if (bone == null) continue;
            bone.skinMatrix.write(matrixData);
        }
    }
    
    public final void destroy()
    {
        matrixBlock.free();
    }
    
    public interface Solvable
    {
        public void populateSolveGraph(DAG<Solvable> graph);
        public default void removeSolved(Set<PoseBone> independent) {}
        public void solve();
    }
}
