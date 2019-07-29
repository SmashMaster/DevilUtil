package com.samrj.devil.model;

import com.samrj.devil.math.Mat4;
import com.samrj.devil.math.Quat;
import com.samrj.devil.math.Transform;
import com.samrj.devil.math.Vec3;
import com.samrj.devil.model.Scene;
import com.samrj.devil.model.constraint.IKConstraint.IKDefinition;
import java.io.IOException;
import java.util.*;
import org.blender.dna.*;

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
    
    ModelObject(Model model, Scene scene, BlenderObject bObject) throws IOException
    {
        super(model, bObject.getId());
        
        this.scene = scene;
        
        arguments = new HashMap<>();
        for (Property p : getSubproperties("dvm_args")) for (int i=0; i<p.properties.size();)
        {
            String pName = p.properties.get(i++).getString();
            String pValue = p.properties.get(i++).getString();
            arguments.put(pName, pValue);
        }
        
        Quat rot;
        switch (bObject.getRotmode())
        {
            case -1: //Axis-angle rotation
                Vec3 axis = Blender.vec3(bObject.getRotAxis());
                float ang = bObject.getRotAngle();
                rot = Quat.rotation(axis, ang);
                break;
            case 0: //Quaternion
                rot = Blender.quat(bObject.getQuat());
                break;
            case 1: //XYZ Euler rotation (blender's axes are different)
                Vec3 angles = Blender.vec3(bObject.getRot());
                rot = Quat.identity();
                rot.rotate(new Vec3(0, 1, 0), angles.y);
                rot.rotate(new Vec3(1, 0, 0), angles.x);
                rot.rotate(new Vec3(0, 0, 1), angles.z);
                break;
            default:
                rot = Quat.identity();
                break;
        }
        
        Vec3 pos = Blender.vec3(bObject.getLoc());
        Vec3 sca = Blender.vec3(bObject.getSize());
        transform = new Transform(pos, rot, sca);
        
        vertexGroups = new ArrayList<>();
        for (bDeformGroup group : Blender.list(bObject.getDefbase(), bDeformGroup.class))
            vertexGroups.add(group.getName().asString());
        
        bPose bPose = bObject.getPose().get();
        ikConstraints = new ArrayList<>();
        if (bPose != null)
        {
            pose = new Pose(bPose);
            
            for (bPoseChannel bChan : Blender.list(bPose.getChanbase(), bPoseChannel.class))
                for (bConstraint bCons : Blender.list(bChan.getConstraints(), bConstraint.class))
            {
                if (bCons.getType() != 3) continue;
                bKinematicConstraint bIK = bCons.getData().cast(bKinematicConstraint.class).get();
                
                if (bIK.getRootbone() != 2) continue; //Only support 2-bone IK at this time
                
                BlenderObject target = bIK.getTar().get();
                String subtarget = bIK.getSubtarget().asString();
                if (target == null || subtarget.isEmpty()) continue;
                
                BlenderObject poleTarget = bIK.getPoletar().get();
                String poleSubtarget = bIK.getPolesubtarget().asString();
                if (poleTarget == null || poleSubtarget.isEmpty()) continue;
                
                String bone = bChan.getName().asString();
                ikConstraints.add(new IKDefinition(bone, subtarget, poleSubtarget, bIK.getPoleangle()));
            }
        }
        else pose = null;
        
        Type dataType = null;
        String dataName = null;
        switch (bObject.getType())
        {
            case 1:
                dataType = Type.MESH;
                org.blender.dna.Mesh bMesh = bObject.getData().cast(org.blender.dna.Mesh.class).get();
                dataName = bMesh.getId().getName().asString().substring(2);
                break;
            case 2:
                dataType = Type.CURVE;
                org.blender.dna.Curve bCurve = bObject.getData().cast(org.blender.dna.Curve.class).get();
                dataName = bCurve.getId().getName().asString().substring(2);
                break;
            case 10:
                dataType = Type.LAMP;
                org.blender.dna.Lamp bLamp = bObject.getData().cast(org.blender.dna.Lamp.class).get();
                dataName = bLamp.getId().getName().asString().substring(2);
                break;
            case 25:
                dataType = Type.ARMATURE;
                bArmature bArmature = bObject.getData().cast(bArmature.class).get();
                dataName = bArmature.getId().getName().asString().substring(2);
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
        
        BlenderObject bParent = bObject.getParent().get();
        if (bParent != null)
        {
            String parentName = bParent.getId().getName().asString().substring(2);
            parent = new DataPointer<>(model, Type.OBJECT, parentName);
            
            if (bObject.getPartype() == 7) parentBoneName = bObject.getParsubstr().asString();
            else parentBoneName = null;
            
            parentMatrix = Blender.mat4(bObject.getParentinv());
        }
        else
        {
            parent = new DataPointer<>(model, null, null);
            parentBoneName = null;
            parentMatrix = null;
        }
        
        bAction bAction = bObject.getAction().get();
        if (bAction != null)
        {
            String actionName = bAction.getId().getName().asString().substring(2);
            action = new DataPointer<>(model, Type.ACTION, actionName);
        }
        else action = new DataPointer<>(model, null, null);
        
        switch(bObject.getEmpty_drawtype())
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
