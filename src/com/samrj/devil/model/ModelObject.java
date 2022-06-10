package com.samrj.devil.model;

import com.samrj.devil.math.Mat4;
import com.samrj.devil.math.Quat;
import com.samrj.devil.math.Transform;
import com.samrj.devil.math.Vec3;
import com.samrj.devil.model.constraint.CopyRotationConstraint.CopyRotDef;
import com.samrj.devil.model.constraint.IKConstraint.IKDefinition;

import java.io.IOException;
import java.util.*;

/**
 * @author Samuel Johnson (SmashMaster)
 * @param <DATA_TYPE> The type of datablock this ModelObject encapsulates.
 * @copyright 2022 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class ModelObject<DATA_TYPE extends DataBlock> extends DataBlockAnimatable
{
    public enum EmptyType
    {
        NONE, ARROWS, PLAINAXES, CIRCLE, SINGLE_ARROW, CUBE, EMPTY_SPHERE, EMPTY_CONE, EMPTY_IMAGE;
    }

    //Object types: https://github.com/blender/blender/blob/master/source/blender/makesdna/DNA_object_types.h
    private static final int TYPE_EMPTY = 0;
    private static final int TYPE_MESH = 1;
    private static final int TYPE_CURVES_LEGACY = 2;
    private static final int TYPE_SURF = 3;
    private static final int TYPE_FONT = 4;
    private static final int TYPE_MBALL = 5;
    private static final int TYPE_LAMP = 10;
    private static final int TYPE_CAMERA = 11;
    private static final int TYPE_SPEAKER = 12;
    private static final int TYPE_LIGHTPROBE = 13;
    private static final int TYPE_LATTICE = 22;
    private static final int TYPE_ARMATURE = 25;
    private static final int TYPE_GPENCIL = 26;
    private static final int TYPE_CURVES = 27;
    private static final int TYPE_POINTCLOUD = 28;
    private static final int TYPE_VOLUME = 29;

    @Deprecated
    public final Map<String, String> arguments; //Requires blender plugin -- use custom properties for this instead.

    public final Transform transform;
    public final List<String> vertexGroups;
    public final Pose pose;
    public final List<IKDefinition> ikConstraints;
    public final List<CopyRotDef> copyRotConstraints;
    public final DataPointer<DATA_TYPE> data;
    public final DataPointer<ModelObject<?>> parent;
    public final String parentBoneName;
    public final Mat4 parentMatrix;
    public final DataPointer<Action> action;
    public final EmptyType emptyType;
    public final DataPointer<ModelCollection> instance;
    
    ModelObject(Model model, BlendFile.Pointer bObject) throws IOException
    {
        super(model, bObject);
        
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
        Vec3 sca = bObject.getField("scale", "size").asVec3();
        transform = new Transform(pos, rot, sca);
        
        vertexGroups = new ArrayList<>();
        for (BlendFile.Pointer group : bObject.getField("defbase").asList("bDeformGroup"))
            vertexGroups.add(group.getField("name").asString());
        
        BlendFile.Pointer bPose = bObject.getField("pose").dereference();
        ikConstraints = new ArrayList<>();
        copyRotConstraints = new ArrayList<>();
        if (bPose != null)
        {
            pose = new Pose(bPose);
            
            for (BlendFile.Pointer bChan : bPose.getField("chanbase").asList("bPoseChannel"))
                for (BlendFile.Pointer bCons : bChan.getField("constraints").asList("bConstraint"))
            {
                switch (bCons.getField("type").asShort())
                {
                    case 3:
                    {
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
                    break;
                    case 8:
                    {
                        BlendFile.Pointer bRotLike = bCons.getField("data").cast("bRotateLikeConstraint").dereference();
                        
                        BlendFile.Pointer target = bRotLike.getField("tar").dereference();
                        String subtarget = bRotLike.getField("subtarget").asString();
                        if (target == null || subtarget.isEmpty()) continue;
                        
                        String bone = bChan.getField("name").asString();
                        
                        copyRotConstraints.add(new CopyRotDef(bone, subtarget));
                    }
                    break;
                }
            }
        }
        else pose = null;
        
        Type dataType = null;
        String dataName = null;

        switch (bObject.getField("type").asShort())
        {
            case TYPE_MESH:
                dataType = Type.MESH;
                BlendFile.Pointer bMesh = bObject.getField("data").cast("Mesh").dereference();
                dataName = bMesh.getField(0).getField("name").asString().substring(2);
                break;
            case TYPE_CURVES_LEGACY:
                dataType = Type.CURVE;
                BlendFile.Pointer bCurve = bObject.getField("data").cast("Curve").dereference();
                dataName = bCurve.getField(0).getField("name").asString().substring(2);
                break;
            case TYPE_LAMP:
                dataType = Type.LAMP;
                BlendFile.Pointer bLamp = bObject.getField("data").cast("Lamp").dereference();
                dataName = bLamp.getField(0).getField("name").asString().substring(2);
                break;
            case TYPE_ARMATURE:
                dataType = Type.ARMATURE;
                BlendFile.Pointer bArmature = bObject.getField("data").cast("bArmature").dereference();
                dataName = bArmature.getField(0).getField("name").asString().substring(2);
                break;

            //Unimplemented types:
            case TYPE_EMPTY, TYPE_SURF, TYPE_FONT, TYPE_MBALL, TYPE_CAMERA, TYPE_SPEAKER, TYPE_LIGHTPROBE,
                    TYPE_LATTICE, TYPE_GPENCIL, TYPE_CURVES, TYPE_POINTCLOUD, TYPE_VOLUME:
            default: break;
        }
        data = new DataPointer<>(model, dataType, dataName);
        
        BlendFile.Pointer bParent = bObject.getField("parent").dereference();
        if (bParent != null)
        {
            String parentName = bParent.getField(0).getField("name").asString().substring(2);
            parent = new DataPointer<>(model, Type.OBJECT, parentName);
            
            if (bObject.getField("partype").asShort() == 7) parentBoneName = bObject.getField("parsubstr").asString();
            else parentBoneName = null;

            //Not 100% sure why this needs to be transposed to get correct results. TODO: Possibly an underlying bug.
            parentMatrix = bObject.getField("parentinv").asMat4().transpose();
        }
        else
        {
            parent = DataPointer.nullPointer(model);
            parentBoneName = null;
            parentMatrix = null;
        }
        
        BlendFile.Pointer bAction = bObject.getField("action").dereference();
        if (bAction != null)
        {
            String actionName = bAction.getField(0).getField("name").asString().substring(2);
            action = new DataPointer<>(model, Type.ACTION, actionName);
        }
        else action = DataPointer.nullPointer(model);
        
        emptyType = EmptyType.values()[bObject.getField("empty_drawtype").asByte() & 0xFF];

        BlendFile.Pointer bInstance = bObject.getField("instance_collection", "dup_group").dereference();
        if (bInstance != null)
        {
            String instanceName = bInstance.getField(0).getField("name").asString().substring(2);
            instance = new DataPointer<>(model, Type.COLLECTION, instanceName);
        }
        else instance = null;
    }
    
    public <T extends DataBlock> ModelObject<T> asType(Class<T> typeClass)
    {
        return typeClass.isInstance(data.get()) ? (ModelObject<T>)this : null;
    }
    
    public <T extends DataBlock> Optional<ModelObject<T>> optionalType(Class<T> typeClass)
    {
        return Optional.ofNullable(asType(typeClass));
    }

    /**
     * These methods do not apply the parent inverse matrix, and cannot work for shearing transformations.
     */

    @Deprecated
    public void applyParentTransform(Transform result)
    {
        ModelObject<?> parentObj = parent.get();
        if (parentObj != null)
        {
            result.mult(parentObj.transform);
            parentObj.applyParentTransform(result);
        }
    }

    @Deprecated
    public void getParentedTransform(Transform result)
    {
        result.set(transform);
        applyParentTransform(result);
    }

    @Deprecated
    public Transform getParentedTransform()
    {
        Transform out = new Transform();
        getParentedTransform(out);
        return out;
    }
}
