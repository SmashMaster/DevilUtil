package com.samrj.devil.model.constraint;

import com.samrj.devil.math.Mat3;
import com.samrj.devil.math.Vec3;
import com.samrj.devil.math.topo.DAG;
import com.samrj.devil.model.ArmatureSolver;
import com.samrj.devil.model.ArmatureSolver.BoneSolver;
import java.util.Set;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class CopyTransformConstraint implements ArmatureSolver.Constraint
{
    private final BoneSolver source, parent, target;
    
    public CopyTransformConstraint(BoneSolver source, BoneSolver target)
    {
        this.source = source;
        parent = target.getParent();
        this.target = target;
    }

    @Override
    public void populateSolveGraph(DAG<ArmatureSolver.Constraint> graph)
    {
        graph.addEdge(source, this);
        if (parent != null) graph.addEdge(parent, this);
        graph.addEdge(this, target);
    }
    
    @Override
    public void removeSolved(Set<BoneSolver> independent)
    {
        independent.remove(target);
    }
    
    @Override
    public void solve()
    {
        Vec3 head = new Vec3(target.bone.head);
        if (parent != null) head.mult(parent.skinMatrix);
        
        Vec3 pos = target.finalTransform.position;
        pos.set(source.getHeadPos()); //object
        pos.sub(head);
        if (parent != null) pos.mult(parent.invRotMat);
        pos.mult(target.bone.invMat);
        
        Mat3 basis = Mat3.identity();
        basis.mult(target.bone.invMat);
        if (target.bone.inheritRotation) basis.mult(parent.invRotMat);
        basis.mult(source.rotMatrix);
        basis.mult(source.bone.matrix); //is this where it should be?
        basis.mult(target.bone.matrix);
        
        target.finalTransform.rotation.setRotation(basis);
    }
}
