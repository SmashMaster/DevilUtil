package com.samrj.devil.graphics.model;

import com.samrj.devil.io.IOUtil;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class ModelObject
{
    public final String name;
    public final Transform transform;
    public final String[] vertexGroups;
    public final Pose pose;
    public final IKConstraint[] ikConstraints;
    
    private final int type;
    private final int dataIndex;
    private final int parentIndex;
    private final int actionIndex;
    
    private Object data;
    private ModelObject parent;
    private Action action;
    
    ModelObject(DataInputStream in) throws IOException
    {
        name = IOUtil.readPaddedUTF(in);
        type = in.readShort();
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
    
    private Object[] dataArray(Model model)
    {
        switch (type)
        {
            case 0: return model.actions;
            case 1: return model.armatures;
            case 2: return model.lamps;
            case 3: return model.materials;
            case 4: return model.meshes;
            default: throw new IllegalArgumentException();
        }
    }
    
    void populate(Model model)
    {
        data = dataIndex < 0 ? null : dataArray(model)[dataIndex];
        if (data instanceof Armature)
            for (IKConstraint ik : ikConstraints) ik.populate((Armature)data);
        parent = parentIndex < 0 ? null : model.objects[parentIndex];
        action = actionIndex < 0 ? null : model.actions[actionIndex];
    }
    
    public Object getData()
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
}
