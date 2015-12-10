package com.samrj.devil.graphics.model;

import com.samrj.devil.math.Vec3;
import java.io.DataInputStream;
import java.io.IOException;

public class Transform
{
    public final Vec3 position;
    public final Vec3 scale;
    
    Transform(DataInputStream in) throws IOException
    {
        position = new Vec3(in);
        if (in.readInt() >= 0) in.skip(16); //Rotation
        scale = new Vec3(in);
    }
}
