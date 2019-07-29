package com.samrj.devil.gl;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL12C.*;
import static org.lwjgl.opengl.GL31C.*;

/**
 * OpenGL rectangle texture class.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2019 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class TextureRectangle extends Texture2DAbstract<TextureRectangle>
{
    TextureRectangle()
    {
        super(GL_TEXTURE_RECTANGLE, GL_TEXTURE_BINDING_RECTANGLE);
        if (!(DGL.getCapabilities().OpenGL31 || DGL.getCapabilities().GL_ARB_texture_rectangle))
            throw new UnsupportedOperationException(
                "Rectangle textures unsupported on this machine.");
        
        int oldID = tempBind();
        parami(GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        parami(GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        parami(GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        parami(GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        tempUnbind(oldID);
    }

    @Override
    TextureRectangle getThis()
    {
        return this;
    }
}
