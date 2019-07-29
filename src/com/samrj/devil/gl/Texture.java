/*
 * Copyright (c) 2019 Sam Johnson
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.samrj.devil.gl;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL13C.*;
import static org.lwjgl.opengl.GL30C.*;

/**
 * Abstract OpenGL texture class.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @param <T> This texture's own type.
 */
public abstract class Texture<T extends Texture<T>> extends DGLObj
{
    public final int id, target, binding;
    private boolean deleted;
    private boolean hasMipmaps;
    private long vramUsage;
    
    Texture(int target, int binding)
    {
        DGL.checkState();
        id = glGenTextures();
        this.target = target;
        this.binding = binding;
    }
    
    abstract T getThis();
    
    /**
     * @return Whether this texture is bound to the currently active texture channel.
     */
    public final boolean isBound()
    {
        return !deleted && glGetInteger(binding) == id;
    }
    
    final int tempBind()
    {
        int oldID = glGetInteger(binding);
        if (oldID != id) glBindTexture(target, id);
        return oldID;
    }
    
    final void tempUnbind(int oldID)
    {
        if (oldID == id) return;
        glBindTexture(target, oldID);
    }
    
    final void setVRAMUsage(long bits)
    {
        if (bits < 0) throw new IllegalArgumentException();
        if (deleted) return;
        if (hasMipmaps) bits *= 2;
        Profiler.addUsedVRAM(bits - vramUsage);
        vramUsage = bits;
    }
    
    /**
     * Binds this OpenGL texture to whichever texture unit is currently active.
     * 
     * @return This texture.
     */
    public final T bind()
    {
        if (deleted) throw new IllegalStateException("Cannot bind deleted texture.");
        glBindTexture(target, id);
        return getThis();
    }
    
    /**
     * Temporarily activates the given texture unit, binds this texture to it,
     * then activates whichever unit was active before this method was called.
     * 
     * @param texture The OpenGL texture unit enum to bind to.
     * @return This texture.
     */
    public final T bind(int texture)
    {
        if (deleted) throw new IllegalStateException("Cannot bind deleted texture.");
        if (texture < GL_TEXTURE0) throw new IllegalArgumentException();
        int old = glGetInteger(GL_ACTIVE_TEXTURE);
        glActiveTexture(texture);
        bind();
        glActiveTexture(old);
        return getThis();
    }
    
    /**
     * Unbinds any texture currently bound to the current texture unit. Might
     * not be this texture! Manage your texture state carefully.
     * 
     * @return This texture.
     */
    public final T unbind()
    {
        if (isBound()) glBindTexture(target, 0);
        return getThis();
    }
    
    @Override
    final void delete()
    {
        Profiler.removeUsedVRAM(vramUsage);
        glDeleteTextures(id);
        deleted = true;
    }
    
    /**
     * Sets the given parameter to the given int for this texture. The texture
     * must be bound.
     * 
     * @param param The OpenGL texture parameter to set.
     * @param value The value to set the parameter to.
     * @return This texture.
     */
    public final T parami(int param, int value)
    {
        if (!isBound()) throw new IllegalStateException("Texture must be bound.");
        glTexParameteri(target, param, value);
        return getThis();
    }
    
    /**
     * Sets the given parameter to the given float for this texture. The texture
     * must be bound.
     * 
     * @param param The OpenGL texture parameter to set.
     * @param value The value to set the parameter to.
     * @return This texture.
     */
    public final T paramf(int param, float value)
    {
        if (!isBound()) throw new IllegalStateException("Texture must be bound.");
        glTexParameterf(target, param, value);
        return getThis();
    }
    
    /**
     * Generates mipmaps for this texture.
     * 
     * @return This texture.
     */
    public final T generateMipmap()
    {
        int oldID = tempBind();
        glGenerateMipmap(target);
        if (!hasMipmaps) setVRAMUsage(vramUsage*2);
        hasMipmaps = true;
        tempUnbind(oldID);
        return getThis();
    }
}
