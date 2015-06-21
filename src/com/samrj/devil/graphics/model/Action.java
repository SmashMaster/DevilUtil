package com.samrj.devil.graphics.model;

import java.io.DataInputStream;
import java.io.IOException;

public class Action
{
    public final String name;
    public final FCurve[] fCurves;
    public final float start, end;
    
    public Action(DataInputStream in) throws IOException
    {
        name = DevilModel.readPaddedUTF(in);
        int numFCurves = in.readInt();
        fCurves = new FCurve[numFCurves];
        float min = Float.POSITIVE_INFINITY, max = Float.NEGATIVE_INFINITY;
        for (int i=0; i<numFCurves; i++)
        {
            FCurve curve = new FCurve(in);
            fCurves[i] = curve;
            if (curve.minX < min) min = curve.minX;
            if (curve.maxX > max) max = curve.maxX;
        }
        start = min; end = max;
    }
}
