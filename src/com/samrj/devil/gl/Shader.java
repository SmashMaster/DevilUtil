package com.samrj.devil.gl;

import com.samrj.devil.io.MemStack;
import com.samrj.devil.io.Memory;
import com.samrj.devil.res.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL20C.*;

/**
 * OpenGL shader object wrapper/loader.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2019 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class Shader extends DGLObj
{
    public static enum State
    {
        NEW, COMPILED, DELETED;
    }
    
    final int id, type;
    private State state;
    private String path;
    
    Shader(int type)
    {
        DGL.checkState();
        if (!DGL.getCapabilities().OpenGL20)
            throw new UnsupportedOperationException("Shaders unsupported in OpenGL < 2.0");
        id = glCreateShader(type);
        this.type = type;
        state = State.NEW;
    }
    
    /**
     * Loads shader sources from the given input stream and then compiles this
     * shader. Buffers the source in native memory.
     * 
     * @param in The input stream to load sources from.
     * @return This shader.
     * @throws IOException If an I/O error occurs.
     */
    public Shader source(InputStream in) throws IOException
    {
        if (state != State.NEW) throw new IllegalStateException("Shader must be new.");
        
        //Source to memory
        int sourceLength = in.available();
        Memory sourceBlock = new Memory(sourceLength);
        ByteBuffer sourceBuffer = sourceBlock.buffer;
        for (int i=0; i<sourceLength; i++) sourceBuffer.put((byte)in.read());

        //Pointer to pointer to memory
        long pointer = MemStack.wrapl(sourceBlock.address);

        //Pointer to length of memory
        long length = MemStack.wrapi(sourceLength);

        //Load shader source
        nglShaderSource(id, 1, pointer, length);

        //Free allocated memory
        MemStack.pop(2);
        sourceBlock.free();
        
        //Compile
        glCompileShader(id);
        
        //Check for errors
        if (glGetShaderi(id, GL_COMPILE_STATUS) != GL_TRUE)
        {
            int logLength = glGetShaderi(id, GL_INFO_LOG_LENGTH);
            String log = glGetShaderInfoLog(id, logLength);
            throw new ShaderException(path != null ? path + " " + log : log);
        }
        
        state = State.COMPILED;
        return this;
    }
    
    /**
     * Loads shader sources from the given resource path and then compiles this
     * shader. Buffers the source in native memory.
     * 
     * @param path The class/file path from which to load sources.
     * @return This shader.
     * @throws IOException If an I/O error occurs.
     */
    public Shader source(String path) throws IOException
    {
        this.path = path;
        return source(Resource.open(path));
    }
    
    /**
     * @return The state of this shader.
     */
    public State state()
    {
        return state;
    }
    
    @Override
    void delete()
    {
        if (state == State.DELETED) return;
        
        glDeleteShader(id);
        state = State.DELETED;
    }
}
