package com.samrj.devil.model;

import com.samrj.devil.math.Vec3;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class Scene extends DataBlock
{
    public final Vec3 backgroundColor;
    public final List<DataPointer<ModelObject<?>>> objects;
    
    Scene(Model model, DataInputStream in) throws IOException
    {
        super(model, in);
        backgroundColor = new Vec3(in);
        int numObjects = in.readInt();
        ArrayList<DataPointer<ModelObject<?>>> list = new ArrayList<>(numObjects);
        for (int i=0; i<numObjects; i++)
            list.add(new DataPointer(model, Type.OBJECT, in.readInt()));
        objects = Collections.unmodifiableList(list);
    }
}
