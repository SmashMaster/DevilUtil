package com.samrj.devil.gl;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL31;

/**
 * OpenGL rectangle texture class.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class TextureRectangle extends Texture2DAbstract<TextureRectangle>
{
    TextureRectangle()
    {
        super(GL31.GL_TEXTURE_RECTANGLE, GL31.GL_TEXTURE_BINDING_RECTANGLE);
        if (!(DGL.getCapabilities().OpenGL31 || DGL.getCapabilities().GL_ARB_texture_rectangle))
            throw new UnsupportedOperationException(
                "Rectangle textures unsupported on this machine.");
        
        int oldID = tempBind();
        parami(GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        parami(GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        parami(GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        parami(GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        tempUnbind(oldID);
    }

    @Override
    TextureRectangle getThis()
    {
        return this;
    }
}
