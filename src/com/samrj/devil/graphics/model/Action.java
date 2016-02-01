package com.samrj.devil.graphics.model;

import com.samrj.devil.io.IOUtil;
import com.samrj.devil.math.Util;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Action implements DataBlock
{
    public final String name;
    public final FCurve[] fcurves;
    public final float minX, maxX;
    
    Action(DataInputStream in) throws IOException
    {
        name = IOUtil.readPaddedUTF(in);
        fcurves = IOUtil.arrayFromStream(in, FCurve.class, FCurve::new);
        
        float min = Float.POSITIVE_INFINITY, max = Float.NEGATIVE_INFINITY;
        for (int i=0; i<fcurves.length; i++)
        {
            if (fcurves[i].minX < min) min = fcurves[i].minX;
            if (fcurves[i].maxX > max) max = fcurves[i].maxX;
        }
        minX = min; maxX = max;
    }
    
    public float loop(float time)
    {
        return Util.loop(time, minX, maxX);
    }
    
    public float clamp(float time)
    {
        return Util.clamp(time, minX, maxX);
    }
    
    public float envelope(float time, float fadeIn, float fadeOut)
    {
        return Util.envelope(time, minX, fadeIn, fadeOut, maxX);
    }
    
    public Pose evaluate(float time)
    {
        Set<String> names = new HashSet<>();
        for (FCurve fcurve : fcurves) names.add(fcurve.boneName);
        Pose pose = new Pose(names);
        for (FCurve fcurve : fcurves) fcurve.apply(pose, time);
        return pose;
    }

    @Override
    public String getName()
    {
        return name;
    }
}
