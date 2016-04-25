package com.samrj.devil.model;

import com.samrj.devil.io.IOUtil;
import com.samrj.devil.model.constraint.IKConstraint.IKDefinition;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * @author Samuel Johnson (SmashMaster)
 * @param <DATA_TYPE> The type of datablock this ModelObject encapsulates.
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class ModelObject<DATA_TYPE extends DataBlock> implements DataBlock
{
    public final String name;
    public final Transform transform;
    public final String[] vertexGroups;
    public final Pose pose;
    public final IKDefinition[] ikConstraints;
    public final DataPointer<DATA_TYPE> data;
    public final DataPointer<ModelObject<?>> parent;
    public final String parentBoneName;
    public final DataPointer<Action> action;
    
    ModelObject(Model model, DataInputStream in) throws IOException
    {
        name = IOUtil.readPaddedUTF(in);
        
        int dataType = in.readInt();
        int dataLibIndex = dataType >= 0 ? in.readInt() : -1;
        int dataIndex = dataType >= 0 && dataLibIndex < 0 ? in.readInt() : -1;
        if (dataLibIndex >= 0) IOUtil.readPaddedUTF(in); //Data library name
        data = new DataPointer(model, dataType, dataIndex);
        int parentIndex = in.readInt();
        parent = new DataPointer<>(model, Type.OBJECT, parentIndex);
        parentBoneName = parentIndex >= 0 ? IOUtil.readPaddedUTF(in) : null;
        
        transform = new Transform(in);
        vertexGroups = IOUtil.arrayFromStream(in, String.class, (s) -> IOUtil.readPaddedUTF(s));
        boolean hasPose = in.readInt() != 0;
        if (hasPose)
        {
            pose = new Pose(in);
            ikConstraints = IOUtil.arrayFromStream(in, IKDefinition.class, IKDefinition::new);
        }
        else
        {
            pose = null;
            ikConstraints = new IKDefinition[0];
        }
        
        action = new DataPointer<>(model, Type.ACTION, in.readInt());
    }
    
    public void applyParentTransform(Transform result)
    {
        ModelObject<?> parentObj = parent.get();
        if (parentObj != null)
        {
            result.mult(parentObj.transform);
            parentObj.applyParentTransform(result);
        }
    }
    
    public void getParentedTransform(Transform result)
    {
        result.set(transform);
        applyParentTransform(result);
    }
    
    @Override
    public String getName()
    {
        return name;
    }
}
