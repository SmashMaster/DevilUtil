package com.samrj.devil.model;

import com.samrj.devil.math.Mat3;
import com.samrj.devil.math.Quat;
import com.samrj.devil.math.topo.DAG;
import java.util.Set;

/**
 * Dynamic parenting constraint. Currently only works for rotation. Also doesn't
 * support inherited rotation yet.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class ChildOfConstraint implements BoneSolver.Solvable
{
    private final Bone parent, realParent, child;
    
    public ChildOfConstraint(Bone parent, Bone child)
    {
        this.parent = parent;
        realParent = child.getParent();
        this.child = child;
    }

    @Override
    public void populateSolveGraph(DAG<BoneSolver.Solvable> graph)
    {
        graph.addEdge(parent, this);
        if (realParent != null) graph.addEdge(realParent, this);
        graph.addEdge(this, child);
    }
    
    @Override
    public void removeSolved(Set<Bone> independent)
    {
        independent.remove(child);
    }
    
    @Override
    public void solve()
    {
        child.finalTransform.position.set(child.poseTransform.position);
        
        Mat3 basis = Mat3.identity();
        basis.mult(child.invMat);
        basis.mult(parent.rotMatrix);
        basis.mult(parent.matrix); //is this where it should be? everything else is I think.
        basis.mult(child.matrix);
        basis.rotate(Quat.normalize(child.poseTransform.rotation));
        
        child.finalTransform.rotation.setRotation(basis);
    }
}
