package com.samrj.devil.gl;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL12C.*;

/**
 * 3D texture class.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2019 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class Texture3D extends Texture3DAbstract<Texture3D>
{
    Texture3D()
    {
        super(GL_TEXTURE_3D, GL_TEXTURE_BINDING_3D);
        
        int oldID = tempBind();
        parami(GL_TEXTURE_WRAP_S, GL_REPEAT);
        parami(GL_TEXTURE_WRAP_T, GL_REPEAT);
        parami(GL_TEXTURE_WRAP_R, GL_REPEAT);
        parami(GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        parami(GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        tempUnbind(oldID);
    }
    
    @Override
    Texture3D getThis()
    {
        return this;
    }
}
