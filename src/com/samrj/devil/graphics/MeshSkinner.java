package com.samrj.devil.graphics;

import com.samrj.devil.gl.ShaderProgram;
import com.samrj.devil.io.IOUtil;
import com.samrj.devil.io.Memory;
import com.samrj.devil.model.ArmatureSolver;
import com.samrj.devil.model.ArmatureSolver.BoneSolver;
import com.samrj.devil.model.Mesh;
import com.samrj.devil.model.ModelObject;
import java.nio.ByteBuffer;
import java.util.List;
import org.lwjgl.opengl.GL20;

/**
 * Class that performs mesh deformation for armatures.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2016 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class MeshSkinner
{
    private final int numGroups;
    private final List<BoneSolver> bones;
    private final Memory matrixBlock;
    private final ByteBuffer matrixData;
    
    public MeshSkinner(ModelObject<Mesh> object, ArmatureSolver solver)
    {
        numGroups = object.data.get().numGroups;
        bones = IOUtil.mapList(object.vertexGroups, solver::getBone);
        matrixBlock = new Memory(bones.size()*16*4);
        matrixData = matrixBlock.buffer;
    }
    
    /**
     * Loads the bone matrix data from the armature solver into this skinner's
     * buffer.
     */
    public void update()
    {
        matrixData.rewind();
        bones.forEach(bone -> bone.skinMatrix.write(matrixData));
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
        GL20.nglUniformMatrix4fv(boneLoc, bones.size(), false, matrixBlock.address);
        shader.uniform1i(countName, numGroups);
    }
    
    /**
     * Frees all native memory allocated by this solver.
     */
    public final void destroy()
    {
        matrixBlock.free();
    }
}
