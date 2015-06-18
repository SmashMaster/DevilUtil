package com.samrj.devil.graphics.model;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.TreeMap;

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
    
    private final TreeMap<Float, Integer> keyframeIndices;
    
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
        keyframeIndices = new TreeMap<>();
        for (int i=0; i<numKeyframes; i++)
        {
            keyframes[i] = new Keyframe(in);
            keyframeIndices.put(keyframes[i].coord.x, i);
        }
    }
    
    public float evaluate(float time)
    {
        Entry<Float, Integer> e0 = keyframeIndices.floorEntry(time);
        if (time <= e0.getKey()) return keyframes[0].coord.y; //Before first
        
        int i0 = e0.getValue();
        Keyframe k0 = keyframes[i0];
        if (i0 == keyframes.length - 1) return k0.coord.y; //After last
        
        Keyframe k1 = keyframes[i0 + 1];
        return Keyframe.evaluate(k0, k1, time);
    }
}
