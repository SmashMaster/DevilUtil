package com.samrj.devilgl;

import static com.samrj.devil.io.BufferUtil.memUtil;
import com.samrj.devil.io.Memory;
import com.samrj.devil.io.Memory.Block;
import com.samrj.devil.res.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilGL/blob/master/LICENSE
 */
public class Shader
{
    public final int id, type;
    
    public Shader(Memory mem, InputStream in, int type) throws IOException
    {
        id = GL20.glCreateShader(type);
        this.type = type;
        
        {
            //Source to memory
            int sourceLength = in.available();
            Block sourceBlock = mem.alloc(sourceLength);
            ByteBuffer sourceBuffer = sourceBlock.readUnsafe();
            for (int i=0; i<sourceLength; i++) sourceBuffer.put((byte)in.read());

            //Pointer to memory
            Block pointerBlock = mem.alloc(8);
            ByteBuffer pointerBuffer = pointerBlock.read();
            pointerBuffer.putLong(sourceBlock.address());
            pointerBuffer.reset();

            //Length to memory
            Block lengthBlock = mem.alloc(4);
            ByteBuffer lengthBuffer = lengthBlock.read();
            lengthBuffer.putInt(sourceLength);
            lengthBuffer.reset();

            //Load shader source
            GL20.glShaderSource(id, 1, pointerBuffer, lengthBuffer);
            
            //Free allocated memory
            lengthBlock.free();
            pointerBlock.free();
            sourceBlock.free();
        }
        
        GL20.glCompileShader(id);
        
        if (GL20.glGetShaderi(id, GL20.GL_COMPILE_STATUS) != GL11.GL_TRUE)
        {
            int logLength = GL20.glGetShaderi(id, GL20.GL_INFO_LOG_LENGTH);
            String log = GL20.glGetShaderInfoLog(id, logLength);
            throw new ShaderException(log);
        }
    }
    
    public Shader(InputStream in, int type) throws IOException
    {
        this(memUtil, in, type);
    }
    
    public Shader(Memory mem, String path, int type) throws IOException
    {
        this(mem, Resource.open(path), type);
    }
    
    public Shader(String path, int type) throws IOException
    {
        this(memUtil, path, type);
    }
}
