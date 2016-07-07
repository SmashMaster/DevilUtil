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
import org.lwjgl.system.MemoryUtil;

/**
 * Class that performs mesh deformation for armatures.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2016 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class MeshSkinner
{
    public final int numGroups;
    
    private final List<BoneSolver> bones;
    private final Memory matBlock;
    private final ByteBuffer matData;
    
    private Memory prevMatBlock;
    private ByteBuffer prevMatData;
    
    public MeshSkinner(ModelObject<Mesh> object, ArmatureSolver solver)
    {
        numGroups = object.data.get().numGroups;
        bones = IOUtil.mapList(object.vertexGroups, solver::getBone);
        matBlock = new Memory(bones.size()*16*4);
        matData = matBlock.buffer;
    }
    
    /**
     * Loads the bone matrix data from the armature solver into this skinner's
     * buffer.
     */
    public void update()
    {
        if (prevMatricesEnabled())
            MemoryUtil.memCopy(matBlock.address, prevMatBlock.address, matBlock.size);
        
        matData.rewind();
        bones.forEach(bone -> bone.skinMatrix.write(matData));
    }
    
    /**
     * Loads the bone matrices to the shader uniform with the given name.
     * 
     * @param shader The shader program to load into.
     * @param arrayName The name of the matrix array variable to set.
     */
    public void uniformMats(ShaderProgram shader, String arrayName)
    {
        int loc = shader.getUniformLocation(arrayName);
        GL20.nglUniformMatrix4fv(loc, bones.size(), false, matBlock.address);
    }
    
    /**
     * Returns whether space has been allocated for previous bone matrices.
     */
    public boolean prevMatricesEnabled()
    {
        return prevMatBlock != null;
    }
    
    /**
     * Allocates space the previous bone matrices. Useful for velocity-buffer
     * motion blur.
     */
    public void enablePrevMatrices()
    {
        if (prevMatricesEnabled()) throw new IllegalStateException();
        
        prevMatBlock = new Memory(matBlock.size);
        prevMatData = prevMatBlock.buffer;
    }
    
    public void uniformPrevMats(ShaderProgram shader, String arrayName)
    {
        if (!prevMatricesEnabled()) throw new IllegalStateException();
        
        int loc = shader.getUniformLocation(arrayName);
        GL20.nglUniformMatrix4fv(loc, bones.size(), false, prevMatBlock.address);
    }
    
    /**
     * Frees all native memory allocated by this solver.
     */
    public final void destroy()
    {
        matBlock.free();
        if (prevMatricesEnabled()) prevMatBlock.free();
    }
}
