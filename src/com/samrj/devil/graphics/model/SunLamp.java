package com.samrj.devil.graphics.model;

import com.samrj.devil.math.Quat;
import com.samrj.devil.math.Vec3;
import java.io.DataInputStream;
import java.io.IOException;

public class SunLamp
{
    public final String name;
    public final Quat rot;
    public final float softShadowSize;
    public final Vec3 color;
    
    SunLamp(DataInputStream in) throws IOException
    {
        name = Model.readPaddedUTF(in);
        
        rot = new Quat();
        rot.read(in);
        
        softShadowSize = in.readInt();
        
        color = new Vec3();
        color.read(in);
    }
}
