package com.samrj.devil.model;

import com.samrj.devil.io.IOUtil;
import com.samrj.devil.math.Util;
import com.samrj.devil.model.Pose.PoseBone;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

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
    
    private final Marker[] markers;
    private final Map<String, Marker> markerMap;
    
    Action(Model model, DataInputStream in) throws IOException
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
        
        markers = IOUtil.arrayFromStream(in, Marker.class, Marker::new);
        markerMap = new HashMap<>(markers.length);
        for (Marker marker : markers) markerMap.put(marker.name, marker);
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
    
    public Pose evaluate(Pose pose, float time)
    {
        for (FCurve fcurve : fcurves) fcurve.apply(pose, time);
        for (PoseBone bone : pose.getBones()) bone.transform.rotation.normalize();
        return pose;
    }
    
    public Pose evaluate(float time)
    {
        return evaluate(new Pose(), time);
    }
    
    public Marker getMarker(String name)
    {
        return markerMap.get(name);
    }
    
    public Stream<Marker> passMarkers(float start, float end)
    {
        if (end == start) return Stream.of();
        else if (end > start) return Stream.of(markers)
                .filter(m -> m.frame > start && m.frame <= end)
                .sorted((a, b) -> Util.compare(a.frame, b.frame));
        else return Stream.of(markers)
                .filter(m -> m.frame < start && m.frame >= end)
                .sorted((a, b) -> Util.compare(b.frame, a.frame));
    }
    
    @Override
    public String getName()
    {
        return name;
    }
    
    public class Marker
    {
        public final String name;
        public final float frame;
        
        private Marker(DataInputStream in) throws IOException
        {
            name = IOUtil.readPaddedUTF(in);
            frame = in.readInt();
        }
        
        @Override
        public String toString()
        {
            return frame + ": \"" + name + "\"";
        }
    }
}
