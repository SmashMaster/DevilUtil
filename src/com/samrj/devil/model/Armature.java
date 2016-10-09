package com.samrj.devil.model;

import com.samrj.devil.io.IOUtil;
import com.samrj.devil.math.Mat3;
import com.samrj.devil.math.Vec3;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    
    Armature(Model model, int modelIndex, DataInputStream in) throws IOException
    {
        super(model, modelIndex, in);
        bones = IOUtil.listFromStream(in, Bone::new);
        bones.forEach(bone -> bone.populate(bones));
        
        nameMap = new HashMap<>(bones.size());
        bones.forEach(bone -> nameMap.put(bone.name, bone));
    }
    
    public Bone getBone(String name)
    {
        return nameMap.get(name);
    }
    
    public class Bone
    {
        public final String name;
        final int parentIndex;
        public final boolean inheritRotation;

        public final Vec3 head, tail;
        public final Mat3 matrix; //bone direction -> object rest direction
        public final Mat3 invMat; //object rest direction -> bone direction

        private Bone parent;

        private Bone(DataInputStream in) throws IOException
        {
            name = IOUtil.readPaddedUTF(in);
            parentIndex = in.readInt();
            inheritRotation = in.readInt() != 0;
            head = new Vec3(in);
            tail = new Vec3(in);
            matrix = new Mat3(in);
            invMat = Mat3.invert(matrix);
        }

        void populate(List<Bone> bones)
        {
            if (parentIndex >= 0) parent = bones.get(parentIndex);
        }

        public Bone getParent()
        {
            return parent;
        }
    }
}
