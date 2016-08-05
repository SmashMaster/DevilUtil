/*
 * Copyright (c) 2016 Sam Johnson
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

import com.samrj.devil.io.Memory;
import java.nio.ByteBuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL43;

/**
 * OpenGL shader storage buffer wrapper. Requires OpenGL 4.3.
 * 
 * Used for uploading very large, read-only blocks of data to shaders.
 * 
 * @author Samuel Johnson (SmashMaster)
 */
public class SSBO extends DGLObj
{
    final int id;
    
    SSBO()
    {
        id = GL15.glGenBuffers();
    }
    
    public void upload(ByteBuffer data)
    {
        int prevBinding = GL11.glGetInteger(GL43.GL_SHADER_STORAGE_BUFFER_BINDING);
        GL15.glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER, id);
        GL15.glBufferData(GL43.GL_SHADER_STORAGE_BUFFER, data.remaining(), data, GL15.GL_STATIC_READ);
        GL15.glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER, prevBinding);
    }
    
    public void upload(long address, long length)
    {
        int prevBinding = GL11.glGetInteger(GL43.GL_SHADER_STORAGE_BUFFER_BINDING);
        GL15.glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER, id);
        GL15.nglBufferData(GL43.GL_SHADER_STORAGE_BUFFER, length, address, GL15.GL_STATIC_READ);
        GL15.glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER, prevBinding);
    }
    
    public void upload(Memory memory)
    {
        if (memory.isFree()) throw new IllegalArgumentException();
        upload(memory.address, memory.size);
    }
    
    @Override
    void delete()
    {
        GL15.glDeleteBuffers(id);
    }
}
