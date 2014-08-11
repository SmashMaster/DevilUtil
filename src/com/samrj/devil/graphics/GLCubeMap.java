package com.samrj.devil.graphics;

import org.lwjgl.opengl.*;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class GLCubeMap
{
    private int id = -1;
    
    private GLCubeMap(TextureData[] textures)
    {
        id = GL11.glGenTextures();
        glDefaultParams();
        
        for (int i=0; i<6; i++)
        {
            TextureData data = textures[i];
            
            GL11.glTexImage2D(GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0,
                    data.format, data.width, data.height, 0, data.baseFormat,
                    GL11.GL_UNSIGNED_BYTE, data.read());
        }
    }
    
    public GLCubeMap(TextureData posX, TextureData negX,
                     TextureData posY, TextureData negY,
                     TextureData posZ, TextureData negZ)
    {
        this(new TextureData[]{posX, negX, posY, negY, posZ, negZ});
    }
    
    private void glDefaultParams()
    {
        glBind();
        glParam(GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        glParam(GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        glParam(GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        glParam(GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        glParam(GL12.GL_TEXTURE_WRAP_R, GL12.GL_CLAMP_TO_EDGE);
    }
    
    public void glBind()
    {
        GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, id);
    }
    
    public void glMultiBind(int i)
    {
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + i);
        glBind();
    }
    
    public void glParam(int name, int value)
    {
        if (!glIsBound()) throw new IllegalStateException();
        GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, name, value);
    }
    
    public void glParam(int name, float value)
    {
        if (!glIsBound()) throw new IllegalStateException();
        GL11.glTexParameterf(GL13.GL_TEXTURE_CUBE_MAP, name, value);
    }
    
    public void glDelete()
    {
        id = -1;
        GL11.glDeleteTextures(id);
    }
    
    public boolean isDeleted()
    {
        return id < 0;
    }
    
    public int id()
    {
        return id;
    }
    
    public boolean glIsBound()
    {
        return GL11.glGetInteger(GL13.GL_TEXTURE_BINDING_CUBE_MAP) == id;
    }
}