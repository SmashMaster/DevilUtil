package com.samrj.devil.graphics;

import com.samrj.devil.gl.ShaderProgram;
import com.samrj.devil.math.Mat4;
import com.samrj.devil.model.ArmatureSolver;
import com.samrj.devil.model.ArmatureSolver.BoneSolver;
import com.samrj.devil.model.Mesh;
import com.samrj.devil.model.ModelObject;
import com.samrj.devil.util.IOUtil;

import java.nio.FloatBuffer;
import java.util.List;

import static org.lwjgl.opengl.GL20C.glUniformMatrix4fv;
import static org.lwjgl.system.MemoryUtil.*;

/**
 * Class that performs mesh deformation for armatures.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2019 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class MeshSkinner
{
    public final int numGroups;
    
    private final List<BoneSolver> bones;
    private final FloatBuffer matData;
    
    private FloatBuffer prevMatData;
    private boolean onFirstFrame = true;
    
    public MeshSkinner(ModelObject<Mesh> object, ArmatureSolver solver)
    {
        numGroups = object.data.get().numGroups;
        bones = IOUtil.mapList(object.vertexGroups, solver::getBone);
        matData = memAllocFloat(bones.size()*16);
    }
    
    /**
     * Loads the bone matrix data from the armature solver into this skinner's
     * buffer.
     */
    public void update()
    {
        if (prevMatricesEnabled())
            memCopy(matData, prevMatData);
        
        matData.clear();
        bones.forEach(bone ->
        {
            if (bone == null) new Mat4().write(matData);
            else bone.skinMatrix.write(matData);
        });
        matData.flip();
        
        if (onFirstFrame && prevMatricesEnabled())
        {
            memCopy(matData, prevMatData);
            onFirstFrame = false;
        }
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
        glUniformMatrix4fv(loc, false, matData);
    }
    
    /**
     * Returns whether space has been allocated for previous bone matrices.
     */
    public boolean prevMatricesEnabled()
    {
        return prevMatData != null;
    }
    
    /**
     * Allocates space the previous bone matrices. Useful for velocity-buffer
     * motion blur.
     */
    public void enablePrevMatrices()
    {
        if (prevMatricesEnabled()) throw new IllegalStateException();
        
        prevMatData = memAllocFloat(matData.capacity());
    }
    
    public void uniformPrevMats(ShaderProgram shader, String arrayName)
    {
        if (!prevMatricesEnabled()) throw new IllegalStateException();
        
        int loc = shader.getUniformLocation(arrayName);
        glUniformMatrix4fv(loc, false, prevMatData);
    }
    
    /**
     * Frees all native memory allocated by this solver.
     */
    public final void destroy()
    {
        memFree(matData);
        if (prevMatricesEnabled()) memFree(prevMatData);
    }
}
