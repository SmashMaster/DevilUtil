package com.samrj.devil.model;

import com.samrj.devil.math.Mat3;
import com.samrj.devil.math.Vec3;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Armature definition class. Can be shared between multiple objects. Give to
 * an ArmatureSolver to pose.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2019 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class Armature extends DataBlock
{
    public final List<Bone> bones;
    private final Map<String, Bone> nameMap;
    
    Armature(Model model, BlendFile.Pointer bArm) throws IOException
    {
        super(model, bArm);
        
        bones = new ArrayList<>();
        nameMap = new HashMap<>();
        for (BlendFile.Pointer bBone : bArm.getField("bonebase").asList("Bone"))
        {
            Bone bone = new Bone(null, bBone);
            recursiveAdd(bone, bBone);
            bones.add(bone);
            nameMap.put(bone.name, bone);
        }
    }
    
    private void recursiveAdd(Bone bone, BlendFile.Pointer bBone) throws IOException
    {
        for (BlendFile.Pointer bChild : bBone.getField("childbase").asList("Bone"))
        {
            Bone child = new Bone(bone, bChild);
            recursiveAdd(child, bChild);
            bones.add(child);
            nameMap.put(child.name, child);
        }
    }
    
    public Bone getBone(String name)
    {
        return nameMap.get(name);
    }
    
    public class Bone
    {
        public final String name;
        public final Bone parent;
        public final boolean inheritRotation;

        public final Vec3 head, tail;
        public final Mat3 matrix; //bone direction -> object rest direction
        public final Mat3 invMat; //object rest direction -> bone direction
        
        private Bone(Bone parent, BlendFile.Pointer bBone) throws IOException
        {
            name = bBone.getField("name").asString();
            this.parent = parent;
            
            inheritRotation = (bBone.getField("flag").asInt() & (1 << 9)) == 0; //BONE_HINGE flag
            
            head = bBone.getField("arm_head").asVec3();
            tail = bBone.getField("arm_tail").asVec3();
            invMat = new Mat3(bBone.getField("arm_mat").asMat4());
            matrix = Mat3.invert(invMat);
        }
    }
}
