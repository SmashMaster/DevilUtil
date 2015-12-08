package com.samrj.devil.graphics.model;

import com.samrj.devil.io.IOUtil;
import com.samrj.devil.math.Vec3;
import java.io.DataInputStream;
import java.io.IOException;

public class ModelObject implements DataBlock
{
    public final String name;
    
    public final Vec3 position;
    public final Vec3 scale;
    public final String[] vertexGroups;
    
    public DataBlock data;
    public Action action;
    
    private final Type dataType;
    private final int dataIndex;
    private final int actionIndex;
    
    ModelObject(DataInputStream in) throws IOException
    {
        name = IOUtil.readPaddedUTF(in);
        dataType = DataBlock.getTypeFromID(in.readInt());
        dataIndex = dataType != Type.UNKNOWN ? in.readInt() : -1;
        position = new Vec3(in);
        if (in.readInt() >= 0) in.skip(16); //Rotation
        scale = new Vec3(in);
        vertexGroups = IOUtil.arrayFromStream(in, String.class, stream -> IOUtil.readPaddedUTF(stream));
        actionIndex = in.readInt();
    }
    
    void populate(Model model)
    {
        data = model.getData(dataType, dataIndex);
        data = model.getData(Type.ACTION, actionIndex);
    }
    
    @Override
    public Type getType()
    {
        return Type.OBJECT;
    }
}
