package com.samrj.devil.gl;

import com.samrj.devil.gl.util.ByteDataStream;
import org.lwjgl.opengl.GL20;

/**
 * @author SmashMaster
 */
public final class Attribute
{
    public final String name;
    private ByteDataStream bytes = null;
    private int index; //This attribute's index on the currently bound shader
    private VarType type; //OpenGL vertex attribute type enum
    private boolean enabled = false;
    
    Attribute(String name)
    {
        if (name == null) throw new NullPointerException();
        this.name = name;
    }
    
    /**
     * Enable this attribute on the given shader. Can fail silently.
     * 
     * @param shader the shader to enable this attribute on.
     * @return whether or not the attribute was successfully enabled.
     */
    boolean softEnable(ShaderProgram shader)
    {
        int shaderID = shader.getID();
        
        index = GL20.glGetAttribLocation(shaderID, name);
        if (index == -1)
        {
            disable();
            return false;
        }
        
        type = VarType.fromGLEnum(GL20.glGetActiveAttribType(shaderID, index));
        bytes = new ByteDataStream(type.size*type.dataType.size);
        enabled = true;
        return true;
    }
    
    /**
     * Enable this attribute on the given shader, and throw an exception if no
     * such variable exists.
     * 
     * @param shader the shader to enable this attribute on.
     */
    void enable(ShaderProgram shader)
    {
        if (!softEnable(shader)) throw new IllegalArgumentException(
                "No such attribute: '" + name + "'!");
    }
    
    void disable()
    {
        type = null;
        bytes = null;
        enabled = false;
    }
    
    boolean isEnabled()
    {
        return enabled;
    }
    
    private void ensureEnabled()
    {
        if (!enabled) throw new IllegalStateException(
                "Attribute '" + name + "' is inactive!");
    }
    
    private void ensureType(VarType type)
    {
        if (this.type != type) throw new IllegalArgumentException(
                "Expected type " + this.type + ", got " + type + "!");
    }
    
    int getIndex()
    {
        ensureEnabled();
        return index;
    }
    
    VarType getType()
    {
        ensureEnabled();
        return type;
    }
    
    int getByteLength()
    {
        ensureEnabled();
        return type.size*type.dataType.size;
    }
    
    void writeTo(ByteDataStream byteStream)
    {
        ensureEnabled();
        bytes.writeTo(byteStream);
    }
    
    //FLOATS
    
    public void set(float x)
    {
        ensureEnabled();
        ensureType(VarType.FLOAT);
        bytes.reset();
        bytes.writeFloat(x);
    }
    
    public void set(float x, float y)
    {
        ensureEnabled();
        ensureType(VarType.FLOAT_VEC2);
        bytes.reset();
        bytes.writeFloat(x);
        bytes.writeFloat(y);
    }
    
    public void set(float x, float y, float z)
    {
        ensureEnabled();
        ensureType(VarType.FLOAT_VEC3);
        bytes.reset();
        bytes.writeFloat(x);
        bytes.writeFloat(y);
        bytes.writeFloat(z);
    }
    
    public void set(float x, float y, float z, float w)
    {
        ensureEnabled();
        ensureType(VarType.FLOAT_VEC4);
        bytes.reset();
        bytes.writeFloat(x);
        bytes.writeFloat(y);
        bytes.writeFloat(z);
        bytes.writeFloat(w);
    }
    
    //DOUBLES
    
    public void set(double x)
    {
        ensureEnabled();
        ensureType(VarType.DOUBLE);
        bytes.reset();
        bytes.writeDouble(x);
    }
    
    public void set(double x, double y)
    {
        ensureEnabled();
        ensureType(VarType.DOUBLE_VEC2);
        bytes.reset();
        bytes.writeDouble(x);
        bytes.writeDouble(y);
    }
    
    public void set(double x, double y, double z)
    {
        ensureEnabled();
        ensureType(VarType.DOUBLE_VEC3);
        bytes.reset();
        bytes.writeDouble(x);
        bytes.writeDouble(y);
        bytes.writeDouble(z);
    }
    
    public void set(double x, double y, double z, double w)
    {
        ensureEnabled();
        ensureType(VarType.DOUBLE_VEC4);
        bytes.reset();
        bytes.writeDouble(x);
        bytes.writeDouble(y);
        bytes.writeDouble(z);
        bytes.writeDouble(w);
    }
    
    //INTS
    
    public void set(int x)
    {
        ensureEnabled();
        ensureType(VarType.INT);
        bytes.reset();
        bytes.writeInt(x);
    }
    
    public void set(int x, int y)
    {
        ensureEnabled();
        ensureType(VarType.INT_VEC2);
        bytes.reset();
        bytes.writeInt(x);
        bytes.writeInt(y);
    }
    
    public void set(int x, int y, int z)
    {
        ensureEnabled();
        ensureType(VarType.INT_VEC3);
        bytes.reset();
        bytes.writeInt(x);
        bytes.writeInt(y);
        bytes.writeInt(z);
    }
    
    public void set(int x, int y, int z, int w)
    {
        ensureEnabled();
        ensureType(VarType.INT_VEC4);
        bytes.reset();
        bytes.writeInt(x);
        bytes.writeInt(y);
        bytes.writeInt(z);
        bytes.writeInt(w);
    }
    
    //BOOLEAN
    
    public void set(boolean x)
    {
        ensureEnabled();
        ensureType(VarType.INT);
        bytes.reset();
        bytes.writeBoolean(x);
    }
    
    public void set(boolean x, boolean y)
    {
        ensureEnabled();
        ensureType(VarType.INT_VEC2);
        bytes.reset();
        bytes.writeBoolean(x);
        bytes.writeBoolean(y);
    }
    
    public void set(boolean x, boolean y, boolean z)
    {
        ensureEnabled();
        ensureType(VarType.INT_VEC3);
        bytes.reset();
        bytes.writeBoolean(x);
        bytes.writeBoolean(y);
        bytes.writeBoolean(z);
    }
    
    public void set(boolean x, boolean y, boolean z, boolean w)
    {
        ensureEnabled();
        ensureType(VarType.INT_VEC4);
        bytes.reset();
        bytes.writeBoolean(x);
        bytes.writeBoolean(y);
        bytes.writeBoolean(z);
        bytes.writeBoolean(w);
    }
}
