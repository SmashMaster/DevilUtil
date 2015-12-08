package com.samrj.devil.graphics.model;

import com.samrj.devil.io.IOUtil;
import java.io.DataInputStream;
import java.io.IOException;

public class Action implements DataBlock
{
    public final String name;
    public final FCurve[] fcurves;
    
    Action(DataInputStream in) throws IOException
    {
        name = IOUtil.readPaddedUTF(in);
        fcurves = IOUtil.arrayFromStream(in, FCurve.class, FCurve::new);
    }

    @Override
    public Type getType()
    {
        return Type.ACTION;
    }
}
