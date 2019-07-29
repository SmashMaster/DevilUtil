package com.samrj.devil.gl;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL30C.*;

/**
 * 2D texture array class.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2019 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class Texture2DArray extends Texture3DAbstract<Texture2DArray>
{
    Texture2DArray()
    {
        super(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_BINDING_2D_ARRAY);
        
        int oldID = tempBind();
        parami(GL_TEXTURE_WRAP_S, GL_REPEAT);
        parami(GL_TEXTURE_WRAP_T, GL_REPEAT);
        parami(GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        parami(GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        tempUnbind(oldID);
    }

    @Override
    Texture2DArray getThis()
    {
        return this;
    }
}
