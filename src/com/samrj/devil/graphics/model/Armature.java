package com.samrj.devil.graphics.model;

import com.samrj.devil.io.IOUtil;
import com.samrj.devil.io.Memory;
import com.samrj.devil.math.topo.DAG;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Armature implements DataBlock
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
        
        nameMap = new HashMap<>();
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
    
    public void updatePoseMatrices()
    {
        matrixData.rewind();
        for (Bone bone : topoSorted)
        {
            bone.updatePoseMatrix();
            bone.poseMatrix.write(matrixData);
        }
    }
    
    final void destroy()
    {
        matrixBlock.free();
    }

    @Override
    public Type getType()
    {
        return Type.ARMATURE;
    }
}
