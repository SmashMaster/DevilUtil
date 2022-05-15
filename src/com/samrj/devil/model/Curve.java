package com.samrj.devil.model;

import com.samrj.devil.math.Vec3;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2019 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class Curve extends DataBlock
{
    public final List<Spline> splines;
    
    Curve(Model model, BlendFile.Pointer bCurve) throws IOException
    {
        super(model, bCurve);
        
        splines = new ArrayList<>();
        for (BlendFile.Pointer bNurb : bCurve.getField("nurb").asList("Nurb"))
            splines.add(new Spline(bNurb));
    }
    
    public static class Spline
    {
        public final boolean cyclic;
        public final List<SplinePoint> points;
        
        Spline(BlendFile.Pointer bNurb) throws IOException
        {
            cyclic = (bNurb.getField("flagu").asShort() & 1) != 0; //CU_NURB_CYCLIC flag
            
            int numPoints = bNurb.getField("pntsu").asInt();
            BlendFile.Pointer[] bezts = bNurb.getField("bezt").dereference().asArray(numPoints);
            
            points = new ArrayList<>(bezts.length);
            for (BlendFile.Pointer bezt : bezts) points.add(new SplinePoint(bezt));
        }
    }
    
    public static class SplinePoint
    {
        public final Vec3 left, co, right;
        
        SplinePoint(BlendFile.Pointer bezt) throws IOException
        {
            BlendFile.Pointer vec = bezt.getField("vec");
            
            left = vec.asVec3();
            co = vec.add(12).asVec3();
            right = vec.add(24).asVec3();
        }
    }
}
