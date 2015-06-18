package com.samrj.devil.graphics.model;

import java.io.DataInputStream;
import java.io.IOException;

public class Action
{
    public final String name;
    public final FCurve[] fCurves;
    
    public Action(DataInputStream in) throws IOException
    {
        name = DevilModel.readPaddedUTF(in);
        int numFCurves = in.readInt();
        fCurves = new FCurve[numFCurves];
        for (int i=0; i<numFCurves; i++)
            fCurves[i] = new FCurve(in);
    }
}
