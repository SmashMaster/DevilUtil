package com.samrj.devil.graphics.model;

import java.io.DataInputStream;
import java.io.IOException;
import org.lwjgl.opengl.GL11;

public class IKConstraint implements Solvable
{
    public final Bone end, start, target, poleTarget;
    
    public IKConstraint(DataInputStream in, Bone[] bones) throws IOException
    {
        int boneIndex = in.readInt();
        int targetIndex = in.readInt();
        int poleTargetIndex = in.readInt();
        
        end = bones[boneIndex];
        start = end.getParent();
        target = bones[targetIndex];
        poleTarget = bones[poleTargetIndex];
    }
    
    @Override
    public void solve()
    {
        GL11.glColor3f(1.0f, 0.0f, 1.0f);
        GL11.glLineWidth(1.0f);
        GL11.glBegin(GL11.GL_LINES);
        start.headFinal.glVertex();
        target.headFinal.glVertex();
        GL11.glEnd();
        
        start.solve();
        end.solve();
    }
}
