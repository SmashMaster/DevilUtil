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
    
    public final Vec3 position;
    public final Quat rotation;
    public final Vec3 scale;
    
    public Transform(DataInputStream in) throws IOException
    {
        position = new Vec3(in);
        rotation = in.readInt() == 0 ? new Quat(in) : new Quat();
        scale = new Vec3(in);
    }
    
    public Transform()
    {
        position = new Vec3();
        rotation = Quat.identity();
        scale = new Vec3(1.0f);
    }
    
    public Transform(Transform transform)
    {
        position = new Vec3(transform.position);
        rotation = new Quat(transform.rotation);
        scale = new Vec3(transform.scale);
    }
    
    public void apply(Mat4 matrix)
    {
        matrix.translate(position);
        matrix.rotate(rotation);
        matrix.mult(scale);
    }
    
    public void apply(Mat3 matrix)
    {
        matrix.rotate(rotation);
        matrix.mult(scale);
    }
    
    public void setIdentity()
    {
        position.set();
        rotation.setIdentity();
        scale.set(1.0f);
    }
    
    public void set(Transform transform)
    {
        position.set(transform.position);
        rotation.set(transform.rotation);
        scale.set(transform.scale);
    }
    
    public void mix(Transform transform, float t)
    {
        position.lerp(transform.position, t);
        rotation.slerp(transform.rotation, t);
        scale.lerp(transform.scale, t);
    }
    
    public void setProperty(Property property, int index, float value)
    {
        switch (property)
        {
            case POSITION: position.setComponent(index, value); return;
            case ROTATION: rotation.setComponent(index, value); return;
            case SCALE: scale.setComponent(index, value);
        }
    }
    
    @Override
    public String toString()
    {
        return "{ " + position + " " + rotation + " " + scale + " }";
    }
}
