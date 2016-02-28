package com.samrj.devil.model;

import com.samrj.devil.math.Mat3;
import com.samrj.devil.math.Vec3;
import com.samrj.devil.math.topo.DAG;
import com.samrj.devil.model.Armature.Bone;
import java.util.Set;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class CopyTransformConstraint implements MeshSkinner.Solvable
{
    private final PoseBone source, parent, target;
    
    public CopyTransformConstraint(PoseBone source, PoseBone target)
    {
        this.source = source;
        parent = target.getParent();
        this.target = target;
    }

    @Override
    public void populateSolveGraph(DAG<MeshSkinner.Solvable> graph)
    {
        graph.addEdge(source, this);
        if (parent != null) graph.addEdge(parent, this);
        graph.addEdge(this, target);
    }
    
    @Override
    public void removeSolved(Set<PoseBone> independent)
    {
        independent.remove(target);
    }
    
    @Override
    public void solve()
    {
        Bone targetBone = target.getBone();
        
        Vec3 head = new Vec3(targetBone.head);
        if (parent != null) head.mult(parent.skinMatrix);
        
        Vec3 pos = target.finalTransform.position;
        pos.set(source.getHeadPos()); //object
        pos.sub(head);
        if (parent != null) pos.mult(parent.invRotMat);
        pos.mult(targetBone.invMat);
        
        Mat3 basis = Mat3.identity();
        basis.mult(targetBone.invMat);
        if (targetBone.inheritRotation) basis.mult(parent.invRotMat);
        basis.mult(source.rotMatrix);
        basis.mult(source.getBone().matrix); //is this where it should be?
        basis.mult(targetBone.matrix);
        
        target.finalTransform.rotation.setRotation(basis);
    }
}
