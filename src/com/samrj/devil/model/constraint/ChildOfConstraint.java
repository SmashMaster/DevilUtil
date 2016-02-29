package com.samrj.devil.model.constraint;

import com.samrj.devil.math.Mat3;
import com.samrj.devil.math.Quat;
import com.samrj.devil.math.topo.DAG;
import com.samrj.devil.model.ArmatureSolver;
import com.samrj.devil.model.ArmatureSolver.BoneSolver;
import java.util.Set;

/**
 * Dynamic parenting constraint. Currently only works for rotation. Also doesn't
 * support inherited rotation yet.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class ChildOfConstraint implements ArmatureSolver.Constraint
{
    private final BoneSolver parent, realParent, child;
    
    public ChildOfConstraint(BoneSolver parent, BoneSolver child)
    {
        this.parent = parent;
        realParent = child.getParent();
        this.child = child;
    }

    @Override
    public void populateSolveGraph(DAG<ArmatureSolver.Constraint> graph)
    {
        graph.addEdge(parent, this);
        if (realParent != null) graph.addEdge(realParent, this);
        graph.addEdge(this, child);
    }
    
    @Override
    public void removeSolved(Set<BoneSolver> independent)
    {
        independent.remove(child);
    }
    
    @Override
    public void solve()
    {
        child.finalTransform.position.set(child.poseTransform.position);
        
        Mat3 basis = Mat3.identity();
        basis.mult(child.bone.invMat);
        basis.mult(parent.rotMatrix);
        basis.mult(parent.bone.matrix); //is this where it should be? everything else is I think.
        basis.mult(child.bone.matrix);
        basis.rotate(Quat.normalize(child.poseTransform.rotation));
        
        child.finalTransform.rotation.setRotation(basis);
    }
}
