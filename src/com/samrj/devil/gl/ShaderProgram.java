package com.samrj.devil.gl;

import com.samrj.devil.io.Memory.Block;
import static com.samrj.devil.io.Memory.memUtil;
import com.samrj.devil.math.Mat2;
import com.samrj.devil.math.Mat3;
import com.samrj.devil.math.Mat4;
import com.samrj.devil.math.Vec2;
import com.samrj.devil.math.Vec3;
import com.samrj.devil.math.Vec4;
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
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
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
     * Returns the location of the attribute with the given name, or -1 if none
     * with the given name exists.
     * 
     * @param name The name of the attribute to find.
     * @return The location of the attribute.
     */
    public int getAttributeLocation(String name)
    {
        return GL20.glGetAttribLocation(id, name);
    }
    
    /**
     * Returns the location of the uniform with the given name, or -1 if none
     * with the given name exists.
     * 
     * @param name The name of the uniform to find.
     * @return The location of a uniform.
     */
    public int getUniformLocation(String name)
    {
        return GL20.glGetUniformLocation(id, name);
    }
    
    /**
     * Specifies the value of a uniform variable for this program. Program must
     * be in use. Returns true if and only if the uniform exists and is active.
     * 
     * @param name The name of the uniform to specify.
     * @param x The value to set the uniform to.
     * @return Whether or not the uniform exists and is active.
     */
    public boolean uniform1i(String name, int x)
    {
        if (DGL.currentProgram() != this) throw new IllegalStateException("Program must be in use.");
        int loc = GL20.glGetUniformLocation(id, name);
        if (loc < 0) return false;
        GL20.glUniform1i(loc, x);
        return true;
    }
    
    /**
     * Specifies the value of a uniform variable for this program. Program must
     * be in use. Returns true if and only if the uniform exists and is active.
     * 
     * @param name The name of the uniform to specify.
     * @param x The value to set the uniform to.
     * @return Whether or not the uniform exists and is active.
     */
    public boolean uniform1f(String name, float x)
    {
        if (DGL.currentProgram() != this) throw new IllegalStateException("Program must be in use.");
        int loc = GL20.glGetUniformLocation(id, name);
        if (loc < 0) return false;
        GL20.glUniform1f(loc, x);
        return true;
    }
    
    /**
     * Specifies the value of a uniform variable for this program. Program must
     * be in use. Returns true if and only if the uniform exists and is active.
     * 
     * @param name The name of the uniform to specify.
     * @return Whether or not the uniform exists and is active.
     */
    public boolean uniform2f(String name, float x, float y)
    {
        if (DGL.currentProgram() != this) throw new IllegalStateException("Program must be in use.");
        int loc = GL20.glGetUniformLocation(id, name);
        if (loc < 0) return false;
        GL20.glUniform2f(loc, x, y);
        return true;
    }
    
    /**
     * Specifies the value of a uniform variable for this program. Program must
     * be in use. Returns true if and only if the uniform exists and is active.
     * 
     * @param name The name of the uniform to specify.
     * @return Whether or not the uniform exists and is active.
     */
    public boolean uniformVec2(String name, Vec2 v)
    {
        return uniform2f(name, v.x, v.y);
    }
    
    /**
     * Specifies the value of a uniform variable for this program. Program must
     * be in use. Returns true if and only if the uniform exists and is active.
     * 
     * @param name The name of the uniform to specify.
     * @return Whether or not the uniform exists and is active.
     */
    public boolean uniform3f(String name, float x, float y, float z)
    {
        if (DGL.currentProgram() != this) throw new IllegalStateException("Program must be in use.");
        int loc = GL20.glGetUniformLocation(id, name);
        if (loc < 0) return false;
        GL20.glUniform3f(loc, x, y, z);
        return true;
    }
    
    /**
     * Specifies the value of a uniform variable for this program. Program must
     * be in use. Returns true if and only if the uniform exists and is active.
     * 
     * @param name The name of the uniform to specify.
     * @return Whether or not the uniform exists and is active.
     */
    public boolean uniformVec3(String name, Vec3 v)
    {
        return uniform3f(name, v.x, v.y, v.z);
    }
    
    /**
     * Specifies the value of a uniform variable for this program. Program must
     * be in use. Returns true if and only if the uniform exists and is active.
     * 
     * @param name The name of the uniform to specify.
     * @return Whether or not the uniform exists and is active.
     */
    public boolean uniform4f(String name, float x, float y, float z, float w)
    {
        if (DGL.currentProgram() != this) throw new IllegalStateException("Program must be in use.");
        int loc = GL20.glGetUniformLocation(id, name);
        if (loc < 0) return false;
        GL20.glUniform4f(loc, x, y, z, w);
        return true;
    }
    
    /**
     * Specifies the value of a uniform variable for this program. Program must
     * be in use. Returns true if and only if the uniform exists and is active.
     * 
     * @param name The name of the uniform to specify.
     * @return Whether or not the uniform exists and is active.
     */
    public boolean uniformVec4(String name, Vec4 v)
    {
        return uniform4f(name, v.x, v.y, v.z, v.w);
    }
    
    /**
     * Specifies the value of a uniform variable for this program. Program must
     * be in use. Returns true if and only if the uniform exists and is active.
     * 
     * @param name The name of the uniform to specify.
     * @return Whether or not the uniform exists and is active.
     */
    public boolean uniformMat2(String name, Mat2 matrix)
    {
        if (DGL.currentProgram() != this) throw new IllegalStateException("Program must be in use.");
        int loc = GL20.glGetUniformLocation(id, name);
        if (loc < 0) return false;
        
        Block b = memUtil.alloc(matrix);
        GL20.glUniformMatrix2fv(loc, false, b.readUnsafe().asFloatBuffer());
        b.free();
        return true;
    }
    
    /**
     * Specifies the value of a uniform variable for this program. Program must
     * be in use. Returns true if and only if the uniform exists and is active.
     * 
     * @param name The name of the uniform to specify.
     * @return Whether or not the uniform exists and is active.
     */
    public boolean uniformMat3(String name, Mat3 matrix)
    {
        if (DGL.currentProgram() != this) throw new IllegalStateException("Program must be in use.");
        int loc = GL20.glGetUniformLocation(id, name);
        if (loc < 0) return false;
        
        Block b = memUtil.alloc(matrix);
        GL20.glUniformMatrix3fv(loc, false, b.readUnsafe().asFloatBuffer());
        b.free();
        return true;
    }
    
    /**
     * Specifies the value of a uniform variable for this program. Program must
     * be in use. Returns true if and only if the uniform exists and is active.
     * 
     * @param name The name of the uniform to specify.
     * @return Whether or not the uniform exists and is active.
     */
    public boolean uniformMat4(String name, Mat4 matrix)
    {
        if (DGL.currentProgram() != this) throw new IllegalStateException("Program must be in use.");
        int loc = GL20.glGetUniformLocation(id, name);
        if (loc < 0) return false;
        
        Block b = memUtil.alloc(matrix);
        GL20.glUniformMatrix4fv(loc, false, b.readUnsafe().asFloatBuffer());
        b.free();
        return true;
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
