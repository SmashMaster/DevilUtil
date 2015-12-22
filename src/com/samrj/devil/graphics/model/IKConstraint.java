package com.samrj.devil.graphics.model;

import com.samrj.devil.io.IOUtil;
import com.samrj.devil.math.Mat3;
import com.samrj.devil.math.Quat;
import com.samrj.devil.math.Util;
import com.samrj.devil.math.Vec3;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class IKConstraint implements BoneSolver.Solvable
{
    public final String boneName;
    public final String targetName;
    public final String poleName;
    public final float poleAngle;
    
    private Bone end, start, parent;
    private Bone target, pole;
    
    private float d1, d2;
    
    IKConstraint(DataInputStream in) throws IOException
    {
        boneName = IOUtil.readPaddedUTF(in);
        targetName = IOUtil.readPaddedUTF(in);
        poleName = IOUtil.readPaddedUTF(in);
        poleAngle = in.readFloat();
    }
    
    void populate(Armature armature)
    {
        end = armature.getBone(boneName);
        start = end.getParent();
        parent = start.getParent();
        target = armature.getBone(targetName);
        pole = armature.getBone(poleName);
        
        d1 = Vec3.dist(start.head, end.head);
        d2 = Vec3.dist(end.head, end.tail);
    }
    
    private float signedAngle(Vec3 a, Vec3 b, Vec3 axis)
    {
        a = Vec3.reject(a, axis).normalize();
        b = Vec3.reject(b, axis).normalize();
        float sign = Util.signum(Vec3.dot(axis, a.cross(b)));
        float angle = (float)Math.acos(Vec3.dot(a, b));
        return sign*angle;
    }
    
    @Override
    public void solve()
    {
        Quat rot = start.transform.rotation.setIdentity();
        Vec3 headPos = start.getHeadPos();
        
        Vec3 ikAxis = target.getHeadPos().sub(headPos);
        float x = ikAxis.length();
        ikAxis.div(x);
        Vec3 poleAxis = pole.getHeadPos().sub(headPos).reject(ikAxis).normalize();
        Vec3 yAxis = Vec3.cross(ikAxis, poleAxis).normalize();
        
        //An idea: Create orthogonal basis vectors then convert into transform.
        //X -> ik axis
        //Y -> calculate from X and Z
        //Z -> rejected pole direction
        
        Mat3 basis = new Mat3(ikAxis.x, yAxis.x, poleAxis.x,
                              ikAxis.y, yAxis.y, poleAxis.y,
                              ikAxis.z, yAxis.z, poleAxis.z);
        
        Mat3 rotMat = Mat3.identity();
        rotMat.mult(start.invMat);
        if (parent != null) rotMat.mult(parent.invRotMat);
        rotMat.mult(basis);
        
        rot.setRotation(rotMat).normalize();
    }
    
    public Bone getEnd()
    {
        return end;
    }
    
    public Bone getStart()
    {
        return start;
    }
    
    public Bone getParent()
    {
        return parent;
    }
    
    public Bone getTarget()
    {
        return target;
    }
    
    public Bone getPole()
    {
        return pole;
    }
}
