package com.samrj.devil.graphics;

import org.lwjgl.opengl.*;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class GLCubeMap
{
    public static void glUnbind()
    {
        GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, 0);
    }
    
    public static void glUnbind(int i)
    {
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + i);
        glUnbind();
    }
    
    public static void glUnbind(int... iArray)
    {
        for (int i : iArray) glUnbind(i);
    }
    
    private int id = -1;
    
    private GLCubeMap(Texture2DData[] dataArray, CubeMapParams params)
    {
        id = GL11.glGenTextures();
        glBind();
        
        for (int i=0; i<6; i++)
        {
            Texture2DData data = dataArray[i];
            
            GL11.glTexImage2D(GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0,
                    data.format, data.width, data.height, 0, data.baseFormat,
                    GL11.GL_UNSIGNED_BYTE, data.read());
        }
        
        if (Texture2DData.isMipmapFilter(params.minFilter))
                GL30.glGenerateMipmap(GL13.GL_TEXTURE_CUBE_MAP);
        
        params.glApply(this);
    }
    
    public GLCubeMap(Texture2DData posX, Texture2DData negX,
                     Texture2DData posY, Texture2DData negY,
                     Texture2DData posZ, Texture2DData negZ,
                     CubeMapParams params)
    {
        this(new Texture2DData[]{posX, negX, posY, negY, posZ, negZ}, params);
    }
    
    public GLCubeMap(Texture2DData posX, Texture2DData negX,
                     Texture2DData posY, Texture2DData negY,
                     Texture2DData posZ, Texture2DData negZ)
    {
        this(new Texture2DData[]{posX, negX, posY, negY, posZ, negZ},
                new CubeMapParams());
    }
    
    public void glBind()
    {
        GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, id);
    }
    
    public void glBind(int i)
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