/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.samrj.devil.graphics;

import org.lwjgl.opengl.GL11;

/**
 * Contains texture parameters for GLTexture2D.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class Texture2DParams
{
    public int wrapS = GL11.GL_REPEAT;
    public int wrapT = GL11.GL_REPEAT;
    public int minFilter = GL11.GL_LINEAR;
    public int magFilter = GL11.GL_LINEAR;
    
    public void glApply(GLTexture2D texture)
    {
        texture.glParam(GL11.GL_TEXTURE_WRAP_S, wrapS);
        texture.glParam(GL11.GL_TEXTURE_WRAP_T, wrapT);
        texture.glParam(GL11.GL_TEXTURE_MIN_FILTER, minFilter);
        texture.glParam(GL11.GL_TEXTURE_MAG_FILTER, magFilter);
    }
}