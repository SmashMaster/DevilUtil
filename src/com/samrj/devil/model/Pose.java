/*
 * Copyright (c) 2016 Sam Johnson
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.samrj.devil.model;

import com.samrj.devil.math.Transform;
import com.samrj.devil.io.IOUtil;
import com.samrj.devil.math.Transform.Property;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Class for storing and manipulating bone pose information. Is not tied to
 * armatures--give to an ArmatureSolver to actually solve a pose.
 * 
 * @author Samuel Johnson (SmashMaster)
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
     */
    public PoseBone getBone(String name)
    {
        return bones.get(name);
    }
    
    /**
     * Returns a collection of each pose bone belonging to this pose.
     */
    public Collection<PoseBone> getBones()
    {
        return Collections.unmodifiableCollection(bones.values());
    }
    
    /**
     * Sets a specific property of a bone. Adds a bone with the given name if
     * none already exists.
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
     * @param pose The pose to intersect with.
     * @return This pose.
     */
    public Pose intersect(Pose pose)
    {
        Iterator<Entry<String, PoseBone>> it = bones.entrySet().iterator();
        while (it.hasNext())
        {
            Entry<String, PoseBone> e = it.next();
            if (!pose.bones.containsKey(e.getKey())) it.remove();
        }
        return this;
    }
    
    /**
     * Removes any bone shared between this and the given pose.
     * 
     * @param pose The pose to mask with.
     * @return This pose.
     */
    public Pose mask(Pose pose)
    {
        for (PoseBone source : pose.bones.values()) bones.remove(source.name); 
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
            target.transform.lerp(source.transform, blend);
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
            transform = Transform.identity();
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
            transform.lerp(bone.transform, blend);
            return this;
        }
    }
}
