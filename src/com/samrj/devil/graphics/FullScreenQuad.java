package com.samrj.devil.graphics;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;

/**
 * OpenGL forward compatible full-screen quad class.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class FullScreenQuad
{
    private final GLVertexPool vertexPool;
    
    /**
     * @param shader The shader this FSQ will be rendered with.
     */
    public FullScreenQuad(GLShader shader)
    {
        shader.glUse();
        vertexPool = new GLVertexPool(shader, 4, 6, 2, GL11.GL_TRIANGLES, GL15.GL_STATIC_DRAW);
        vertexPool.addAttribute("in_pos", false, 2);
        vertexPool.addVertices(-1f, -1f,
                               -1f, 1f,
                               1f, 1f,
                               1f, -1f);
        vertexPool.addIndices(0, 1, 2, 3);
        shader.glUnuse();
    }
    
    public void draw()
    {
        vertexPool.draw();
    }
    
    public void glDelete()
    {
        vertexPool.glDelete();
    }
    
    public boolean isDeleted()
    {
        return vertexPool.isDeleted();
    }
    
    public void setShader(GLShader shader)
    {
        vertexPool.setShader(shader);
    }
}
