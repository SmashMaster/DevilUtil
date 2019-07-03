package com.samrj.devil.model;

import com.samrj.devil.math.Vec3;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.blender.dna.BezTriple;
import org.blender.dna.Nurb;
import org.cakelab.blender.nio.CArrayFacade;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2019 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class Curve extends DataBlock
{
    public final List<Spline> splines;
    
    Curve(Model model, org.blender.dna.Curve bCurve) throws IOException
    {
        super(model, bCurve.getId());
        
        splines = new ArrayList<>();
        for (Nurb bNurb : Blender.blendList(bCurve.getNurb(), Nurb.class))
            splines.add(new Spline(bNurb));
    }
    
    public static class Spline
    {
        public final boolean cyclic;
        public final List<SplinePoint> points;
        
        Spline(Nurb bNurb) throws IOException
        {
            cyclic = (bNurb.getFlagu() & 1) != 0; //CU_NURB_CYCLIC flag
            
            int numPoints = bNurb.getPntsu();
            BezTriple[] bezts = bNurb.getBezt().toArray(numPoints);

            points = new ArrayList<>(bezts.length);
            for (BezTriple bezt : bezts) points.add(new SplinePoint(bezt));
        }
    }
    
    public static class SplinePoint
    {
        public final Vec3 left, co, right;
        
        SplinePoint(BezTriple bezt) throws IOException
        {
            CArrayFacade<CArrayFacade<Float>> vec = bezt.getVec();
            
            left = Blender.vec3(vec.get(0));
            co = Blender.vec3(vec.get(1));
            right = Blender.vec3(vec.get(2));
        }
    }
}
