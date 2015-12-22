package com.samrj.devil.graphics.model;

import com.samrj.devil.io.IOUtil;
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
    
    @Override
    public void solve()
    {
        //I dont even know
    }
    
    public Bone getEnd()
    {
        return end;
    }
    
    public Bone getStart()
    {
        return start;
    }
    
    @Override
    public Bone getParent()
    {
        return parent;
    }
}
