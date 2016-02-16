package com.samrj.devil.graphics.model;

import com.samrj.devil.math.Mat3;
import com.samrj.devil.math.Vec3;
import com.samrj.devil.math.topo.DAG;
import java.util.Set;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class CopyTransformConstraint implements BoneSolver.Solvable
{
    private final Bone source, parent, target;
    
    public CopyTransformConstraint(Bone source, Bone target)
    {
        this.source = source;
        parent = target.getParent();
        this.target = target;
    }

    @Override
    public void populateSolveGraph(DAG<BoneSolver.Solvable> graph)
    {
        graph.addEdge(source, this);
        if (parent != null) graph.addEdge(parent, this);
        graph.addEdge(this, target);
    }
    
    @Override
    public void removeSolved(Set<Bone> independent)
    {
        independent.remove(target);
    }
    
    @Override
    public void solve()
    {
        Vec3 head = new Vec3(target.head);
        if (parent != null) head.mult(parent.skinMatrix);
        
        Vec3 pos = target.finalTransform.position;
        pos.set(source.getHeadPos()); //object
        pos.sub(head);
        if (parent != null) pos.mult(parent.invRotMat);
        pos.mult(target.invMat);
        
        Mat3 basis = Mat3.identity();
        basis.mult(target.invMat);
        if (target.inheritRotation) basis.mult(parent.invRotMat);
        basis.mult(source.rotMatrix);
        basis.mult(source.matrix); //is this where it should be?
        basis.mult(target.matrix);
        
        target.finalTransform.rotation.setRotation(basis);
    }
}
