package com.samrj.devil.gl;

import org.lwjgl.opengl.GL11;

public class Texture2D
{
    private final int id;
    private boolean deleted;
    
    Texture2D()
    {
        id = GL11.glGenTextures();
    }
    
    void bind()
    {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
    }
    
    void unbind()
    {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
    }
    
    void delete()
    {
        GL11.glDeleteTextures(id);
        deleted = true;
    }
}
