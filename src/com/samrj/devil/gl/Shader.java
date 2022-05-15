package com.samrj.devil.gl;

import com.samrj.devil.util.IOUtil;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;

import static org.lwjgl.opengl.GL11C.GL_TRUE;
import static org.lwjgl.opengl.GL20C.*;

/**
 * OpenGL shader object wrapper/loader.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2022 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class Shader extends DGLObj
{
    public enum State
    {
        NEW, COMPILED, DELETED;
    }
    
    final int id, type;
    private State state;
    private Path path;
    
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
     * Loads shader sources from the native ByteBuffer and then compiles this shader.
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
            byte[] bytes = IOUtil.readAllBytes(in);
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
     * Loads shader sources from the given file path and then compiles this shader.
     * 
     * @param path The file path from which to load the source.
     * @return This shader.
     * @throws IOException If an I/O error occurs.
     */
    public Shader sourceFromFile(Path path) throws IOException
    {
        this.path = path;

        try (FileChannel channel = FileChannel.open(path))
        {
            long size = channel.size();
            if (size > Integer.MAX_VALUE) throw new IOException("File size > 2.15GB");
            
            try (MemoryStack stack = MemoryStack.stackPush())
            {
                ByteBuffer buffer = stack.malloc((int)size);
                channel.read(buffer);
                buffer.flip();
                return source(buffer);
            }
        }
    }

    /**
     * Loads shader sources from the given file path and then compiles this shader.
     */
    public Shader sourceFromFile(String path) throws IOException
    {
        return sourceFromFile(Path.of(path));
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
