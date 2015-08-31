package com.samrj.devil.gl;

import org.lwjgl.opengl.GL11;

public abstract class Texture
{
    private final int target;
    final int id;
    
    public Texture(int target)
    {
        this.target = target;
        id = GL11.glGenTextures();
    }
    
    final void bind()
    {
        GL11.glBindTexture(target, id);
    }
    
    final void unbind()
    {
        GL11.glBindTexture(target, 0);
    }
    
    final void delete()
    {
        GL11.glDeleteTextures(id);
    }
}
