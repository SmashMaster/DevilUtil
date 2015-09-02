package com.samrj.devil.gl;

import org.lwjgl.opengl.GL11;

/**
 * Abstract OpenGL texture class.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public abstract class Texture
{
    public final int id, target, binding;
    private boolean deleted;
    
    Texture(int target, int binding)
    {
        id = GL11.glGenTextures();
        this.target = target;
        this.binding = binding;
    }
    
    /**
     * @return Whether this texture is bound to the currently active texture channel.
     */
    public final boolean isBound()
    {
        return !deleted && GL11.glGetInteger(binding) == id;
    }
    
    final int tempBind()
    {
        int oldID = GL11.glGetInteger(binding);
        if (oldID != id) GL11.glBindTexture(target, id);
        return oldID;
    }
    
    final void tempUnbind(int oldID)
    {
        if (oldID == id) return;
        GL11.glBindTexture(target, oldID);
    }
    
    /**
     * Binds this OpenGL texture to whichever texture unit is currently active.
     */
    public final void bind()
    {
        if (deleted) throw new IllegalStateException("Cannot bind deleted texture.");
        GL11.glBindTexture(target, id);
    }
    
    /**
     * Unbinds any texture currently bound to the current texture unit. Might
     * not be this texture! Manage your texture state carefully.
     */
    public final void unbind()
    {
        if (isBound()) GL11.glBindTexture(target, 0);
    }
    
    final void delete()
    {
        GL11.glDeleteTextures(id);
        deleted = true;
    }
    
    /**
     * Sets the given parameter to the given int for this texture. The texture
     * must be bound.
     * 
     * @param param The OpenGL texture parameter to set.
     * @param value The value to set the parameter to.
     */
    public final void parami(int param, int value)
    {
        if (!isBound()) throw new IllegalStateException("Texture must be bound.");
        GL11.glTexParameteri(target, param, value);
    }
    
    /**
     * Sets the given parameter to the given float for this texture. The texture
     * must be bound.
     * 
     * @param param The OpenGL texture parameter to set.
     * @param value The value to set the parameter to.
     */
    public final void paramf(int param, float value)
    {
        if (!isBound()) throw new IllegalStateException("Texture must be bound.");
        GL11.glTexParameterf(target, param, value);
    }
}
