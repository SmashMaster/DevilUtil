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
    private boolean active = false;
    
    Attribute(String name)
    {
        if (name == null) throw new NullPointerException();
        this.name = name;
    }
    
    /**
     * Can already be enabled, might be switching to another shader.
     */
    void enable(ShaderProgram shader)
    {
        int shaderID = shader.getID();
        
        index = GL20.glGetAttribLocation(shaderID, name);
        if (index == -1) throw new IllegalArgumentException(
                "No such attribute: '" + name + "'!");
        
        type = VarType.fromGLEnum(GL20.glGetActiveAttribType(shaderID, index));
        bytes = new ByteDataStream(type.size*type.dataType.size);
        active = true;
    }
    
    void disable()
    {
        if (!active) return;
        bytes = null;
        active = false;
    }
    
    private void ensureActive()
    {
        if (!active) throw new IllegalStateException(
                "Attribute '" + name + "' is inactive!");
    }
    
    private void ensureType(VarType type)
    {
        if (this.type != type) throw new IllegalArgumentException(
                "Expected type " + this.type + ", got " + type + "!");
    }
    
    int getIndex()
    {
        ensureActive();
        return index;
    }
    
    VarType getType()
    {
        ensureActive();
        return type;
    }
    
    int getByteLength()
    {
        ensureActive();
        return type.size*type.dataType.size;
    }
    
    void writeTo(ByteDataStream byteStream)
    {
        ensureActive();
        bytes.writeTo(byteStream);
    }
    
    //FLOATS
    
    public void set(float x)
    {
        ensureActive();
        ensureType(VarType.FLOAT);
        bytes.reset();
        bytes.writeFloat(x);
    }
    
    public void set(float x, float y)
    {
        ensureActive();
        ensureType(VarType.FLOAT_VEC2);
        bytes.reset();
        bytes.writeFloat(x);
        bytes.writeFloat(y);
    }
    
    public void set(float x, float y, float z)
    {
        ensureActive();
        ensureType(VarType.FLOAT_VEC3);
        bytes.reset();
        bytes.writeFloat(x);
        bytes.writeFloat(y);
        bytes.writeFloat(z);
    }
    
    public void set(float x, float y, float z, float w)
    {
        ensureActive();
        ensureType(VarType.FLOAT_VEC4);
        bytes.reset();
        bytes.writeFloat(x);
        bytes.writeFloat(y);
        bytes.writeFloat(z);
        bytes.writeFloat(w);
    }
    
    //INTS
    
    public void set(int x)
    {
        ensureActive();
        ensureType(VarType.INT);
        bytes.reset();
        bytes.writeInt(x);
    }
    
    public void set(int x, int y)
    {
        ensureActive();
        ensureType(VarType.INT_VEC2);
        bytes.reset();
        bytes.writeInt(x);
        bytes.writeInt(y);
    }
    
    public void set(int x, int y, int z)
    {
        ensureActive();
        ensureType(VarType.INT_VEC3);
        bytes.reset();
        bytes.writeInt(x);
        bytes.writeInt(y);
        bytes.writeInt(z);
    }
    
    public void set(int x, int y, int z, int w)
    {
        ensureActive();
        ensureType(VarType.INT_VEC4);
        bytes.reset();
        bytes.writeInt(x);
        bytes.writeInt(y);
        bytes.writeInt(z);
        bytes.writeInt(w);
    }
    
    //DOUBLES
    
    public void set(double x)
    {
        ensureActive();
        ensureType(VarType.DOUBLE);
        bytes.reset();
        bytes.writeDouble(x);
    }
    
    public void set(double x, double y)
    {
        ensureActive();
        ensureType(VarType.DOUBLE_VEC2);
        bytes.reset();
        bytes.writeDouble(x);
        bytes.writeDouble(y);
    }
    
    public void set(double x, double y, double z)
    {
        ensureActive();
        ensureType(VarType.DOUBLE_VEC3);
        bytes.reset();
        bytes.writeDouble(x);
        bytes.writeDouble(y);
        bytes.writeDouble(z);
    }
    
    public void set(double x, double y, double z, double w)
    {
        ensureActive();
        ensureType(VarType.DOUBLE_VEC4);
        bytes.reset();
        bytes.writeDouble(x);
        bytes.writeDouble(y);
        bytes.writeDouble(z);
        bytes.writeDouble(w);
    }
    
    //TODO: Matrices
}
