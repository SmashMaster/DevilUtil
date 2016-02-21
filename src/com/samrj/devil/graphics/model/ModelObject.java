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
    
    private final int dataType, dataIndex, dataLibIndex;
    private final String dataLibName;
    private final int parentIndex;
    private final String parentBoneName;
    private final int actionIndex;
    
    private boolean populated;
    private Object data;
    private ModelObject parent;
    private Bone parentBone;
    private Action action;
    
    ModelObject(DataInputStream in) throws IOException
    {
        name = IOUtil.readPaddedUTF(in);
        dataType = in.readInt();
        dataLibIndex = dataType >= 0 ? in.readInt() : -1;
        dataIndex = dataType >= 0 && dataLibIndex < 0 ? in.readInt() : -1;
        dataLibName = dataLibIndex >= 0 ? IOUtil.readPaddedUTF(in) : null;
        parentIndex = in.readInt();
        parentBoneName = parentIndex >= 0 ? IOUtil.readPaddedUTF(in) : null;
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
    
    private Model.ArrayMap dataArray(Model model)
    {
        switch (dataType)
        {
            case 0: return model.libraries;
            case 1: return model.actions;
            case 2: return model.armatures;
            case 3: return model.curves;
            case 4: return model.lamps;
            case 5: return model.materials;
            case 6: return model.meshes;
            case 7: return model.scenes;
            default: throw new IllegalArgumentException();
        }
    }
    
    void populate(Model model)
    {
        if (populated) return;
        
        data = dataIndex < 0 ? null : dataArray(model).get(dataIndex);
        if (data instanceof Armature)
            for (IKConstraint ik : ikConstraints) ik.populate((Armature)data);
        parent = parentIndex < 0 ? null : model.objects.get(parentIndex);
        if (parent != null)
        {
            parent.populate(model);
            if (parent.getData() instanceof Armature)
            {
                Armature armature = (Armature)parent.getData();
                parentBone = armature.getBone(parentBoneName);
            }
        }
        action = actionIndex < 0 ? null : model.actions.get(actionIndex);
        
        populated = true;
    }
    
    public Object getData()
    {
        return data;
    }
    
    public ModelObject getParent()
    {
        return parent;
    }
    
    public Bone getParentBone()
    {
        return parentBone;
    }
    
    public Action getAction()
    {
        return action;
    }
    
    @Override
    public String getName()
    {
        return name;
    }
}
