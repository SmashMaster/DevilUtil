package com.samrj.devil.model;

import com.samrj.devil.math.Util;
import com.samrj.devil.model.Pose.PoseBone;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Stream;
import org.blender.dna.TimeMarker;
import org.blender.dna.bAction;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2019 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class Action extends DataBlock
{
    public final List<FCurve> fcurves;
    public final float minX, maxX;
    
    private final List<Marker> markers;
    private final Map<String, Marker> markerMap;
    
    Action(Model model, bAction bAction) throws IOException
    {
        super(model, bAction.getId());
        
        fcurves = new ArrayList<>();
        float min = Float.POSITIVE_INFINITY, max = Float.NEGATIVE_INFINITY;
        for (org.blender.dna.FCurve bfCurve : Blender.list(bAction.getCurves(), org.blender.dna.FCurve.class))
        {
            FCurve fCurve = new FCurve(bfCurve);
            fcurves.add(new FCurve(bfCurve));
            
            if (fCurve.minX < min) min = fCurve.minX;
            if (fCurve.maxX > max) max = fCurve.maxX;
        }
        minX = min; maxX = max;
        
        markers = new ArrayList<>();
        for (TimeMarker bMarker : Blender.list(bAction.getMarkers(), TimeMarker.class))
        {
            float frame = bMarker.getFrame();
            String[] markerNames = bMarker.getName().asString().split("\\+");
            for (String markerName : markerNames)
                markers.add(new Marker(frame, markerName.trim()));
        }
        
        markerMap = new HashMap<>(markers.size());
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
        for (PoseBone bone : pose.getBones()) bone.transform.rot.normalize();
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
    
    public Marker requireMarker(String name)
    {
        Marker marker = markerMap.get(name);
        if (marker == null) throw new NoSuchElementException(name);
        return marker;
    }
    
    public Stream<Marker> passMarkers(float start, float end)
    {
        if (end == start) return Stream.of();
        else if (end > start) return markers.stream()
                .filter(m -> m.frame > start && m.frame <= end)
                .sorted((a, b) -> Util.compare(a.frame, b.frame));
        else return markers.stream()
                .filter(m -> m.frame < start && m.frame >= end)
                .sorted((a, b) -> Util.compare(b.frame, a.frame));
    }
    
    public class Marker
    {
        public final float frame;
        public final String name;
        
        private Marker(float frame, String name) throws IOException
        {
            this.frame = frame;
            this.name = name;
        }
        
        @Override
        public String toString()
        {
            return frame + ": \"" + name + "\"";
        }
    }
}
