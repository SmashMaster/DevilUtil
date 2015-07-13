package com.samrj.devil.graphics.model;

import com.samrj.devil.math.Quat;
import com.samrj.devil.math.Util;
import com.samrj.devil.math.Vec3;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * DevilModel inverse kinematics constraint.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class IKConstraint implements Solvable
{
    public final Bone parent, start, end, target, poleTarget;
    public final float poleAngle;
    public final float startSqLen, endSqLen, startLen, endLen, length;
    
    public IKConstraint(DataInputStream in, Bone[] bones) throws IOException
    {
        int boneIndex = in.readInt();
        int targetIndex = in.readInt();
        int poleTargetIndex = in.readInt();
        poleAngle = in.readFloat();
        
        end = bones[boneIndex];
        start = end.getParent();
        parent = start.getParent();
        target = bones[targetIndex];
        poleTarget = bones[poleTargetIndex];
        
        startSqLen = start.tail.squareDist(start.head);
        endSqLen = end.tail.squareDist(start.tail);
        startLen = (float)Math.sqrt(startSqLen);
        endLen = (float)Math.sqrt(endSqLen);
        length = startLen + endLen;
    }
    
    @Override
    public void solve()
    {
        start.solveHeadPosition();
        
        Vec3 dir = Vec3.sub(target.headFinal, start.headFinal);
        float distSq = dir.squareLength();
        float dist = (float)Math.sqrt(distSq);
        dir.div(dist);
        
        Vec3 poleDir = Vec3.sub(poleTarget.headFinal, start.headFinal).normalize();
        Vec3 hingeAxis = Vec3.cross(poleDir, dir).normalize();
        Vec3 kneePos = new Vec3();
        
        if (dist < length)
        {
            Vec3 chordYDir = Vec3.cross(hingeAxis, dir).negate().normalize();
            float chordX = (distSq - endSqLen + startSqLen)/(dist*2.0f);
            Vec3 chordCenter = Vec3.mult(dir, chordX).add(start.headFinal);
            float chordY = (float)Math.sqrt(startSqLen - chordX*chordX);
            kneePos.set(chordYDir).mult(chordY).add(chordCenter);
        }
        else kneePos.set(dir).mult(startLen).add(start.headFinal);
        
        start.reachTowards(kneePos);
        start.solveRotationMatrix(); //Solve rotation matrix then correct roll error
        
        //Roll calculation very slightly different from Blender.
        //Until fixed, should not use inherit rotation for first bone after IK.
        Vec3 localRollTarget = new Vec3(hingeAxis); //Global
        localRollTarget.mult(start.inverseRotMatrix);
        localRollTarget.mult(start.inverseBaseMatrix);
        float rollAngle = (float)Math.atan2(localRollTarget.z, localRollTarget.y);
        start.rotation.mult(Quat.rotation(new Vec3(1.0f, 0.0f, 0.0f), 
                                          Util.reduceAngle(rollAngle + poleAngle)));
        
        start.solveRotationMatrix();
        start.solveTailPosition();
        start.solveMatrix();
        end.solveHeadPosition();
        
        end.reachTowards(target.headFinal);
        
        end.solveRotationMatrix();
        end.solveTailPosition();
        end.solveMatrix();
    }
}