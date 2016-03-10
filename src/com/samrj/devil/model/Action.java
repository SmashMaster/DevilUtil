package com.samrj.devil.model;

import com.samrj.devil.io.IOUtil;
import com.samrj.devil.math.Util;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Action implements DataBlock
{
    public final String name;
    public final FCurve[] fcurves;
    public final Marker[] markers;
    public final float minX, maxX;
    private final Map<String, Marker> markerMap;
    
    Action(Model model, DataInputStream in) throws IOException
    {
        name = IOUtil.readPaddedUTF(in);
        fcurves = IOUtil.arrayFromStream(in, FCurve.class, FCurve::new);
        markers = IOUtil.arrayFromStream(in, Marker.class, Marker::new);
        
        float min = Float.POSITIVE_INFINITY, max = Float.NEGATIVE_INFINITY;
        for (int i=0; i<fcurves.length; i++)
        {
            if (fcurves[i].minX < min) min = fcurves[i].minX;
            if (fcurves[i].maxX > max) max = fcurves[i].maxX;
        }
        minX = min; maxX = max;
        
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
    
    private int getPrevMarkerIndex(float time)
    {
        for (int i=0; i<markers.length; i++) if (markers[i].frame >= time)
            return i - 1;
        return markers.length - 1;
    }
    
    private int getNextMarkerIndex(float time)
    {
        for (int i=0; i<markers.length; i++) if (markers[i].frame >= time)
            return i;
        return -1;
    }
    
    /**
     * For each marker between the two given times, calls the given marker
     * callback. If the starting time is greater than the end, assumes this
     * action loops. Inclusive with the starting time, exclusive with the end.
     * 
     * @param start The starting time.
     * @param end The ending time.
     * @param markerCallback The function to call for each marker passed.
     */
    public void passMarkers(float start, float end, Consumer<Marker> markerCallback)
    {
        int i0 = getNextMarkerIndex(start);
        int i1 = getPrevMarkerIndex(end);
        if (i0 < 0 || i1 < 0) return;
        
        if (start <= end)
        {
            for (int i=i0; i<=i1; i++) markerCallback.accept(markers[i]);
        }
        else
        {
            for (int i=i0; i<markers.length; i++) markerCallback.accept(markers[i]);
            for (int i=0; i<=i1; i++) markerCallback.accept(markers[i]);
        }
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
