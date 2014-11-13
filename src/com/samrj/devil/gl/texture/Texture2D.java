package com.samrj.devil.gl.texture;

import java.io.IOException;
import org.lwjgl.opengl.GL11;

public final class Texture2D
{
    public final int width, height;
    public final TexFormat format;
    public final int id;
    
    public Texture2D(ImageBuffer image)
    {
        if (image == null) throw new NullPointerException();
        width = image.width;
        height = image.height;
        format = image.format;
        
        id = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, format.glFormat,
                image.width, image.height, 0, format.glBaseFormat,
                format.type.glEnum, image.getBuffer());
        
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
    }
    
    public Texture2D(String path) throws IOException
    {
        this(new ImageBuffer(path));
    }
}
