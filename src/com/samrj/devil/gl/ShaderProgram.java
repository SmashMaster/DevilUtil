package com.samrj.devil.gl;

import static com.samrj.devil.io.BufferUtil.memUtil;
import com.samrj.devil.io.Memory.Block;
import com.samrj.devil.util.QuickIdentitySet;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryUtil;

/**
 * OpenGL shader program wrapper.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilGL/blob/master/LICENSE
 */
public final class ShaderProgram
{
    public static enum State
    {
        NEW, LINKED, COMPLETE, DELETED;
    }
    
    /**
     * The OpenGL id of this shader program.
     */
    final int id;
    
    private final Set<Shader> shaders;
    private List<Attribute> attributes;
    private State state;
    
    ShaderProgram()
    {
        id = GL20.glCreateProgram();
        shaders = new QuickIdentitySet<>();
        state = State.NEW;
    }
    
    /**
     * Attaches the given shader to this program.
     * 
     * @param shader The shader to attach to this program.
     */
    public void attach(Shader shader)
    {
        if (state != State.NEW) throw new IllegalStateException(
                "Shader program must be new to attach shaders.");
        if (shader.state() != Shader.State.COMPILED) throw new IllegalStateException(
                "Cannot attach shader that is not compiled.");
        
        GL20.glAttachShader(id, shader.id);
        shaders.add(shader);
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
     * Links this shader program, creating executables that may run on the GPU
     * and compiling a list of input attributes.
     */
    public void link()
    {
        if (state != State.NEW) throw new IllegalStateException(
                "Shader program must be new to link.");
        
        GL20.glLinkProgram(id);
        checkStatus(GL20.GL_LINK_STATUS);
        
        int numAttributes = GL20.glGetProgrami(id, GL20.GL_ACTIVE_ATTRIBUTES);
        ArrayList<Attribute> attList = new ArrayList<>(numAttributes);
        for (int index=0; index<numAttributes; index++)
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

            attList.add(new Attribute(name, type, size, location));
        }
        attributes = Collections.unmodifiableList(attList);
        
        state = State.LINKED;
    }
    
    /**
     * Validates this program, checking to see whether the executables contained
     * in this program can be executed in the current OpenGL state.
     */
    public void validate()
    {
        if (state != State.LINKED) throw new IllegalStateException(
                "Shader program must be linked to validate.");
        
        GL20.glValidateProgram(id);
        checkStatus(GL20.GL_VALIDATE_STATUS);
        
        state = State.COMPLETE;
    }
    
    /**
     * Use this shader for any subsequent draw calls.
     */
    void use()
    {
        if (state != State.COMPLETE) throw new IllegalStateException(
                "Shader must be complete to use.");
        GL20.glUseProgram(id);
    }
    
    /**
     * 
     * @return A set of each shader attached to this program.
     */
    public Set<Shader> getShaders()
    {
        return Collections.unmodifiableSet(shaders);
    }
    
    /**
     * @return A list of every vertex attribute associated with this program.
     */
    public List<Attribute> getAttributes()
    {
        if (state != State.LINKED && state != State.COMPLETE)
            throw new IllegalStateException("Shader must be linked or complete to have attributes.");
        return attributes;
    }
    
    /**
     * @return The state of this shader program.
     */
    public State state()
    {
        return state;
    }
    
    void delete()
    {
        if (state == State.DELETED) return;
        
        if (attributes != null) attributes = null;
        GL20.glDeleteProgram(id);
        
        state = State.DELETED;
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
