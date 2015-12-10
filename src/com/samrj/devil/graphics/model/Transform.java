package com.samrj.devil.graphics.model;

import com.samrj.devil.math.Mat3;
import com.samrj.devil.math.Mat4;
import com.samrj.devil.math.Quat;
import com.samrj.devil.math.Vec3;
import java.io.DataInputStream;
import java.io.IOException;

public class Transform
{
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
        //Apply scaling
    }
    
    public void set(Transform transform)
    {
        Vec3.copy(transform.position, position);
        Quat.copy(transform.rotation, rotation);
        Vec3.copy(transform.scale, scale);
    }
}
