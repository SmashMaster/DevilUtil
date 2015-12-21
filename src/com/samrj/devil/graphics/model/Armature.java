package com.samrj.devil.graphics.model;

import com.samrj.devil.graphics.model.Transform.Property;
import com.samrj.devil.io.IOUtil;
import com.samrj.devil.io.Memory;
import com.samrj.devil.math.topo.DAG;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Armature
{
    public final String name;
    public final Bone[] bones;
    private final Map<String, Bone> nameMap;
    private final List<Bone> topoSorted;
    
    public final Memory matrixBlock;
    public final ByteBuffer matrixData;
    
    Armature(DataInputStream in) throws IOException
    {
        name = IOUtil.readPaddedUTF(in);
        bones = IOUtil.arrayFromStream(in, Bone.class, Bone::new);
        for (Bone bone : bones) bone.populate(bones);
        
        nameMap = new HashMap<>(bones.length);
        for (Bone bone : bones) nameMap.put(bone.name, bone);
        
        //Do a topological sort so that we can simply update matrices in-order.
        DAG<Bone> dag = new DAG<>();
        for (Bone bone : bones) dag.add(bone);
        for (Bone bone : bones)
        {
            Bone parent = bone.getParent();
            if (parent == null) continue;
            dag.addEdge(parent, bone);
        }
        topoSorted = dag.sort();
        
        matrixBlock = new Memory(bones.length*16*4);
        matrixData = matrixBlock.buffer;
    }
    
    public Bone getBone(String name)
    {
        return nameMap.get(name);
    }
    
    public void setBoneProperty(String name, Property property, int index, float value)
    {
        Bone bone = nameMap.get(name);
        if (bone == null) return;
        bone.transform.setProperty(property, index, value);
    }
    
    
    public void bufferPoseMatrices(String[] vertexGroups)
    {
        matrixData.rewind();
        for (Bone bone : topoSorted) bone.updatePoseMatrix();
        
        for (String group : vertexGroups)
        {
            Bone bone = nameMap.get(group);
            if (bone == null) continue;
            bone.poseMatrix.write(matrixData);
        }
    }
    
    final void destroy()
    {
        matrixBlock.free();
    }
}
