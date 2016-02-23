package com.samrj.devil.geo3d;

import com.samrj.devil.math.Mat3;
import com.samrj.devil.math.Vec3;

/**
 * Oriented bounding box class.
 *
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class OBox3
{
    private static final float EPSILON = 1.0f/65536.0f;
    
    public final Vec3 pos = new Vec3();
    public final Mat3 rot = new Mat3();
    public final Vec3 sca = new Vec3();
    
    public OBox3()
    {
    }
    
    public OBox3(OBox3 box)
    {
        pos.set(box.pos);
        rot.set(box.rot);
        sca.set(box.sca);
    }
    
    public boolean touching(OBox3 b)
    {
        Mat3 m = Mat3.mult(rot, b.rot);
        Vec3 t = Vec3.sub(b.pos, pos).mult(rot);
        
        Mat3 absM = new Mat3();
        for (int i=0; i<3; i++) for (int j=0; j<3; j++)
            absM.setEntry(i, j, Math.abs(m.getEntry(i, j)) + EPSILON);
        
        Vec3 temp = new Vec3();
        float ra, rb;
        
        for (int i=0; i<3; i++)
        {
            ra = sca.getComponent(i);
            rb = b.sca.dot(temp.setAsRow(absM, i));
            if (Math.abs(t.getComponent(i)) > ra + rb) return false;
        }
        
        for (int i=0; i<3; i++)
        {
            ra = sca.dot(temp.setAsColumn(absM, i));
            rb = b.sca.getComponent(i);
            if (Math.abs(t.dot(temp.setAsColumn(m, i))) > ra + rb) return false;
        }
        
        //One of these seperating axis tests is incorrect.
        
        ra = sca.y*absM.g + sca.z*absM.d;
        rb = b.sca.y*absM.c + b.sca.z*absM.b;
        if (Math.abs(t.z*m.d - t.y*m.g) > ra + rb) return false;
        
        ra = sca.y*absM.h + sca.z*absM.e;
        rb = b.sca.x*absM.c + b.sca.z*absM.a;
        if (Math.abs(t.z*m.e - t.y*m.h) > ra + rb) return false;
        
        ra = sca.y*absM.i + sca.z*absM.f;
        rb = b.sca.x*absM.b + b.sca.y*absM.a;
        if (Math.abs(t.z*m.f - t.y*m.i) > ra + rb) return false;
        
        ra = sca.x*absM.g + sca.z*absM.a;
        rb = b.sca.y*absM.f + b.sca.z*absM.e;
        if (Math.abs(t.x*m.g - t.z*m.a) > ra + rb) return false;
        
        ra = sca.x*absM.h + sca.z*absM.b;
        rb = b.sca.x*absM.f + b.sca.z*absM.d;
        if (Math.abs(t.x*m.h - t.z*m.b) > ra + rb) return false;

        ra = sca.x*absM.i + sca.z*absM.c;
        rb = b.sca.x*absM.e + b.sca.y*absM.d;
        if (Math.abs(t.x*m.i - t.z*m.c) > ra + rb) return false;

        ra = sca.x*absM.d + sca.y*absM.a;
        rb = b.sca.y*absM.i + b.sca.z*absM.h;
        if (Math.abs(t.y*m.a - t.x*m.d) > ra + rb) return false;

        ra = sca.x*absM.e + sca.y*absM.b;
        rb = b.sca.x*absM.i + b.sca.z*absM.g;
        if (Math.abs(t.y*m.b - t.x*m.e) > ra + rb) return false;

        ra = sca.x*absM.f + sca.y*absM.c;
        rb = b.sca.x*absM.h + b.sca.y*absM.g;
        if (Math.abs(t.y*m.c - t.x*m.f) > ra + rb) return false;
        
        return true;
    }
}
