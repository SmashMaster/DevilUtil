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
public class ChildOfConstraint implements MeshSkinner.Solvable
{
    private final PoseBone parent, realParent, child;
    
    public ChildOfConstraint(PoseBone parent, PoseBone child)
    {
        this.parent = parent;
        realParent = child.getParent();
        this.child = child;
    }

    @Override
    public void populateSolveGraph(DAG<MeshSkinner.Solvable> graph)
    {
        graph.addEdge(parent, this);
        if (realParent != null) graph.addEdge(realParent, this);
        graph.addEdge(this, child);
    }
    
    @Override
    public void removeSolved(Set<PoseBone> independent)
    {
        independent.remove(child);
    }
    
    @Override
    public void solve()
    {
        child.finalTransform.position.set(child.poseTransform.position);
        
        Mat3 basis = Mat3.identity();
        basis.mult(child.getBone().invMat);
        basis.mult(parent.rotMatrix);
        basis.mult(parent.getBone().matrix); //is this where it should be? everything else is I think.
        basis.mult(child.getBone().matrix);
        basis.rotate(Quat.normalize(child.poseTransform.rotation));
        
        child.finalTransform.rotation.setRotation(basis);
    }
}
