package com.samrj.devil.gl;

import com.samrj.devil.io.Memory;
import com.samrj.devil.res.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

/**
 * OpenGL shader object wrapper/loader.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class Shader
{
    public static enum State
    {
        NEW, COMPILED, DELETED;
    }
    
    final int id, type;
    private State state;
    
    Shader(int type)
    {
        id = GL20.glCreateShader(type);
        this.type = type;
        state = State.NEW;
    }
    
    /**
     * Loads shader sources from the given input stream and then compiles this
     * shader. Buffers the source in native memory.
     * 
     * @param in The input stream to load sources from.
     * @throws IOException If an I/O error occurs.
     */
    public void source(InputStream in) throws IOException
    {
        if (state != State.NEW) throw new IllegalStateException("Shader must be new.");
        
        //Source to memory
        int sourceLength = in.available();
        Memory sourceBlock = new Memory(sourceLength);
        ByteBuffer sourceBuffer = sourceBlock.buffer;
        for (int i=0; i<sourceLength; i++) sourceBuffer.put((byte)in.read());

        //Pointer to memory
        Memory pointerBlock = Memory.wrapl(sourceBlock.address);

        //Length to memory
        Memory lengthBlock = Memory.wrapi(sourceLength);

        //Load shader source
        GL20.nglShaderSource(id, 1, pointerBlock.address, lengthBlock.address);

        //Free allocated memory
        lengthBlock.free();
        pointerBlock.free();
        sourceBlock.free();
        
        //Compile
        GL20.glCompileShader(id);
        
        if (GL20.glGetShaderi(id, GL20.GL_COMPILE_STATUS) != GL11.GL_TRUE)
        {
            int logLength = GL20.glGetShaderi(id, GL20.GL_INFO_LOG_LENGTH);
            String log = GL20.glGetShaderInfoLog(id, logLength);
            throw new ShaderException(log);
        }
        
        state = State.COMPILED;
    }
    
    /**
     * Loads shader sources from the given resource path and then compiles this
     * shader. Buffers the source in native memory.
     * 
     * @param path The class/file path from which to load sources.
     * @throws IOException If an I/O error occurs.
     */
    public void source(String path) throws IOException
    {
        source(Resource.open(path));
    }
    
    /**
     * @return The state of this shader.
     */
    public State state()
    {
        return state;
    }
    
    void delete()
    {
        if (state == State.DELETED) return;
        
        GL20.glDeleteShader(id);
        state = State.DELETED;
    }
}
