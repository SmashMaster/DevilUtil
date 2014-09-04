package com.samrj.devil.graphics;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

/**
 * Contains texture parameters for GLCubeMap.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class CubeMapParams
{
    public int wrapS = GL12.GL_CLAMP_TO_EDGE;
    public int wrapT = GL12.GL_CLAMP_TO_EDGE;
    public int wrapR = GL12.GL_CLAMP_TO_EDGE;
    public int minFilter = GL11.GL_LINEAR;
    public int magFilter = GL11.GL_LINEAR;
    
    public void glApply(GLCubeMap cubemap)
    {
        cubemap.glParam(GL11.GL_TEXTURE_WRAP_S, wrapS);
        cubemap.glParam(GL11.GL_TEXTURE_WRAP_T, wrapT);
        cubemap.glParam(GL12.GL_TEXTURE_WRAP_R, wrapR);
        cubemap.glParam(GL11.GL_TEXTURE_MIN_FILTER, minFilter);
        cubemap.glParam(GL11.GL_TEXTURE_MAG_FILTER, magFilter);
    }
}
