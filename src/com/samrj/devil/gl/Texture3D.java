package com.samrj.devil.gl;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

/**
 * 3D texture class.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class Texture3D extends Texture3DAbstract
{
    Texture3D()
    {
        super(GL12.GL_TEXTURE_3D, GL12.GL_TEXTURE_BINDING_3D);
        
        int oldID = tempBind();
        parami(GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        parami(GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        parami(GL12.GL_TEXTURE_WRAP_R, GL11.GL_REPEAT);
        parami(GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        parami(GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        tempUnbind(oldID);
    }
    
    @Override
    Texture getThis()
    {
        return this;
    }
}
