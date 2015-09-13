package com.samrj.devil.gl;

import com.samrj.devil.math.Util;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

/**
 * OpenGL 2D texture class.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class Texture2D extends Texture2DAbstract
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
    
    /**
     * Generates mipmaps for this texture.
     */
    public void generateMipmap()
    {
        if (!DGL.getCapabilities().OpenGL30) throw new UnsupportedOperationException();
        if (!Util.isPower2(getWidth()) || !Util.isPower2(getHeight()))
            throw new IllegalStateException("Cannot generate mipmap for NPOT texture.");
        
        int oldID = tempBind();
        GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
        tempUnbind(oldID);
    }
}
