package com.samrj.devil.graphics.model;

import com.samrj.devil.io.IOUtil;
import com.samrj.devil.io.Memory;
import com.samrj.devil.math.topo.DAG;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

public class Armature implements DataBlock
{
    public final String name;
    public final Bone[] bones;
    private final List<Bone> topo;
    
    public final Memory matrixBlock;
    public final ByteBuffer matrixData;
    
    Armature(DataInputStream in) throws IOException
    {
        name = IOUtil.readPaddedUTF(in);
        bones = IOUtil.arrayFromStream(in, Bone.class, Bone::new);
        for (Bone bone : bones) bone.populate(bones);
        
        //Do a topological sort so that we can simply update matrices in-order.
        DAG<Bone> dag = new DAG<>();
        for (Bone bone : bones) dag.add(bone);
        for (Bone bone : bones)
        {
            Bone parent = bone.getParent();
            if (parent == null) continue;
            dag.addEdge(parent, bone);
        }
        topo = dag.sort();
        
        matrixBlock = new Memory(bones.length*16*4);
        matrixData = matrixBlock.buffer;
    }
    
    public void updatePoseMatrices()
    {
        matrixData.rewind();
        for (Bone bone : topo)
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
