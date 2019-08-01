package com.samrj.devil.gl;

import com.samrj.devil.res.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;

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
     * Loads shader sources from the given native ByteBuffer and then compiles
     * this shader.
     * 
     * @param buffer The ByteBuffer to load the source from.
     * @return This shader.
     * @throws IOException If an I/O error occurs.
     */
    public Shader source(ByteBuffer buffer)
    {
        if (state != State.NEW) throw new IllegalStateException("Shader must be new.");
        
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            PointerBuffer string = stack.pointers(buffer);
            IntBuffer length = stack.ints(buffer.remaining());

            //Load shader source
            glShaderSource(id, string, length);
        }
        
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
     * Loads shader source code from the given stream, and then closes it.
     * 
     * @param in The InputStream to load the source from.
     * @return This shader.
     * @throws IOException If an I/O error occurs.
     */
    public Shader source(InputStream in) throws IOException
    {
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            byte[] bytes = in.readAllBytes();
            in.close();
            ByteBuffer buffer = stack.malloc(bytes.length);
            buffer.put(bytes);
            buffer.flip();
            return source(buffer);
        }
    }
    
    /**
     * Loads shader source code from the given String.
     */
    public Shader source(String string)
    {
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            return source(stack.ASCIISafe(string));
        }
    }
    
    /**
     * Loads shader sources from the given resource path and then compiles this
     * shader. Buffers the source in native memory.
     * 
     * @param path The class/file path from which to load sources.
     * @return This shader.
     * @throws IOException If an I/O error occurs.
     */
    public Shader sourceFromRes(String path) throws IOException
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
