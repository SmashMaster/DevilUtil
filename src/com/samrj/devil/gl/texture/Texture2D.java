package com.samrj.devil.gl.texture;

import org.lwjgl.opengl.GL11;

public final class Texture2D
{
    public final int width, height;
    private final int id;
    
    public Texture2D(ImageBuffer image)
    {
        if (image == null) throw new NullPointerException();
        width = image.width;
        height = image.height;
        
        id = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, image.format.bands,
                image.width, image.height, 0, image.format.glEnum,
                image.type.glEnum, image.getBuffer());
        
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
    }
}
