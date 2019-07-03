package com.samrj.devil.model.constraint;

import com.samrj.devil.math.Mat3;
import com.samrj.devil.math.Quat;
import com.samrj.devil.math.Util;
import com.samrj.devil.math.Vec3;
import com.samrj.devil.math.topo.DAG;
import com.samrj.devil.model.ArmatureSolver;
import com.samrj.devil.model.ArmatureSolver.BoneSolver;
import java.io.IOException;
import java.util.Set;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class IKConstraint implements ArmatureSolver.Constraint
{
    private static float nrm(Vec3 v)
    {
        float len = v.length();
        v.div(len);
        return len;
    }
    
    private final BoneSolver end, start, parent;
    private final BoneSolver target, pole;
    private final float poleAngle;
    private final float d1, d2;
    private final Vec3 hinge1 = new Vec3(), hinge2 = new Vec3();
    private final float ang2init;
    
    public IKConstraint(IKDefinition def, ArmatureSolver solver)
    {
        end = solver.getBone(def.boneName);
        start = end.getParent();
        parent = start.getParent();
        target = solver.getBone(def.targetName);
        pole = solver.getBone(def.poleName);
        poleAngle = def.poleAngle;
        
        Vec3 dp1 = Vec3.sub(end.bone.head, start.bone.head);
        Vec3 dp2 = Vec3.sub(end.bone.tail, end.bone.head);
        d1 = nrm(dp1);
        d2 = nrm(dp2);
        
        Vec3 hinge = Vec3.cross(dp1, dp2);
        float hingeLen = nrm(hinge);
        Vec3.mult(hinge, start.bone.invMat, hinge1);
        Vec3.mult(hinge, end.bone.invMat, hinge2);
        
        ang2init = (float)Math.atan2(hingeLen, dp1.dot(dp2));
    }
    
    private void toStart(Vec3 v)
    {
        if (parent != null) v.mult(parent.invRotMat);
        v.mult(start.bone.invMat);
    }
    
    @Override
    public void populateSolveGraph(DAG<ArmatureSolver.Constraint> graph)
    {
        graph.add(this);
        
        graph.addEdge(parent, this);
        graph.addEdge(target, this);
        graph.addEdge(pole, this);
        graph.addEdge(this, start);
    }

    @Override
    public void removeSolved(Set<BoneSolver> nonconstrained)
    {
        nonconstrained.remove(start);
        nonconstrained.remove(end);
    }
    
    @Override
    public void solve()
    {
        start.finalTransform.setIdentity();
        end.finalTransform.setIdentity();
        
        Quat rot1 = start.finalTransform.rot;
        Quat rot2 = end.finalTransform.rot;
        Vec3 headPos = start.getHeadPos();
        
        //Create basis vectors for bone orientation.
        Vec3 ikAxis = target.getHeadPos().sub(headPos);
        Vec3 poleAxis = pole.getHeadPos().sub(headPos).reject(ikAxis).normalize();
        float x = nrm(ikAxis);
        toStart(ikAxis);
        toStart(poleAxis);
        Vec3 yAxis = Vec3.cross(poleAxis, ikAxis).normalize();
        Mat3 basis = new Mat3(ikAxis.x, yAxis.x, poleAxis.x,
                              ikAxis.y, yAxis.y, poleAxis.y,
                              ikAxis.z, yAxis.z, poleAxis.z);
        rot1.mult(Quat.rotation(basis));
        rot1.rotate(new Vec3(1, 0, 0), poleAngle);
        
        if (x < d1 + d2) //Calculate IK angles and perform hinge rotations.
        {
            float ang1 = (float)Math.acos((d1*d1 + x*x - d2*d2)/(2.0f*d1*x));
            rot1.rotate(hinge1, -ang1);
            float ang2 = (float)Math.acos((d1*d1 + d2*d2 - x*x)/(2.0f*d1*d2));
            rot2.rotate(hinge2, Util.PI - ang2 - ang2init);
        }
        else rot2.rotate(hinge2, -ang2init); //Reach towards target.
        //Also need cases for targets that are too close.
    }
    
    public static class IKDefinition
    {
        public final String boneName;
        public final String targetName;
        public final String poleName;
        public final float poleAngle;
        
        public IKDefinition(String boneName, String targetName, String poleName, float poleAngle) throws IOException
        {
            this.boneName = boneName;
            this.targetName = targetName;
            this.poleName = poleName;
            this.poleAngle = poleAngle;
        }
    }
}
