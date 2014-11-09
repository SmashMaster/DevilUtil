package com.samrj.devil.gl;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL40;

public final class Attribute
{
    public static String typeName(int type)
    {
        switch (type)
        {
            case GL11.GL_FLOAT:       return "GL_FLOAT";
            case GL20.GL_FLOAT_VEC2:  return "GL_FLOAT_VEC2";
            case GL20.GL_FLOAT_VEC3:  return "GL_FLOAT_VEC3";
            case GL20.GL_FLOAT_VEC4:  return "GL_FLOAT_VEC4";
            case GL11.GL_INT:         return "GL_INT";
            case GL20.GL_INT_VEC2:    return "GL_INT_VEC2";
            case GL20.GL_INT_VEC3:    return "GL_INT_VEC3";
            case GL20.GL_INT_VEC4:    return "GL_INT_VEC4";
            case GL11.GL_DOUBLE:      return "GL_DOUBLE";
            case GL40.GL_DOUBLE_VEC2: return "GL_DOUBLE_VEC2";
            case GL40.GL_DOUBLE_VEC3: return "GL_DOUBLE_VEC3";
            case GL40.GL_DOUBLE_VEC4: return "GL_DOUBLE_VEC4";
            default: throw new IllegalArgumentException("No such type: " + type);
        }
    }
    
    public static int typeSize(int type)
    {
        switch (type)
        {
            case GL11.GL_FLOAT:
            case GL11.GL_INT:
            case GL11.GL_DOUBLE: return 1;
            case GL20.GL_FLOAT_VEC2:
            case GL20.GL_INT_VEC2:
            case GL40.GL_DOUBLE_VEC2: return 2;
            case GL20.GL_FLOAT_VEC3:
            case GL20.GL_INT_VEC3:
            case GL40.GL_DOUBLE_VEC3: return 3;
            case GL20.GL_FLOAT_VEC4:
            case GL20.GL_INT_VEC4:
            case GL40.GL_DOUBLE_VEC4: return 4;
            default: throw new IllegalArgumentException("No such type: " + type);
        }
    }
    
    public static int typeBytesPerElem(int type)
    {
        switch (type)
        {
            case GL11.GL_FLOAT:
            case GL20.GL_FLOAT_VEC2:
            case GL20.GL_FLOAT_VEC3:
            case GL20.GL_FLOAT_VEC4:
            case GL11.GL_INT:
            case GL20.GL_INT_VEC2:
            case GL20.GL_INT_VEC3:
            case GL20.GL_INT_VEC4: return 4;
            case GL11.GL_DOUBLE:
            case GL40.GL_DOUBLE_VEC2:
            case GL40.GL_DOUBLE_VEC3:
            case GL40.GL_DOUBLE_VEC4: return 8;
            default: throw new IllegalArgumentException("No such type: " + type);
        }
    }
    
    public static int typeDataType(int type)
    {
        switch (type)
        {
            case GL11.GL_FLOAT:
            case GL20.GL_FLOAT_VEC2:
            case GL20.GL_FLOAT_VEC3:
            case GL20.GL_FLOAT_VEC4: return GL11.GL_FLOAT;
            case GL11.GL_INT:
            case GL20.GL_INT_VEC2:
            case GL20.GL_INT_VEC3:
            case GL20.GL_INT_VEC4: return GL11.GL_INT;
            case GL11.GL_DOUBLE:
            case GL40.GL_DOUBLE_VEC2:
            case GL40.GL_DOUBLE_VEC3:
            case GL40.GL_DOUBLE_VEC4: return GL11.GL_DOUBLE;
            default: throw new IllegalArgumentException("No such type: " + type);
        }
    }
    
    public final String name;
    private ByteDataStream bytes = null;
    private int index; //This attribute's index on the currently bound shader
    private int size; //Number of elements for this attribute's type
    private int type; //OpenGL vertex attribute type enum
    private int byteLength; //Total length of this attribute in bytes
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
                "No such attribute: '" + name + "'");
        
        size = GL20.glGetActiveAttribSize(shaderID, index);
        type = GL20.glGetActiveAttribType(shaderID, index);
        byteLength = size*typeBytesPerElem(type);
        bytes = new ByteDataStream(byteLength);
        
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
                "Attribute '" + name + "' is inactive.");
    }
    
    private void ensureType(int type)
    {
        if (this.type != type) throw new IllegalArgumentException(
                "Expected type " + typeName(this.type) + ", got " + typeName(type));
    }
    
    int getIndex()
    {
        ensureActive();
        return index;
    }
    
    int getSize()
    {
        ensureActive();
        return size;
    }
    
    int getType()
    {
        ensureActive();
        return type;
    }
    
    int getByteLength()
    {
        ensureActive();
        return byteLength;
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
        ensureType(GL11.GL_FLOAT);
        bytes.reset();
        bytes.writeFloat(x);
    }
    
    public void set(float x, float y)
    {
        ensureActive();
        ensureType(GL20.GL_FLOAT_VEC2);
        bytes.reset();
        bytes.writeFloat(x);
        bytes.writeFloat(y);
    }
    
    public void set(float x, float y, float z)
    {
        ensureActive();
        ensureType(GL20.GL_FLOAT_VEC3);
        bytes.reset();
        bytes.writeFloat(x);
        bytes.writeFloat(y);
        bytes.writeFloat(z);
    }
    
    public void set(float x, float y, float z, float w)
    {
        ensureActive();
        ensureType(GL20.GL_FLOAT_VEC4);
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
        ensureType(GL11.GL_INT);
        bytes.reset();
        bytes.writeInt(x);
    }
    
    public void set(int x, int y)
    {
        ensureActive();
        ensureType(GL20.GL_INT_VEC2);
        bytes.reset();
        bytes.writeInt(x);
        bytes.writeInt(y);
    }
    
    public void set(int x, int y, int z)
    {
        ensureActive();
        ensureType(GL20.GL_INT_VEC3);
        bytes.reset();
        bytes.writeInt(x);
        bytes.writeInt(y);
        bytes.writeInt(z);
    }
    
    public void set(int x, int y, int z, int w)
    {
        ensureActive();
        ensureType(GL20.GL_INT_VEC4);
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
        ensureType(GL11.GL_DOUBLE);
        bytes.reset();
        bytes.writeDouble(x);
    }
    
    public void set(double x, double y)
    {
        ensureActive();
        ensureType(GL40.GL_DOUBLE_VEC2);
        bytes.reset();
        bytes.writeDouble(x);
        bytes.writeDouble(y);
    }
    
    public void set(double x, double y, double z)
    {
        ensureActive();
        ensureType(GL40.GL_DOUBLE_VEC3);
        bytes.reset();
        bytes.writeDouble(x);
        bytes.writeDouble(y);
        bytes.writeDouble(z);
    }
    
    public void set(double x, double y, double z, double w)
    {
        ensureActive();
        ensureType(GL40.GL_DOUBLE_VEC4);
        bytes.reset();
        bytes.writeDouble(x);
        bytes.writeDouble(y);
        bytes.writeDouble(z);
        bytes.writeDouble(w);
    }
}
