package com.samrj.devil.graphics.model;

import com.samrj.devil.io.IOUtil;
import java.io.DataInputStream;
import java.io.IOException;

public class ModelObject implements DataBlock
{
    public final String name;
    public final Transform transform;
    public final String[] vertexGroups;
    public final Pose pose;
    
    public DataBlock data;
    public ModelObject parent;
    public Action action;
    
    private final Type dataType;
    private final int dataIndex;
    private final int parentIndex;
    private final int actionIndex;
    
    ModelObject(DataInputStream in) throws IOException
    {
        name = IOUtil.readPaddedUTF(in);
        dataType = DataBlock.getTypeFromID(in.readShort());
        dataIndex = in.readShort();
        parentIndex = in.readInt();
        transform = new Transform(in);
        vertexGroups = IOUtil.arrayFromStream(in, String.class, stream -> IOUtil.readPaddedUTF(stream));
        pose = in.readInt() != 0 ? new Pose(in) : null;
        actionIndex = in.readInt();
    }
    
    void populate(Model model)
    {
        data = model.getData(dataType, dataIndex);
        parent = model.getData(Type.OBJECT, parentIndex);
        action = model.getData(Type.ACTION, actionIndex);
        if (pose != null) pose.populate((Armature)data);
    }
    
    @Override
    public Type getType()
    {
        return Type.OBJECT;
    }
}
