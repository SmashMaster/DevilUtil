package com.samrj.devil.graphics;

import com.samrj.devil.math.Vector2f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;

/**
 * Handles 3D OpenGL texture.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class GLTexture3D
{
    public static void glUnbind()
    {
        
        GL11.glBindTexture(GL12.GL_TEXTURE_3D, 0);
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
    
    public final int width, height, depth;
    private int id = -1;
    
    public GLTexture3D(Texture3DData data, Texture3DParams params)
    {
        if (data == null || params == null) throw new NullPointerException();
        
        GL11.glEnable(GL12.GL_TEXTURE_3D);
        id = GL11.glGenTextures();
        glBind();
        
        this.width = data.width;
        this.height = data.height;
        this.depth = data.depth;
        
        GL12.glTexImage3D(GL12.GL_TEXTURE_3D, 0, data.format, width, height, depth, 0,
                data.baseFormat, GL11.GL_UNSIGNED_BYTE, data.read());
        
        if (TexUtil.isMipmapFilter(params.minFilter))
            GL30.glGenerateMipmap(GL12.GL_TEXTURE_3D);
        
        params.glApply(this);
    }
    
    public GLTexture3D(Texture3DData data)
    {
        this(data, new Texture3DParams());
    }
    
    public void glBind()
    {
        if (isDeleted()) throw new IllegalStateException("Cannot bind deleted texture.");
        GL11.glBindTexture(GL12.GL_TEXTURE_3D, id);
    }
    
    public void glBind(int i)
    {
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + i);
        glBind();
    }
    
    public void glParam(int name, int value)
    {
        if (!glIsBound()) throw new IllegalStateException();
        GL11.glTexParameteri(GL12.GL_TEXTURE_3D, name, value);
    }
    
    public void glParam(int name, float value)
    {
        if (!glIsBound()) throw new IllegalStateException();
        GL11.glTexParameterf(GL12.GL_TEXTURE_3D, name, value);
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
        return GL11.glGetInteger(GL12.GL_TEXTURE_BINDING_3D) == id;
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