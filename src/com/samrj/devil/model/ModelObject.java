package com.samrj.devil.model;

import com.samrj.devil.math.Mat4;
import com.samrj.devil.math.Quat;
import com.samrj.devil.math.Transform;
import com.samrj.devil.math.Vec3;
import com.samrj.devil.model.constraint.IKConstraint.IKDefinition;
import java.io.IOException;
import java.util.*;

/**
 * @author Samuel Johnson (SmashMaster)
 * @param <DATA_TYPE> The type of datablock this ModelObject encapsulates.
 * @copyright 2019 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class ModelObject<DATA_TYPE extends DataBlock> extends DataBlock
{
    public enum EmptyType
    {
        AXES, CUBE, SPHERE;
    }
    
    public final Scene scene;
    public final Map<String, String> arguments;
    public final Transform transform;
    public final List<String> vertexGroups;
    public final Pose pose;
    public final List<IKDefinition> ikConstraints;
    public final DataPointer<DATA_TYPE> data;
    public final DataPointer<ModelObject<?>> parent;
    public final String parentBoneName;
    public final Mat4 parentMatrix;
    public final DataPointer<Action> action;
    public final EmptyType emptyType;
    
    ModelObject(Model model, Scene scene, BlendFile.Pointer bObject) throws IOException
    {
        super(model, bObject);
        
        this.scene = scene;
        
        arguments = new HashMap<>();
        for (Property p : getSubproperties("dvm_args")) for (int i=0; i<p.properties.size();)
        {
            String pName = p.properties.get(i++).getString();
            String pValue = p.properties.get(i++).getString();
            arguments.put(pName, pValue);
        }
        
        Quat rot;
        switch (bObject.getField("rotmode").asShort())
        {
            case -1: //Axis-angle rotation
                Vec3 axis = bObject.getField("rotAxis").asVec3(); 
                float ang = bObject.getField("rotAngle").asFloat();
                rot = Quat.rotation(axis, ang);
                break;
            case 0: //Quaternion
                rot = bObject.getField("quat").asQuat();
                break;
            case 1: //XYZ Euler rotation (blender's axes are different)
                Vec3 angles = bObject.getField("rot").asVec3();
                rot = Quat.identity();
                rot.rotate(new Vec3(0, 1, 0), angles.y);
                rot.rotate(new Vec3(1, 0, 0), angles.x);
                rot.rotate(new Vec3(0, 0, 1), angles.z);
                break;
            default:
                rot = Quat.identity();
                break;
        }
        
        Vec3 pos = bObject.getField("loc").asVec3();
        Vec3 sca = bObject.getField("size").asVec3();
        transform = new Transform(pos, rot, sca);
        
        vertexGroups = new ArrayList<>();
        for (BlendFile.Pointer group : bObject.getField("defbase").asList("bDeformGroup"))
            vertexGroups.add(group.getField("name").asString());
        
        BlendFile.Pointer bPose = bObject.getField("pose").dereference();
        ikConstraints = new ArrayList<>();
        if (bPose != null)
        {
            pose = new Pose(bPose);
            
            for (BlendFile.Pointer bChan : bPose.getField("chanbase").asList("bPoseChannel"))
                for (BlendFile.Pointer bCons : bChan.getField("constraints").asList("bConstraint"))
            {
                if (bCons.getField("type").asShort() != 3) continue;
                BlendFile.Pointer bIK = bCons.getField("data").cast("bKinematicConstraint").dereference();
                
                if (bIK.getField("rootbone").asShort() != 2) continue; //Only support 2-bone IK at this time
                
                BlendFile.Pointer target = bIK.getField("tar").dereference();
                String subtarget = bIK.getField("subtarget").asString();
                if (target == null || subtarget.isEmpty()) continue;
                
                BlendFile.Pointer poleTarget = bIK.getField("poletar").dereference();
                String poleSubtarget = bIK.getField("polesubtarget").asString();
                if (poleTarget == null || poleSubtarget.isEmpty()) continue;
                
                String bone = bChan.getField("name").asString();
                float poleAngle = bIK.getField("poleangle").asFloat();
                ikConstraints.add(new IKDefinition(bone, subtarget, poleSubtarget, poleAngle));
            }
        }
        else pose = null;
        
        Type dataType = null;
        String dataName = null;
        switch (bObject.getField("type").asShort())
        {
            case 1:
                dataType = Type.MESH;
                BlendFile.Pointer bMesh = bObject.getField("data").cast("Mesh").dereference();
                dataName = bMesh.getField(0).getField("name").asString().substring(2);
                break;
            case 2:
                dataType = Type.CURVE;
                BlendFile.Pointer bCurve = bObject.getField("data").cast("Curve").dereference();
                dataName = bCurve.getField(0).getField("name").asString().substring(2);
                break;
            case 10:
                dataType = Type.LAMP;
                BlendFile.Pointer bLamp = bObject.getField("data").cast("Lamp").dereference();
                dataName = bLamp.getField(0).getField("name").asString().substring(2);
                break;
            case 25:
                dataType = Type.ARMATURE;
                BlendFile.Pointer bArmature = bObject.getField("data").cast("bArmature").dereference();
                dataName = bArmature.getField(0).getField("name").asString().substring(2);
                break;
                
            //Unimplemented types:
            case 3: //surf
            case 4: //font
            case 5: //mball
            case 11: //camera
            case 12: //speaker
            case 22: //lattice
                
            case 0: //empty
            default:
                break;
        }
        data = new DataPointer<>(model, dataType, dataName);
        
        BlendFile.Pointer bParent = bObject.getField("parent").dereference();
        if (bParent != null)
        {
            String parentName = bParent.getField(0).getField("name").asString().substring(2);
            parent = new DataPointer<>(model, Type.OBJECT, parentName);
            
            if (bObject.getField("partype").asShort() == 7) parentBoneName = bObject.getField("parsubstr").asString();
            else parentBoneName = null;
            
            parentMatrix = bObject.getField("parentinv").asMat4();
        }
        else
        {
            parent = new DataPointer<>(model, null, null);
            parentBoneName = null;
            parentMatrix = null;
        }
        
        BlendFile.Pointer bAction = bObject.getField("action").dereference();
        if (bAction != null)
        {
            String actionName = bAction.getField(0).getField("name").asString().substring(2);
            action = new DataPointer<>(model, Type.ACTION, actionName);
        }
        else action = new DataPointer<>(model, null, null);
        
        switch(bObject.getField("empty_drawtype").asByte())
        {
            case 2:
                emptyType = EmptyType.AXES;
                break;
            case 5:
                emptyType = EmptyType.CUBE;
                break;
            case 6:
                emptyType = EmptyType.SPHERE;
                break;
                
            //Unimplemented types:
            case 1: //arrows
            case 3: //circle
            case 4: //single arrow
            case 7: //cone
            case 8: //image
                
            default:
                emptyType = null;
                break;
        }
    }
    
    public <T extends DataBlock> ModelObject<T> asType(Class<T> typeClass)
    {
        return typeClass.isInstance(data.get()) ? (ModelObject<T>)this : null;
    }
    
    public <T extends DataBlock> Optional<ModelObject<T>> optionalType(Class<T> typeClass)
    {
        return Optional.ofNullable(asType(typeClass));
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
    
    public Transform getParentedTransform()
    {
        Transform out = new Transform();
        getParentedTransform(out);
        return out;
    }
}
