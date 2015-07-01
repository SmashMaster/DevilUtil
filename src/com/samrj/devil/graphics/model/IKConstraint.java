package com.samrj.devil.graphics.model;

import com.samrj.devil.math.Quat4f;
import com.samrj.devil.math.Util;
import com.samrj.devil.math.Vector3f;
import java.io.DataInputStream;
import java.io.IOException;

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
        startLen = Util.sqrt(startSqLen);
        endLen = Util.sqrt(endSqLen);
        length = startLen + endLen;
    }
    
    @Override
    public void solve()
    {
        start.solveHeadPosition();
        
        Vector3f dir = target.headFinal.csub(start.headFinal);
        float distSq = dir.squareLength();
        float dist = Util.sqrt(distSq);
        dir.div(dist);
        
        Vector3f poleDir = poleTarget.headFinal.csub(start.headFinal).normalize();
        Vector3f hingeAxis = poleDir.copy().cross(dir).normalize();
        Vector3f kneePos = new Vector3f();
        
        if (dist < length)
        {
            Vector3f chordYDir = hingeAxis.copy().cross(dir).negate().normalize();
            float chordX = (distSq - endSqLen + startSqLen)/(dist*2.0f);
            Vector3f chordCenter = dir.cmult(chordX).add(start.headFinal);
            float chordY = Util.sqrt(startSqLen - chordX*chordX);
            kneePos.set(chordYDir).mult(chordY).add(chordCenter);
        }
        else kneePos.set(dir).mult(startLen).add(start.headFinal);
        
        start.reachTowards(kneePos);
        start.solveRotationMatrix(); //Solve rotation matrix then correct roll error
        
        //Roll calculation very slightly different from Blender.
        //Until fixed, should not use inherit rotation for first bone after IK.
        Vector3f localRollTarget = hingeAxis.copy(); //Global
        localRollTarget.mult(start.inverseRotMatrix);
        localRollTarget.mult(start.inverseBaseMatrix);
        float rollAngle = Util.atan2(localRollTarget.z, localRollTarget.y);
        start.rotation.mult(Quat4f.axisAngle(Util.Axis.X, Util.reduceRad(rollAngle + poleAngle)));
        
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