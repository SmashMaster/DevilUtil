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
public class Scene implements DataBlock
{
    public final String name;
    public final Vec3 backgroundColor;
    public final DataPointer<ModelObject<?>>[] objects;
    
    Scene(Model model, DataInputStream in) throws IOException
    {
        name = IOUtil.readPaddedUTF(in);
        backgroundColor = new Vec3(in);
        objects = new DataPointer[in.readInt()];
        for (int i=0; i<objects.length; i++)
            objects[i] = new DataPointer(model, Type.OBJECT, in.readInt());
    }
    
    @Override
    public String getName()
    {
        return name;
    }
}
