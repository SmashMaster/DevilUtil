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

import org.lwjgl.system.MemoryStack;

import static org.lwjgl.opengl.GL11C.glReadBuffer;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE_CUBE_MAP_POSITIVE_X;
import static org.lwjgl.opengl.GL20C.glDrawBuffers;
import static org.lwjgl.opengl.GL30C.*;

/**
 * OpenGL frame buffer wrapper. Requires OpenGL 3.0.
 * 
 * @author Samuel Johnson (SmashMaster)
 */
public final class FBO extends DGLObj
{
    private static String statusName(int status)
    {
        switch (status)
        {
            case GL_FRAMEBUFFER_COMPLETE:
                return "GL_FRAMEBUFFER_COMPLETE";
            case GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT:
                return "GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT";
            case GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT:
                return "GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT";
            case GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER:
                return "GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER";
            case GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER:
                return "GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER";
            case GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE:
                return "GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE";
            default: return null;
        }
    }
    
    public final int id;
    private boolean deleted;
    
    FBO()
    {
        DGL.checkState();
        if (!DGL.getCapabilities().OpenGL30) throw new UnsupportedOperationException(
                "Frame buffers unsupported in OpenGL < 3.0");
        id = glGenFramebuffers();
    }
    
    private void ensureBound()
    {
        if (deleted) throw new IllegalStateException("FBO deleted.");
        if (DGL.currentFBO() != this) throw new IllegalStateException("FBO not bound.");
    }

    /**
     * Binds this FBO. Equivalent to calling DGL.bind(this), but returns this.
     */
    public FBO bind()
    {
        DGL.bindFBO(this);
        return this;
    }
    
    void bind(int target)
    {
        glBindFramebuffer(target, id);
    }
    
    /**
     * Attaches the given texture to this frame buffer. Must be bound.
     * 
     * @param texture The texture to attach.
     * @param attachment The attachment type.
     */
    public void texture2D(Texture2D texture, int attachment)
    {
        ensureBound();
        glFramebufferTexture2D(GL_FRAMEBUFFER, attachment, texture.target, texture.id, 0);
    }
    
    /**
     * Attaches a layer of the given 3D texture to this frame buffer. This frame
     * buffer must be bound.
     * 
     * @param texture The 3D texture to attach.
     * @param layer The layer of the given texture to attach.
     * @param attachment The attachment type.
     */
    public void texture3D(Texture3D texture, int layer, int attachment)
    {
        ensureBound();
        if (layer < 0 || layer >= texture.getDepth()) throw new ArrayIndexOutOfBoundsException();
        glFramebufferTexture3D(GL_FRAMEBUFFER, attachment, texture.target, texture.id, 0, layer);
    }
    
    /**
     * Attaches a layer of the given 2D texture array to this frame buffer. This
     * frame buffer must be bound.
     * 
     * @param texture The 2D texture array to attach.
     * @param layer The layer of the given texture array to attach.
     * @param attachment The attachment type.
     */
    public void textureLayer(Texture2DArray texture, int layer, int attachment)
    {
        ensureBound();
        if (layer < 0 || layer >= texture.getDepth()) throw new ArrayIndexOutOfBoundsException();
        glFramebufferTextureLayer(GL_FRAMEBUFFER, attachment, texture.id, 0, layer);
    }
    
    /**
     * Attaches a face of the given cubemap texture to this frame buffer. This
     * frame buffer must be bound.
     * 
     * @param texture The 2D texture array to attach.
     * @param face The face of the given cubemap to attach.
     * @param attachment The attachment type.
     */
    public void textureCubemap(TextureCubemap texture, int face, int attachment)
    {
        ensureBound();
        if (face < 0 || face >= 6) throw new ArrayIndexOutOfBoundsException();
        glFramebufferTexture2D(GL_FRAMEBUFFER, attachment,
                GL_TEXTURE_CUBE_MAP_POSITIVE_X + face, texture.id, 0);
    }
    
    public void texture2DMultisample(Texture2DMultisample texture, int attachment)
    {
        ensureBound();
        glFramebufferTexture2D(GL_FRAMEBUFFER, attachment, texture.target, texture.id, 0);
    }
    
    /**
     * Attaches the given render buffer to this frame buffer. This frame buffer
     * must be bound.
     * 
     * @param rbo The render buffer to attach.
     * @param attachment The attachment type.
     */
    public void renderBuffer(RBO rbo, int attachment)
    {
        ensureBound();
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, attachment, GL_RENDERBUFFER, rbo.id);
    }
    
    /**
     * Enables each of the given color buffer attachments for this frame buffer.
     * 
     * @param a An array of draw buffers.
     */
    public void drawBuffers(int... a)
    {
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            ensureBound();
            glDrawBuffers(stack.ints(a));
        }
    }
    
    /**
     * Specifies which color buffer to read from for this frame buffer.
     * 
     * @param buffer The buffer to read from.
     */
    public void readBuffer(int buffer)
    {
        ensureBound();
        glReadBuffer(buffer);
    }
    
    /**
     * @return The status of this frame buffer, as a string.
     */
    public String getStatus()
    {
        ensureBound();
        int status = glCheckFramebufferStatus(GL_FRAMEBUFFER);
        return statusName(status);
    }
    
    @Override
    void delete()
    {
        if (deleted) return;
        if (DGL.currentReadFBO() == this) DGL.bindFBO(null, GL_READ_FRAMEBUFFER);
        if (DGL.currentDrawFBO() == this) DGL.bindFBO(null, GL_DRAW_FRAMEBUFFER);
        glDeleteFramebuffers(id);
    }
}
