package com.samrj.devil.gl;

import org.lwjgl.opengl.GL11;

/**
 * OpenGL 2D texture class.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class Texture2D extends Texture2DAbstract<Texture2D>
{
    Texture2D()
    {
        super(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_BINDING_2D);
        
        int oldID = tempBind();
        parami(GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        parami(GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        parami(GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        parami(GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        tempUnbind(oldID);
    }
    
    @Override
    Texture2D getThis()
    {
        return this;
    }
}
