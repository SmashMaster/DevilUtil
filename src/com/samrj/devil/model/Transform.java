package com.samrj.devil.model;

import com.samrj.devil.math.Mat3;
import com.samrj.devil.math.Mat4;
import com.samrj.devil.math.Quat;
import com.samrj.devil.math.Vec3;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Transform
{
    public enum Property
    {
        POSITION, ROTATION, SCALE;
    }
    
    static Property propFromID(int i)
    {
        switch (i)
        {
            case 0: return Property.POSITION;
            case 1: return Property.ROTATION;
            case 3: return Property.SCALE;
            default: return null;
        }
    }
    
    public final Vec3 pos;
    public final Quat rot;
    public final Vec3 sca;
    
    public Transform(DataInputStream in) throws IOException
    {
        pos = new Vec3(in);
        rot = new Quat(in);
        sca = new Vec3(in);
    }
    
    public Transform()
    {
        pos = new Vec3();
        rot = Quat.identity();
        sca = new Vec3(1.0f);
    }
    
    public Transform(Transform transform)
    {
        pos = new Vec3(transform.pos);
        rot = new Quat(transform.rot);
        sca = new Vec3(transform.sca);
    }
    
    public Mat4 apply(Mat4 matrix)
    {
        matrix.translate(pos);
        matrix.rotate(rot);
        matrix.mult(sca);
        return matrix;
    }
    
    public Mat3 apply(Mat3 matrix)
    {
        matrix.rotate(rot);
        matrix.mult(sca);
        return matrix;
    }
    
    public Vec3 apply(Vec3 vector)
    {
        vector.mult(sca);
        vector.mult(rot);
        vector.add(pos);
        return vector;
    }
    
    public Mat4 toMatrix()
    {
        Mat4 out = Mat4.identity();
        apply(out);
        return out;
    }
    
    public void setIdentity()
    {
        pos.set();
        rot.setIdentity();
        sca.set(1.0f);
    }
    
    public void set(Transform transform)
    {
        pos.set(transform.pos);
        rot.set(transform.rot);
        sca.set(transform.sca);
    }
    
    public void mix(Transform transform, float t)
    {
        pos.lerp(transform.pos, t);
        rot.slerp(transform.rot, t);
        sca.lerp(transform.sca, t);
    }
    
    public void lerp(Transform transform, float t)
    {
        pos.lerp(transform.pos, t);
        rot.lerp(transform.rot, t);
        rot.normalize();
        sca.lerp(transform.sca, t);
    }
    
    public void mult(Transform transform)
    {
        pos.mult(transform.rot);
        pos.mult(transform.sca);
        pos.add(transform.pos);
        rot.mult(transform.rot);
        sca.mult(transform.sca);
    }
    
    public void setProperty(Property property, int index, float value)
    {
        switch (property)
        {
            case POSITION: pos.setComponent(index, value); return;
            case ROTATION: rot.setComponent(index, value); return;
            case SCALE: sca.setComponent(index, value);
        }
    }
    
    @Override
    public String toString()
    {
        return "{ " + pos + " " + rot + " " + sca + " }";
    }
}
