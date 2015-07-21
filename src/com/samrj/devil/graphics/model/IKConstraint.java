package com.samrj.devil.graphics.model;

import com.samrj.devil.math.Mat4;
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
    private static float getPoleAngle(Vec3 pole, Mat4 startMat, Vec3 start, Vec3 end)
    {
        Vec3 x = Vec3.sub(end, start).normalize();
        Vec3 z = new Vec3(0, 0, 1).mult(startMat);
        Vec3.cross(x, z, z);
        Vec3 y = Vec3.cross(z, x);
        
        Vec3 localPole = Vec3.sub(pole, start);
        float poleY = localPole.scalarProject(y);
        float poleZ = localPole.scalarProject(z);
        
        return (float)Math.atan2(poleZ, poleY);
    }
    
    public final Bone parent, start, end, target, pole;
    public final float poleAngle, initialPoleAngle;
    public final float startSqLen, endSqLen, startLen, endLen, length;
    
    public IKConstraint(DataInputStream in, Bone[] bones) throws IOException
    {
        int boneIndex = in.readInt();
        int targetIndex = in.readInt();
        int poleIndex = in.readInt();
        poleAngle = in.readFloat();
        
        end = bones[boneIndex];
        start = end.getParent();
        parent = start.getParent();
        target = bones[targetIndex];
        pole = bones[poleIndex];
        
        startSqLen = start.tail.squareDist(start.head);
        endSqLen = end.tail.squareDist(start.tail);
        startLen = (float)Math.sqrt(startSqLen);
        endLen = (float)Math.sqrt(endSqLen);
        length = startLen + endLen;
        
        initialPoleAngle = getPoleAngle(pole.head, start.baseMatrix, start.head, end.tail);
    }
    
    @Override
    public void solve()
    {
        start.solveHeadPosition();
        
        float curPoleAngle = getPoleAngle(pole.headFinal, start.baseMatrix, start.headFinal, target.headFinal);
        float roll = curPoleAngle + poleAngle + initialPoleAngle;
        
        float distSq = Vec3.squareDist(start.headFinal, target.headFinal);
        float dist = (float)Math.sqrt(distSq);
        float chordX = (distSq - endSqLen + startSqLen)/(dist*2.0f);
        float chordY = (float)Math.sqrt(startSqLen - chordX*chordX);
        float pitch = (float)Math.atan2(chordY, chordX);
        
        start.reachTowards(target.headFinal);
        start.rotation.rotate(new Vec3(1, 0, 0), roll);
        start.rotation.rotate(new Vec3(0, 0, 1), pitch);
        start.solveRotationMatrix();
        start.solveTailPosition(); start.solveMatrix();
        
        end.solveHeadPosition();
        end.reachTowards(target.headFinal);
        end.solveRotationMatrix();
        end.solveTailPosition(); end.solveMatrix();
    }
}
