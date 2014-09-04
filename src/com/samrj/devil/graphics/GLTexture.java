package com.samrj.devil.graphics;

import com.samrj.devil.math.Vector2f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

/**
 * Handles the allocation, loading, manipulation, and deletion of OpenGL texture
 * objects. Provides an object-oriented view of OpenGL's state-based textures.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class GLTexture
{
    public static void glUnbind(int target)
    {
        GL11.glBindTexture(target, 0);
    }
    
    public static void gUnbind()
    {
        glUnbind(GL11.GL_TEXTURE_2D);
    }
    
    public static void glMultiUnbind(int target, int i)
    {
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + i);
        glUnbind(target);
    }
    
    public static void glMultiUnbind(int i)
    {
        glMultiUnbind(GL11.GL_TEXTURE_2D, i);
    }
    
    public static void glMultiUnbindArray(int... iArray)
    {
        for (int i : iArray) glMultiUnbind(GL11.GL_TEXTURE_2D, i);
    }
    
    public final int width, height;
    private int id = -1;
    
    public GLTexture(TextureData data)
    {
        id = GL11.glGenTextures();
        glDefaultParams();
        
        this.width = data.width;
        this.height = data.height;
        
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, data.format, width, height, 0,
                data.baseFormat, GL11.GL_UNSIGNED_BYTE, data.read());
    }
    
    private void glDefaultParams()
    {
        glBind();
        glParam(GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        glParam(GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        glParam(GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        glParam(GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
    }
    
    public void glBind(int target)
    {
        if (isDeleted()) throw new IllegalStateException("Cannot bind deleted texture.");
        GL11.glBindTexture(target, id);
    }
    
    public void glBind()
    {
        glBind(GL11.GL_TEXTURE_2D);
    }
    
    public void glMultiBind(int target, int i)
    {
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + i);
        glBind(target);
    }
    
    public void glMultiBind(int i)
    {
        glMultiBind(GL11.GL_TEXTURE_2D, i);
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