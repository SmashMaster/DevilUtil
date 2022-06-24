package com.samrj.devil.model.constraint;

import com.samrj.devil.math.topo.DAG;
import com.samrj.devil.model.ArmatureSolver;
import com.samrj.devil.model.ArmatureSolver.BoneSolver;

import java.util.Set;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2022 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class CopyRotationConstraint implements ArmatureSolver.Constraint
{
    public record Definition(String boneName, String targetName) {}

    private final BoneSolver source, parent, target;
    public float influence = 1.0f;
    
    public CopyRotationConstraint(Definition def, ArmatureSolver solver)
    {
        source = solver.getBone(def.boneName);
        target = solver.getBone(def.targetName);
        parent = target.getParent();
    }

    @Override
    public void populateSolveGraph(DAG<ArmatureSolver.Constraint> graph)
    {
        graph.add(this);
        
        graph.addEdge(source, this);
        if (parent != null) graph.addEdge(parent, this);
        graph.addEdge(this, target);
    }
    
    @Override
    public void removeSolved(Set<BoneSolver> nonconstrained)
    {
        nonconstrained.remove(target);
    }
    
    @Override
    public void solve()
    {
        target.finalTransform.rot.set(source.finalTransform.rot);
    }
}
