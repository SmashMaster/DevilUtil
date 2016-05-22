package com.samrj.devil.model;

import com.samrj.devil.io.IOUtil;
import com.samrj.devil.math.Vec3;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Curve extends DataBlock
{
    public final Spline[] splines;
    
    Curve(Model model, DataInputStream in) throws IOException
    {
        super(model, in);
        splines = IOUtil.arrayFromStream(in, Spline.class, Spline::new);
    }
    
    public static class Spline
    {
        public final boolean cyclic;
        public final SplinePoint[] points;
        
        Spline(DataInputStream in) throws IOException
        {
            cyclic = in.readInt() != 0;
            points = IOUtil.arrayFromStream(in, SplinePoint.class, SplinePoint::new);
        }
    }
    
    public static class SplinePoint
    {
        public final Vec3 co, left, right;
        
        SplinePoint(DataInputStream in) throws IOException
        {
            co = new Vec3(in);
            left = new Vec3(in);
            right = new Vec3(in);
        }
    }
}
