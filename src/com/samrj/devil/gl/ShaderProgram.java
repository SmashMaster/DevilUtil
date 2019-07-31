/*
 * Copyright (c) 2019 Sam Johnson
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

import com.samrj.devil.math.*;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.*;
import org.lwjgl.system.MemoryStack;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL20C.*;
import static org.lwjgl.opengl.GL30C.*;
import static org.lwjgl.system.MemoryUtil.*;

/**
 * OpenGL shader program wrapper.
 * 
 * @author Samuel Johnson (SmashMaster)
 */
public final class ShaderProgram extends DGLObj
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
    private Map<String, Attribute> attMap;
    private State state;
    
    ShaderProgram()
    {
        DGL.checkState();
        if (!DGL.getCapabilities().OpenGL20)
            throw new UnsupportedOperationException("Shader programs unsupported in OpenGL < 2.0");
        id = glCreateProgram();
        shaders = Collections.newSetFromMap(new IdentityHashMap<>());
        state = State.NEW;
    }
    
    /**
     * Attaches the given shader to this program.
     * 
     * @param shader The shader to attach to this program.
     * @return This shader program.
     */
    public ShaderProgram attach(Shader shader)
    {
        if (state != State.NEW) throw new IllegalStateException(
                "Shader program must be new to attach shaders.");
        if (shader.state() != Shader.State.COMPILED) throw new IllegalStateException(
                "Cannot attach shader that is not compiled.");
        
        glAttachShader(id, shader.id);
        shaders.add(shader);
        return this;
    }
    
    /**
     * Detaches the given shader from this program. Can be safely done at any
     * point after linking.
     * 
     * @param shader The shader to detach.
     * @return This shader program.
     */
    public ShaderProgram detach(Shader shader)
    {
        glDetachShader(id, shader.id);
        shaders.remove(shader);
        return this;
    }
    
    /**
     * Attaches each of the given shaders to this program.
     * 
     * @param shaders An array of shaders to attach to this program.
     * @return This shader program.
     */
    public ShaderProgram attach(Shader... shaders)
    {
        for (Shader shader : shaders) attach(shader);
        return this;
    }
    
    /**
     * Detaches all shaders from this program. Should be called after linking.
     * 
     * @return This shader program.
     */
    public ShaderProgram detachAll()
    {
        for (Shader shader : shaders) glDetachShader(id, shader.id);
        shaders.clear();
        return this;
    }
    
    private void checkStatus(int type)
    {
        if (glGetProgrami(id, type) != GL_TRUE)
        {
            int logLength = glGetProgrami(id, GL_INFO_LOG_LENGTH);
            String log = glGetProgramInfoLog(id, logLength);
            throw new ShaderException(log);
        }
    }
    
    /**
     * Links this shader program, creating executables that may run on the GPU
     * and compiling a list of input attributes.
     * 
     * @return This shader program.
     */
    public ShaderProgram link()
    {
        if (state != State.NEW) throw new IllegalStateException(
                "Shader program must be new to link.");
        
        glLinkProgram(id);
        checkStatus(GL_LINK_STATUS);
        
        int numAttributes = glGetProgrami(id, GL_ACTIVE_ATTRIBUTES);
        ArrayList<Attribute> attList = new ArrayList<>(numAttributes);
        attMap = new HashMap<>(numAttributes);
        
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            int attBytes = 4 + 4 + 4 + 32;
            long address = stack.nmalloc(attBytes);
            ByteBuffer buffer = memByteBuffer(address, attBytes);
            for (int index=0; index<numAttributes; index++)
            {
                long nameAddress = address + 12;
                nglGetActiveAttrib(id, index, 31, address, address + 4, address + 8, nameAddress);

                buffer.rewind();
                buffer.getInt();
                int size = buffer.getInt();
                int type = buffer.getInt();
                String name = memASCII(nameAddress);
                int location = nglGetAttribLocation(id, nameAddress);

                Attribute att = new Attribute(name, type, size, location);
                attList.add(att);
                attMap.put(name, att);
            }
        }
        
        attributes = Collections.unmodifiableList(attList);
        
        state = State.LINKED;
        return this;
    }
    
    /**
     * Validates this program, checking to see whether the executables contained
     * in this program can be executed in the current OpenGL state.
     * 
     * @return This shader program.
     */
    public ShaderProgram validate()
    {
        if (state != State.LINKED) throw new IllegalStateException(
                "Shader program must be linked to validate.");
        
        glValidateProgram(id);
        checkStatus(GL_VALIDATE_STATUS);
        
        state = State.COMPLETE;
        return this;
    }
    
    /**
     * Use this shader for any subsequent draw calls.
     */
    void use()
    {
        if (state == State.DELETED) throw new IllegalStateException(
                "Shader must not be deleted to use.");
        glUseProgram(id);
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
        return glGetAttribLocation(id, name);
    }
    
    // <editor-fold defaultstate="collapsed" desc="Uniform methods">
    /**
     * Returns the location of the uniform with the given name, or -1 if none
     * with the given name exists.
     * 
     * @param name The name of the uniform to find.
     * @return The location of a uniform.
     */
    public int getUniformLocation(String name)
    {
        if (DGL.currentProgram() != this) throw new IllegalStateException("Program must be in use.");
        return glGetUniformLocation(id, name);
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
        int loc = glGetUniformLocation(id, name);
        if (loc < 0) return false;
        glUniform1i(loc, x);
        return true;
    }
    
    /**
     * Specifies the values of a uniform variable array for this program. Must
     * be in use. Returns true if and only if the uniform exists and is active.
     * 
     * @param name The name of the uniform array to specify.
     * @param array An array of values to set the uniform to.
     * @return Whether or not the uniform exists and is active.
     */
    public boolean uniform1iv(String name, int... array)
    {
        if (DGL.currentProgram() != this) throw new IllegalStateException("Program must be in use.");
        int loc = glGetUniformLocation(id, name);
        if (loc < 0) return false;
        
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            glUniform1iv(loc, stack.ints(array));
            return true;
        }
    }
    
    public boolean uniform1b(String name, boolean b)
    {
        return uniform1i(name, b ? 1 : 0);
    }
    
    public boolean uniform1bv(String name, boolean... array)
    {
        int[] ints = new int[array.length];
        for (int i=0; i<array.length; i++) ints[i] = array[i] ? 1 : 0;
        return uniform1iv(name, ints);
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
        int loc = glGetUniformLocation(id, name);
        if (loc < 0) return false;
        glUniform1f(loc, x);
        return true;
    }
    
    /**
     * Specifies the values of a uniform variable array for this program. Must
     * be in use. Returns true if and only if the uniform exists and is active.
     * 
     * @param name The name of the uniform array to specify.
     * @param array An array of values to set the uniform to.
     * @return Whether or not the uniform exists and is active.
     */
    public boolean uniform1fv(String name, float... array)
    {
        if (DGL.currentProgram() != this) throw new IllegalStateException("Program must be in use.");
        int loc = glGetUniformLocation(id, name);
        if (loc < 0) return false;
        
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            glUniform1fv(loc, stack.floats(array));
            return true;
        }
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
        int loc = glGetUniformLocation(id, name);
        if (loc < 0) return false;
        glUniform2f(loc, x, y);
        return true;
    }
    
    /**
     * Specifies the values of a uniform variable array for this program. Must
     * be in use. Returns true if and only if the uniform exists and is active.
     * 
     * @param name The name of the uniform array to specify.
     * @param array An array of values to set the uniform to.
     * @return Whether or not the uniform exists and is active.
     */
    public boolean uniform2fv(String name, float... array)
    {
        if (DGL.currentProgram() != this) throw new IllegalStateException("Program must be in use.");
        int loc = glGetUniformLocation(id, name);
        if (loc < 0) return false;
        
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            glUniform2fv(loc, stack.floats(array));
            return true;
        }
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
     * Specifies the values of a uniform variable array for this program. Must
     * be in use. Returns true if and only if the uniform exists and is active.
     * 
     * @param name The name of the uniform array to specify.
     * @param array An array of values to set the uniform to.
     * @return Whether or not the uniform exists and is active.
     */
    public boolean uniformVec2v(String name, Vec2... array)
    {
        if (DGL.currentProgram() != this) throw new IllegalStateException("Program must be in use.");
        int loc = glGetUniformLocation(id, name);
        if (loc < 0) return false;
        
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            FloatBuffer buffer = stack.mallocFloat(array.length*2);
            for (Vec2 v : array) v.write(buffer);
            buffer.flip();
            glUniform2fv(loc, buffer);
            return true;
        }
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
        int loc = glGetUniformLocation(id, name);
        if (loc < 0) return false;
        glUniform3f(loc, x, y, z);
        return true;
    }
    
    /**
     * Specifies the values of a uniform variable array for this program. Must
     * be in use. Returns true if and only if the uniform exists and is active.
     * 
     * @param name The name of the uniform array to specify.
     * @param array An array of values to set the uniform to.
     * @return Whether or not the uniform exists and is active.
     */
    public boolean uniform3fv(String name, float... array)
    {
        if (DGL.currentProgram() != this) throw new IllegalStateException("Program must be in use.");
        int loc = glGetUniformLocation(id, name);
        if (loc < 0) return false;
        
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            glUniform3fv(loc, stack.floats(array));
            return true;
        }
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
     * Specifies the values of a uniform variable array for this program. Must
     * be in use. Returns true if and only if the uniform exists and is active.
     * 
     * @param name The name of the uniform array to specify.
     * @param array An array of values to set the uniform to.
     * @return Whether or not the uniform exists and is active.
     */
    public boolean uniformVec3v(String name, Vec3... array)
    {
        if (DGL.currentProgram() != this) throw new IllegalStateException("Program must be in use.");
        int loc = glGetUniformLocation(id, name);
        if (loc < 0) return false;
        
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            FloatBuffer buffer = stack.mallocFloat(array.length*3);
            for (Vec3 v : array) v.write(buffer);
            buffer.flip();
            glUniform3fv(loc, buffer);
            return true;
        }
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
        int loc = glGetUniformLocation(id, name);
        if (loc < 0) return false;
        glUniform4f(loc, x, y, z, w);
        return true;
    }
    
    /**
     * Specifies the values of a uniform variable array for this program. Must
     * be in use. Returns true if and only if the uniform exists and is active.
     * 
     * @param name The name of the uniform array to specify.
     * @param array An array of values to set the uniform to.
     * @return Whether or not the uniform exists and is active.
     */
    public boolean uniform4fv(String name, float... array)
    {
        if (DGL.currentProgram() != this) throw new IllegalStateException("Program must be in use.");
        int loc = glGetUniformLocation(id, name);
        if (loc < 0) return false;
        
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            glUniform4fv(loc, stack.floats(array));
            return true;
        }
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
     * Specifies the values of a uniform variable array for this program. Must
     * be in use. Returns true if and only if the uniform exists and is active.
     * 
     * @param name The name of the uniform array to specify.
     * @param array An array of values to set the uniform to.
     * @return Whether or not the uniform exists and is active.
     */
    public boolean uniformVec4v(String name, Vec4... array)
    {
        if (DGL.currentProgram() != this) throw new IllegalStateException("Program must be in use.");
        int loc = glGetUniformLocation(id, name);
        if (loc < 0) return false;
        
        
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            FloatBuffer buffer = stack.mallocFloat(array.length*4);
            for (Vec4 v : array) v.write(buffer);
            buffer.flip();
            glUniform4fv(loc, buffer);
            return true;
        }
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
        int loc = glGetUniformLocation(id, name);
        if (loc < 0) return false;
        
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            glUniformMatrix2fv(loc, false, matrix.mallocFloat(stack));
            return true;
        }
    }
    
    /**
     * Specifies the values of a uniform variable array for this program. Must
     * be in use. Returns true if and only if the uniform exists and is active.
     * 
     * @param name The name of the uniform array to specify.
     * @param array An array of values to set the uniform to.
     * @return Whether or not the uniform exists and is active.
     */
    public boolean uniformMat2v(String name, Mat2... array)
    {
        if (DGL.currentProgram() != this) throw new IllegalStateException("Program must be in use.");
        int loc = glGetUniformLocation(id, name);
        if (loc < 0) return false;
        
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            FloatBuffer buffer = stack.mallocFloat(array.length*4);
            for (Mat2 m : array) m.write(buffer);
            buffer.flip();
            glUniformMatrix2fv(loc, false, buffer);
            return true;
        }
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
        int loc = glGetUniformLocation(id, name);
        if (loc < 0) return false;
        
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            glUniformMatrix3fv(loc, false, matrix.mallocFloat(stack));
            return true;
        }
    }
    
    /**
     * Specifies the values of a uniform variable array for this program. Must
     * be in use. Returns true if and only if the uniform exists and is active.
     * 
     * @param name The name of the uniform array to specify.
     * @param array An array of values to set the uniform to.
     * @return Whether or not the uniform exists and is active.
     */
    public boolean uniformMat3v(String name, Mat3... array)
    {
        if (DGL.currentProgram() != this) throw new IllegalStateException("Program must be in use.");
        int loc = glGetUniformLocation(id, name);
        if (loc < 0) return false;
        
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            FloatBuffer buffer = stack.mallocFloat(array.length*9);
            for (Mat3 m : array) m.write(buffer);
            buffer.flip();
            glUniformMatrix3fv(loc, false, buffer);
            return true;
        }
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
        int loc = glGetUniformLocation(id, name);
        if (loc < 0) return false;
        
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            glUniformMatrix4fv(loc, false, matrix.mallocFloat(stack));
            return true;
        }
    }
    
    /**
     * Specifies the values of a uniform variable array for this program. Must
     * be in use. Returns true if and only if the uniform exists and is active.
     * 
     * @param name The name of the uniform array to specify.
     * @param array An array of values to set the uniform to.
     * @return Whether or not the uniform exists and is active.
     */
    public boolean uniformMat4v(String name, Mat4... array)
    {
        if (DGL.currentProgram() != this) throw new IllegalStateException("Program must be in use.");
        int loc = glGetUniformLocation(id, name);
        if (loc < 0) return false;
        
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            FloatBuffer buffer = stack.mallocFloat(array.length*16);
            for (Mat4 m : array) m.write(buffer);
            buffer.flip();
            glUniformMatrix4fv(loc, false, buffer);
            return true;
        }
    }
    // </editor-fold>
    
    /**
     * Binds the given color output name to the given color attachment.
     * 
     * @param name The output variable name to bind.
     * @param colorNumber The color attachment layer to bind to.
     */
    public void bindFragDataLocation(String name, int colorNumber)
    {
        if (DGL.currentProgram() != this) throw new IllegalStateException("Program must be in use.");
        glBindFragDataLocation(id, colorNumber, name);
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
     * Returns the attribute with the given name, or null if no such attribute
     * is active.
     * 
     * @param name The name of the attribute to find.
     * @return The attribute with the given name.
     */
    public Attribute getAttribute(String name)
    {
        return attMap.get(name);
    }
    
    /**
     * @return The state of this shader program.
     */
    public State state()
    {
        return state;
    }
    
    @Override
    void delete()
    {
        if (state == State.DELETED) return;
        
        if (DGL.currentProgram() == this) DGL.useProgram(null);
        attributes = null;
        attMap = null;
        glDeleteProgram(id);
        
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
