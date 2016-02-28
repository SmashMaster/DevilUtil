package com.samrj.devil.model;

import com.samrj.devil.io.IOUtil;
import com.samrj.devil.model.Transform.Property;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Class for storing and manipulating bone pose information.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Pose
{
    private final Map<String, PoseBone> bones;
    
    Pose(DataInputStream in) throws IOException
    {
        int numBones = in.readInt();
        bones = new HashMap<>(numBones);
        for (int i=0; i<numBones; i++)
        {
            PoseBone bone = new PoseBone(in);
            bones.put(bone.name, bone);
        }
    }
    
    /**
     * Creates a new deep copy of the given pose.
     * 
     * @param pose A pose to copy.
     */
    public Pose(Pose pose)
    {
        bones = new HashMap<>(pose.bones.size());
        for (PoseBone bone : pose.bones.values())
            bones.put(bone.name, new PoseBone(bone));
    }
    
    /**
     * Creates a new blank pose.
     */
    public Pose()
    {
        bones = new HashMap<>();
    }
    
    /**
     * Returns the bone with the given name, or null if there is no such bone.
     * 
     * @param name
     * @return 
     */
    public PoseBone getBone(String name)
    {
        return bones.get(name);
    }
    
    /**
     * Sets a specific property of a bone.
     * 
     * @param name The name of the bone to change.
     * @param property The property to change.
     * @param index The index of the value to change.
     * @param value Any float.
     * @return This pose.
     */
    public Pose setProperty(String name, Property property, int index, float value)
    {
        PoseBone bone = bones.get(name);
        if (bone == null) bones.put(name, bone = new PoseBone(name));
        bone.transform.setProperty(property, index, value);
        return this;
    }
    
    /**
     * Sets this to a blank pose.
     * 
     * @return This pose.
     */
    public Pose clear()
    {
        bones.clear();
        return this;
    }
    
    /**
     * Removes any bones not shared between this and the given pose.
     * 
     * @param pose The pose to mask with.
     * @return This pose.
     */
    public Pose intersect(Pose pose)
    {
        return this;
    }
    
    /**
     * Removes any bone shared between this and the given pose.
     * @param pose
     * @return 
     */
    public Pose remove(Pose pose)
    {
        return this;
    }
    
    /**
     * Adds all bones from the given pose, replacing any old ones.
     * 
     * @param pose The pose to copy.
     * @return This pose.
     */
    public Pose put(Pose pose)
    {
        for (PoseBone source : pose.bones.values())
        {
            PoseBone target = bones.get(source.name);
            if (target == null) bones.put(source.name, target = new PoseBone(source.name));
            target.transform.set(source.transform);
        }
        return this;
    }
    
    /**
     * Mixes in bones from the given pose. New bones are assumed to mix starting
     * from the identity transform.
     * 
     * @param pose The pose to blend into.
     * @param blend The factor to blend by.
     * @return This pose.
     */
    public Pose mix(Pose pose, float blend)
    {
        for (PoseBone source : pose.bones.values())
        {
            PoseBone target = bones.get(source.name);
            if (target == null) bones.put(source.name, target = new PoseBone(source.name));
            target.transform.mix(source.transform, blend);
        }
        return this;
    }
    
    /**
     * Class representing the pose transform of a single bone.
     */
    public class PoseBone
    {
        public final String name;
        public final Transform transform;

        private PoseBone(DataInputStream in) throws IOException
        {
            name = IOUtil.readPaddedUTF(in);
            transform = new Transform(in);
        }

        private PoseBone(PoseBone bone)
        {
            name = bone.name;
            transform = new Transform(bone.transform);
        }
        
        private PoseBone(String name)
        {
            this.name = name;
            transform = new Transform();
        }
        
        /**
         * Sets a single property value for this bone.
         * 
         * @param property The property to change.
         * @param index The index of the value to change.
         * @param value Any float.
         * @return This bone.
         */
        public PoseBone setProperty(Property property, int index, float value)
        {
            transform.setProperty(property, index, value);
            return this;
        }

        /**
         * Copies the given bone's transform to this.
         * 
         * @param bone The bone to copy.
         * @return This bone.
         */
        public PoseBone set(PoseBone bone)
        {
            transform.set(bone.transform);
            return this;
        }
        
        /**
         * Mixes the given bone's transform into this.
         * 
         * @param bone The bone to mix with this.
         * @param blend The factor to blend by.
         * @return This bone.
         */
        public PoseBone mix(PoseBone bone, float blend)
        {
            transform.mix(bone.transform, blend);
            return this;
        }
    }
}
