package com.samrj.devil.gl;

import static org.lwjgl.opengl.GL11C.*;

/**
 * OpenGL 2D texture class.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2019 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class Texture2D extends Texture2DAbstract<Texture2D>
{
    Texture2D()
    {
        super(GL_TEXTURE_2D, GL_TEXTURE_BINDING_2D);
        
        int oldID = tempBind();
        parami(GL_TEXTURE_WRAP_S, GL_REPEAT);
        parami(GL_TEXTURE_WRAP_T, GL_REPEAT);
        parami(GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        parami(GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        tempUnbind(oldID);
    }
    
    @Override
    Texture2D getThis()
    {
        return this;
    }
}
