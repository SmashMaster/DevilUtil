package com.samrj.devil.graphics.model;

import com.samrj.devil.math.Mat4;
import com.samrj.devil.math.Quat;
import com.samrj.devil.math.Vec3;
import java.io.DataInputStream;
import java.io.IOException;

public class Transform
{
    enum Property
    {
        POSITION, ROTATION, SCALE;
    }
    
    static Property propFromID(int i)
    {
        switch (i)
        {
            case 0: return Property.POSITION;
            case 1: return Property.ROTATION;
            case 2: return Property.SCALE;
            default: return null;
        }
    }
    
    public final Vec3 position;
    public final Quat rotation;
    public final Vec3 scale;
    
    Transform(DataInputStream in) throws IOException
    {
        position = new Vec3(in);
        rotation = in.readInt() == 0 ? new Quat(in) : new Quat();
        scale = new Vec3(in);
    }
    
    Transform()
    {
        position = new Vec3();
        rotation = Quat.identity();
        scale = new Vec3();
    }
    
    public void apply(Mat4 matrix)
    {
        matrix.translate(position);
        matrix.rotate(rotation);
        matrix.mult(scale);
    }
    
    public void set(Transform transform)
    {
        position.set(transform.position);
        rotation.set(transform.rotation);
        scale.set(transform.scale);
    }
    
    void setProperty(Property property, int index, float value)
    {
        switch (property)
        {
            case POSITION: position.setComponent(index, value); return;
            case ROTATION: rotation.setComponent(index, value); return;
            case SCALE: scale.setComponent(index, value);
        }
    }
}
