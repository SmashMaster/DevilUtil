package com.samrj.devil.graphics;

import com.samrj.devil.math.Vector2f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;

/**
 * Handles the allocation, loading, manipulation, and deletion of OpenGL texture
 * objects. Provides an object-oriented view of OpenGL's state-based textures.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class GLTexture2D
{
    public static void glUnbind()
    {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
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
    
    public final int width, height;
    private int id = -1;
    
    public GLTexture2D(Texture2DData data, Texture2DParams params)
    {
        if (data == null || params == null) throw new NullPointerException();
        boolean enabling = false;
        if (!GL11.glIsEnabled(GL11.GL_TEXTURE_2D)) {
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            enabling = true;
        }
        id = GL11.glGenTextures();
        glBind();
        
        this.width = data.width;
        this.height = data.height;
        
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, data.format, width, height, 0,
                data.baseFormat, GL11.GL_UNSIGNED_BYTE, data.read());
        
        if (TexUtil.isMipmapFilter(params.minFilter))
            GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
        
        params.glApply(this);
        if (enabling) GL11.glDisable(GL11.GL_TEXTURE_2D);
    }
    
    public GLTexture2D(Texture2DData data)
    {
        this(data, new Texture2DParams());
    }
    
    public void glBind()
    {
        if (isDeleted()) throw new IllegalStateException("Cannot bind deleted texture.");
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
    }
    
    public void glBind(int i)
    {
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + i);
        glBind();
    }
    
    public void glParam(int name, int value)
    {
        if (!glIsBound()) throw new IllegalStateException();
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, name, value);
    }
    
    public void glParam(int name, float value)
    {
        if (!glIsBound()) throw new IllegalStateException();
        GL11.glTexParameterf(GL11.GL_TEXTURE_2D, name, value);
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
    
    public boolean glIsBound()
    {
        return GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D) == id;
    }
    
    public int id()
    {
        return id;
    }
    
    public Vector2f size()
    {
        return new Vector2f(width, height);
    }
}
