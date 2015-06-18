package com.samrj.devil.graphics.model;

import java.io.DataInputStream;
import java.io.IOException;

public class FCurve
{
    public static enum Property
    {
        LOCATION, ROTATION;
    }
    
    public final int boneIndex;
    public final Property property;
    public final int component;
    public final Keyframe[] keyframes;
    
    public FCurve(DataInputStream in) throws IOException
    {
        boneIndex = in.readInt();
        switch (DevilModel.readPaddedUTF(in))
        {
            case "LC": property = Property.LOCATION; break;
            case "RT": property = Property.ROTATION; break;
            default: throw new IllegalArgumentException();
        }
        component = in.readInt();
        int numKeyframes = in.readInt();
        keyframes = new Keyframe[numKeyframes];
        for (int i=0; i<numKeyframes; i++)
        {
            keyframes[i] = new Keyframe(in);
        }
    }
}
