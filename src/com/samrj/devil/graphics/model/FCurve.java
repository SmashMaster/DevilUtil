package com.samrj.devil.graphics.model;

import com.samrj.devil.io.IOUtil;
import com.samrj.devil.math.Vec2;
import java.io.DataInputStream;
import java.io.IOException;

public class FCurve
{
    public String boneName;
    public int propertyID;
    public int arrayIndex;
    public Keyframe[] keyframes;
    
    FCurve(DataInputStream in) throws IOException
    {
        boolean hasBone = in.readShort() == 1;
        propertyID = in.readShort();
        boneName = hasBone ? IOUtil.readPaddedUTF(in) : null;
        arrayIndex = in.readInt();
        keyframes = IOUtil.arrayFromStream(in, Keyframe.class, Keyframe::new);
    }
    
    public class Keyframe
    {
        public final int interpolation;
        public final Vec2 co, left, right;
        
        Keyframe(DataInputStream in) throws IOException
        {
            interpolation = in.readInt();
            co = new Vec2(in);
            left = new Vec2(in);
            right = new Vec2(in);
        }
    }
}
