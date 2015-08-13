package com.samrj.devilgl;

import static com.samrj.devil.io.BufferUtil.memUtil;
import com.samrj.devil.io.Memory;
import com.samrj.devil.io.Memory.Block;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryUtil;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilGL/blob/master/LICENSE
 */
public class ShaderProgram
{
    public static final ShaderProgram load(Memory mem, String path) throws IOException
    {
        Shader vertShader = new Shader(mem, path + ".vert", GL20.GL_VERTEX_SHADER);
        Shader fragShader = new Shader(mem, path + ".frag", GL20.GL_FRAGMENT_SHADER);
        
        ShaderProgram shader = new ShaderProgram();
        shader.attach(vertShader);
        shader.attach(fragShader);
        shader.link();
        shader.validate();
        return shader;
    }
    
    public static final ShaderProgram load(String path) throws IOException
    {
        return load(memUtil, path);
    }
    
    /**
     * The OpenGL id of this shader program.
     */
    public final int id;
    
    private List<Attribute> attributes;
    
    /**
     * Creates a new shader program.
     */
    public ShaderProgram()
    {
        id = GL20.glCreateProgram();
    }
    
    private void checkStatus(int type)
    {
        if (GL20.glGetProgrami(id, type) != GL11.GL_TRUE)
        {
            int logLength = GL20.glGetProgrami(id, GL20.GL_INFO_LOG_LENGTH);
            String log = GL20.glGetProgramInfoLog(id, logLength);
            throw new ShaderException(log);
        }
    }
    
    /**
     * Attaches the given shader to this program.
     * 
     * @param shader The shader to attach to this program.
     */
    public void attach(Shader shader)
    {
        GL20.glAttachShader(id, shader.id);
    }
    
    private Attribute getActiveAttribute(int index)
    {
        Block block = memUtil.alloc(4 + 4 + 4 + 32);
        long ptr = block.address();
        long namePtr = ptr + 12;
        GL20.nglGetActiveAttrib(id, index, 31, ptr, ptr + 4, ptr + 8, namePtr);
        
        ByteBuffer buffer = block.readUnsafe();
        buffer.position(buffer.position() + 4);
        int size = buffer.getInt();
        int type = buffer.getInt();
        String name = MemoryUtil.memDecodeASCII(namePtr);
        int location = GL20.nglGetAttribLocation(id, namePtr);
        
        block.free();
        
        return new Attribute(name, type, size, location);
    }
    
    /**
     * Links this shader program, creating executables that may run on the GPU.
     */
    public void link()
    {
        GL20.glLinkProgram(id);
        checkStatus(GL20.GL_LINK_STATUS);
        
        int numAttributes = GL20.glGetProgrami(id, GL20.GL_ACTIVE_ATTRIBUTES);
        ArrayList<Attribute> attList = new ArrayList<>(numAttributes);
        for (int i=0; i<numAttributes; i++) attList.add(getActiveAttribute(i));
        attributes = Collections.unmodifiableList(attList);
    }
    
    /**
     * Validates this program, checking to see whether the executables contained
     * in this program can be executed in the current OpenGL state.
     */
    public void validate()
    {
        GL20.glValidateProgram(id);
        checkStatus(GL20.GL_VALIDATE_STATUS);
    }
    
    /**
     * Use this shader for any subsequent draw calls.
     */
    public void use()
    {
        GL20.glUseProgram(id);
    }
    
    /**
     * @return An array of every vertex attribute associated with this program.
     */
    public List<Attribute> getAttributes()
    {
        return attributes;
    }
    
    /**
     * Vertex attribute class for this specific shader program.
     */
    public class Attribute
    {
        /**
         * The name of this attribute, as it appears in the vertex shader.
         */
        public final String name;
        
        /**
         * The type of this attribute.
         */
        public final AttributeType type;
        
        /**
         * The number of elements in this attribute array, if this attribute is
         * an array type.
         */
        public final int size;
        
        /**
         * The location of this attribute.
         */
        public final int location;

        private Attribute(String name, int type, int size, int location)
        {
            this.name = name;
            this.type = AttributeType.get(type);
            if (this.type == null) throw new IllegalArgumentException();
            this.size = size;
            this.location = location;
        }

        @Override
        public String toString()
        {
            return "Att \"" + name + "\", type: " + type + ", size: " + size + ", loc: " + location;
        }
    }
}
