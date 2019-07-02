package com.samrj.devil.model;

import com.samrj.devil.math.Mat3;
import com.samrj.devil.math.Vec3;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.blender.dna.bArmature;

/**
 * Armature definition class. Can be shared between multiple objects. Give to
 * an ArmatureSolver to pose.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class Armature extends DataBlock
{
    public final List<Bone> bones;
    private final Map<String, Bone> nameMap;
    
    Armature(Model model, bArmature bArm) throws IOException
    {
        super(model, bArm.getId().getName().asString().substring(2));
        
        bones = new ArrayList<>();
        nameMap = new HashMap<>();
        for (org.blender.dna.Bone bBone : Blender.blendList(bArm.getBonebase(), org.blender.dna.Bone.class))
        {
            Bone bone = new Bone(null, bBone);
            recursiveAdd(bone, bBone);
            bones.add(bone);
            nameMap.put(bone.name, bone);
        }
    }
    
    private void recursiveAdd(Bone bone, org.blender.dna.Bone bBone) throws IOException
    {
        for (org.blender.dna.Bone bChild : Blender.blendList(bBone.getChildbase(), org.blender.dna.Bone.class))
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
        
        private Bone(Bone parent, org.blender.dna.Bone bBone) throws IOException
        {
            name = bBone.getName().asString();
            this.parent = parent;
            
            inheritRotation = (bBone.getFlag() & (1 << 9)) != 0; //BONE_HINGE flag
            
            head = Blender.vec3(bBone.getArm_head());
            tail = Blender.vec3(bBone.getArm_tail());
            matrix = Blender.mat3(bBone.getArm_mat()); //Might need to be bone_mat instead.
            invMat = Mat3.invert(matrix);
        }
    }
}
