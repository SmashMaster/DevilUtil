package com.samrj.devil.graphics;

import com.samrj.devil.gl.ShaderProgram;
import com.samrj.devil.io.Memory;
import com.samrj.devil.model.ArmatureSolver;
import com.samrj.devil.model.ArmatureSolver.BoneSolver;
import com.samrj.devil.model.Mesh;
import com.samrj.devil.model.ModelObject;
import java.nio.ByteBuffer;
import org.lwjgl.opengl.GL20;

/**
 * Class that performs mesh deformation for armatures.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class MeshSkinner
{
    private final Mesh mesh;
    private final ArmatureSolver solver;
    private final String[] vertexGroups;
    private final Memory matrixBlock;
    private final ByteBuffer matrixData;
    
    public MeshSkinner(ModelObject<Mesh> object)
    {
        mesh = object.data.get();
        solver = object.parent.get().armatureSolver;
        if (solver == null) throw new IllegalArgumentException("Mesh parent is not an armature.");
        vertexGroups = object.vertexGroups;
        matrixBlock = new Memory(vertexGroups.length*16*4);
        matrixData = matrixBlock.buffer;
    }
    
    /**
     * Loads the bone matrices to the shader uniform with the given name.
     * 
     * @param shader The shader program to load into.
     * @param arrayName The name of the matrix array variable to set.
     * @param countName The name of the group count variable to set.
     */
    public void glUniform(ShaderProgram shader, String arrayName, String countName)
    {
        int boneLoc = shader.getUniformLocation(arrayName);
        GL20.nglUniformMatrix4fv(boneLoc, vertexGroups.length, false, matrixBlock.address);
        shader.uniform1i(countName, mesh.numGroups);
    }
    
    /**
     * Loads the bone matrix data from the armature solver into this skinner's
     * buffer.
     */
    public void update()
    {
        matrixData.rewind();
        for (String group : vertexGroups)
        {
            BoneSolver bone = solver.getBone(group);
            if (bone == null) continue;
            bone.skinMatrix.write(matrixData);
        }
    }
    
    /**
     * Frees all native memory allocated by this solver.
     */
    public final void destroy()
    {
        matrixBlock.free();
    }
}
