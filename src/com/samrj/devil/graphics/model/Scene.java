package com.samrj.devil.graphics.model;

import com.samrj.devil.io.IOUtil;
import com.samrj.devil.math.Vec3;
import java.io.DataInputStream;
import java.io.IOException;

public class Scene implements DataBlock
{
    public final String name;
    public final Vec3 backgroundColor;
    public final ModelObject[] objects;
    
    private final int[] objectIndices;
    
    Scene(DataInputStream in) throws IOException
    {
        name = IOUtil.readPaddedUTF(in);
        backgroundColor = new Vec3(in);
        objectIndices = new int[in.readInt()];
        objects = new ModelObject[objectIndices.length];
        for (int i=0; i<objectIndices.length; i++)
            objectIndices[i] = in.readInt();
    }
    
    void populate(Model model)
    {
        for (int i=0; i<objects.length; i++)
            objects[i] = model.objects[objectIndices[i]];
    }
    
    @Override
    public Type getType()
    {
        return Type.SCENE;
    }
}
