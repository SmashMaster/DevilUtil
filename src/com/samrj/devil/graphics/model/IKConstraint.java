package com.samrj.devil.graphics.model;

import com.samrj.devil.math.Quat4f;
import com.samrj.devil.math.Util;
import com.samrj.devil.math.Vector3f;
import java.io.DataInputStream;
import java.io.IOException;
import org.lwjgl.opengl.GL11;

public class IKConstraint implements Solvable
{
    public final Bone parent, start, end, target, poleTarget;
    public final float startSqLen, endSqLen, length;
    
    public IKConstraint(DataInputStream in, Bone[] bones) throws IOException
    {
        int boneIndex = in.readInt();
        int targetIndex = in.readInt();
        int poleTargetIndex = in.readInt();
        
        end = bones[boneIndex];
        start = end.getParent();
        parent = start.getParent();
        target = bones[targetIndex];
        poleTarget = bones[poleTargetIndex];
        
        startSqLen = start.tail.squareDist(start.head);
        endSqLen = end.tail.squareDist(start.tail);
        length = Util.sqrt(startSqLen) + Util.sqrt(endSqLen);
    }
    
    @Override
    public void solve()
    {
        start.solveHeadPosition();
        
        Vector3f dir = target.headFinal.csub(start.headFinal);
        float distSq = dir.squareLength();
        float dist = Util.sqrt(distSq);
        dir.div(dist);
        
        float chordDist = (distSq - endSqLen + startSqLen)/(dist*2.0f);
        float chordLen = Util.sqrt(startSqLen - chordDist*chordDist);
        
        Vector3f chordCenter = dir.cmult(chordDist).add(start.headFinal);
        
        Vector3f chordDir = poleTarget.headFinal.csub(start.headFinal).normalize();
        chordDir.cross(dir).normalize(); //Perpendicular to chord direction and target direction
        chordDir.cross(dir).negate().normalize(); //Perpendicular to target dir and faces towards pole target
        
        Vector3f kneePos = chordDir.cmult(chordLen).add(chordCenter);
        
        //Just need to figure out how to set the two bones' rotations now
        
        GL11.glColor3f(1.0f, 0.0f, 1.0f);
        GL11.glLineWidth(1.0f);
        GL11.glBegin(GL11.GL_LINE_STRIP);
        start.headFinal.glVertex();
        kneePos.glVertex();
        target.headFinal.glVertex();
        GL11.glEnd();
        
        start.reachTowards(kneePos);
        
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