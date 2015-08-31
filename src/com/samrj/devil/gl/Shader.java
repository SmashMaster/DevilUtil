package com.samrj.devil.gl;

import com.samrj.devil.io.Memory;
import com.samrj.devil.io.Memory.Block;
import static com.samrj.devil.io.Memory.memUtil;
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
     * shader. Uses the given memory for buffering the sources.
     * 
     * @param memory The memory to use for loading.
     * @param in The input stream to load sources from.
     * @throws IOException If an I/O error occurs.
     */
    public void source(Memory memory, InputStream in) throws IOException
    {
        if (state != State.NEW) throw new IllegalStateException("Shader must be new.");
        
        //Source to memory
        int sourceLength = in.available();
        Block sourceBlock = memory.alloc(sourceLength);
        ByteBuffer sourceBuffer = sourceBlock.readUnsafe();
        for (int i=0; i<sourceLength; i++) sourceBuffer.put((byte)in.read());

        //Pointer to memory
        Block pointerBlock = memory.alloc(8);
        ByteBuffer pointerBuffer = pointerBlock.read();
        pointerBuffer.putLong(sourceBlock.address());
        pointerBuffer.reset();

        //Length to memory
        Block lengthBlock = memory.alloc(4);
        ByteBuffer lengthBuffer = lengthBlock.read();
        lengthBuffer.putInt(sourceLength);
        lengthBuffer.reset();

        //Load shader source
        GL20.glShaderSource(id, 1, pointerBuffer, lengthBuffer);

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
     * Loads shader sources from the given input stream and then compiles this
     * shader. Uses DevilUtil default memory for buffering the sources--may not
     * be sufficient for large sources!
     * 
     * @param in The input stream to load sources from.
     * @throws IOException If an I/O error occurs.
     */
    public void source(InputStream in) throws IOException
    {
        source(memUtil, in);
    }
    
    /**
     * Loads shader sources from the given resource path and then compiles this
     * shader. Uses the given memory for buffering the sources.
     * 
     * @param memory The memory to use for loading.
     * @param path The class/file path from which to load sources.
     * @throws IOException If an I/O error occurs.
     */
    public void source(Memory memory, String path) throws IOException
    {
        source(memory, Resource.open(path));
    }
    
     /**
     * Loads shader sources from the given resource path and then compiles this
     * shader. Uses DevilUtil default memory for buffering the sources--may not
     * be sufficient for large sources!
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
