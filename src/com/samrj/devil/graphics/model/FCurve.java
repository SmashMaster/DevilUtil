package com.samrj.devil.graphics.model;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.TreeMap;

public class FCurve
{
    public final int boneIndex;
    public final Bone.Property property;
    public final int propIndex;
    public final Keyframe[] keyframes;
    public final float minX, maxX;
    
    private final TreeMap<Float, Integer> keyframeIndices;
    
    public FCurve(DataInputStream in) throws IOException
    {
        boneIndex = in.readInt();
        switch (DevilModel.readPaddedUTF(in))
        {
            case "LC": property = Bone.Property.LOCATION; break;
            case "RT": property = Bone.Property.ROTATION; break;
            default: throw new IllegalArgumentException();
        }
        propIndex = in.readInt();
        int numKeyframes = in.readInt();
        keyframes = new Keyframe[numKeyframes];
        float min = Float.POSITIVE_INFINITY, max = Float.NEGATIVE_INFINITY;
        keyframeIndices = new TreeMap<>();
        for (int i=0; i<numKeyframes; i++)
        {
            keyframes[i] = new Keyframe(in);
            float x = keyframes[i].coord.x;
            if (x < min) min = x;
            if (x > max) max = x;
            keyframeIndices.put(x, i);
        }
        minX = min; maxX = max;
        
        for (int i0=0; i0<numKeyframes - 1; i0++)
            Keyframe.validate(keyframes[i0], keyframes[i0 + 1]);
    }
    
    public float evaluate(float time)
    {
        Entry<Float, Integer> e0 = keyframeIndices.floorEntry(time);
        if (e0 == null) return keyframes[0].coord.y; //Before first
        
        int i0 = e0.getValue();
        Keyframe k0 = keyframes[i0];
        if (i0 == keyframes.length - 1) return k0.coord.y; //After last
        
        Keyframe k1 = keyframes[i0 + 1];
        return Keyframe.evaluate(k0, k1, time);
    }
    
    public void apply(Armature armature, float time)
    {
        Bone bone = armature.bones[boneIndex];
        float value = evaluate(time);
        bone.set(property, propIndex, value);
    }
}
