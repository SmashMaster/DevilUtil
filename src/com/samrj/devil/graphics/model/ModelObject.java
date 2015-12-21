package com.samrj.devil.graphics.model;

import com.samrj.devil.io.IOUtil;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class ModelObject implements DataBlock
{
    public final String name;
    public final Transform transform;
    public final String[] vertexGroups;
    public final Pose pose;
    public final IKConstraint[] ikConstraints;
    
    private final Type dataType;
    private final int dataIndex;
    private final int parentIndex;
    private final int actionIndex;
    
    private DataBlock data;
    private ModelObject parent;
    private Action action;
    
    ModelObject(DataInputStream in) throws IOException
    {
        name = IOUtil.readPaddedUTF(in);
        dataType = DataBlock.getTypeFromID(in.readShort());
        dataIndex = in.readShort();
        parentIndex = in.readInt();
        transform = new Transform(in);
        vertexGroups = IOUtil.arrayFromStream(in, String.class, (s) -> IOUtil.readPaddedUTF(s));
        boolean hasPose = in.readInt() != 0;
        if (hasPose)
        {
            pose = new Pose(in);
            ikConstraints = IOUtil.arrayFromStream(in, IKConstraint.class, IKConstraint::new);
        }
        else
        {
            pose = null;
            ikConstraints = new IKConstraint[0];
        }
        actionIndex = in.readInt();
    }
    
    void populate(Model model)
    {
        data = model.getData(dataType, dataIndex);
        parent = model.getData(Type.OBJECT, parentIndex);
        action = model.getData(Type.ACTION, actionIndex);
    }
    
    public DataBlock getData()
    {
        return data;
    }
    
    public ModelObject getParent()
    {
        return parent;
    }
    
    public Action getAction()
    {
        return action;
    }
    
    @Override
    public Type getType()
    {
        return Type.OBJECT;
    }
}
