package com.samrj.devil.graphics.model;

import com.samrj.devil.io.IOUtil;
import com.samrj.devil.math.Mat3;
import com.samrj.devil.math.Quat;
import com.samrj.devil.math.Util;
import com.samrj.devil.math.Vec3;
import com.samrj.devil.math.topo.DAG;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Set;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class IKConstraint implements BoneSolver.Solvable
{
    private static float nrm(Vec3 v)
    {
        float len = v.length();
        v.div(len);
        return len;
    }
    
    public final String boneName;
    public final String targetName;
    public final String poleName;
    public final float poleAngle;
    
    private Bone end, start, parent;
    private Bone target, pole;
    
    private float d1, d2;
    private final Vec3 hinge1, hinge2;
    private float ang2init;
    
    IKConstraint(DataInputStream in) throws IOException
    {
        boneName = IOUtil.readPaddedUTF(in);
        targetName = IOUtil.readPaddedUTF(in);
        poleName = IOUtil.readPaddedUTF(in);
        poleAngle = in.readFloat();
        hinge1 = new Vec3();
        hinge2 = new Vec3();
    }
    
    void populate(Armature armature)
    {
        end = armature.getBone(boneName);
        start = end.getParent();
        parent = start.getParent();
        target = armature.getBone(targetName);
        pole = armature.getBone(poleName);
        
        Vec3 dp1 = Vec3.sub(end.head, start.head);
        Vec3 dp2 = Vec3.sub(end.tail, end.head);
        d1 = nrm(dp1);
        d2 = nrm(dp2);
        
        Vec3 hinge = Vec3.cross(dp1, dp2);
        float hingeLen = nrm(hinge);
        Vec3.mult(hinge, start.invMat, hinge1);
        Vec3.mult(hinge, end.invMat, hinge2);
        
        ang2init = (float)Math.atan2(hingeLen, dp1.dot(dp2));
    }
    
    private void toStart(Vec3 v)
    {
        if (parent != null) v.mult(parent.invRotMat);
        v.mult(start.invMat);
    }
    
    @Override
    public void populateSolveGraph(DAG<BoneSolver.Solvable> graph)
    {
        graph.addEdge(parent, this);
        graph.addEdge(target, this);
        graph.addEdge(pole, this);
        graph.addEdge(this, start);
    }

    @Override
    public void removeSolved(Set<Bone> independent)
    {
        independent.remove(start);
        independent.remove(end);
    }
    
    @Override
    public void solve()
    {
        start.finalTransform.setIdentity();
        end.finalTransform.setIdentity();
        
        Quat rot1 = start.finalTransform.rotation;
        Quat rot2 = end.finalTransform.rotation;
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
        rot1.rotate(new Vec3(1,0,0), poleAngle);
        
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
}
