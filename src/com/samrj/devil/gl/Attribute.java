package com.samrj.devil.gl;

import com.samrj.devil.gl.util.ByteDataStream;
import com.samrj.devil.gl.util.Primitive;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL40;

public final class Attribute
{
    public static enum Type
    {
        FLOAT      (GL11.GL_FLOAT,       1, Primitive.FLOAT),
        FLOAT_VEC2 (GL20.GL_FLOAT_VEC2,  2, Primitive.FLOAT),
        FLOAT_VEC3 (GL20.GL_FLOAT_VEC3,  3, Primitive.FLOAT),
        FLOAT_VEC4 (GL20.GL_FLOAT_VEC4,  4, Primitive.FLOAT),
        INT        (GL11.GL_INT,         1, Primitive.INT),
        INT_VEC2   (GL20.GL_INT_VEC2,    2, Primitive.INT),
        INT_VEC3   (GL20.GL_INT_VEC3,    3, Primitive.INT),
        INT_VEC4   (GL20.GL_INT_VEC4,    4, Primitive.INT),
        DOUBLE     (GL11.GL_DOUBLE,      1, Primitive.DOUBLE),
        DOUBLE_VEC2(GL40.GL_DOUBLE_VEC2, 2, Primitive.DOUBLE),
        DOUBLE_VEC3(GL40.GL_DOUBLE_VEC3, 3, Primitive.DOUBLE),
        DOUBLE_VEC4(GL40.GL_DOUBLE_VEC4, 4, Primitive.DOUBLE);
        
        public final int glEnum;
        public final int size;
        public final Primitive dataType;
        
        private Type(int glEnum, int size, Primitive dataType)
        {
            this.glEnum = glEnum;
            this.size = size;
            this.dataType = dataType;
        }
    }
    
    private static Type typeFromGLEnum(int glEnum)
    {
        switch (glEnum)
        {
            case GL11.GL_FLOAT:       return Type.FLOAT;
            case GL20.GL_FLOAT_VEC2:  return Type.FLOAT_VEC2;
            case GL20.GL_FLOAT_VEC3:  return Type.FLOAT_VEC3;
            case GL20.GL_FLOAT_VEC4:  return Type.FLOAT_VEC4;
            case GL11.GL_INT:         return Type.INT;
            case GL20.GL_INT_VEC2:    return Type.INT_VEC2;
            case GL20.GL_INT_VEC3:    return Type.INT_VEC3;
            case GL20.GL_INT_VEC4:    return Type.INT_VEC4;
            case GL11.GL_DOUBLE:      return Type.DOUBLE;
            case GL40.GL_DOUBLE_VEC2: return Type.DOUBLE_VEC2;
            case GL40.GL_DOUBLE_VEC3: return Type.DOUBLE_VEC3;
            case GL40.GL_DOUBLE_VEC4: return Type.DOUBLE_VEC4;
            default: throw new IllegalArgumentException(
                    "Illegal OpenGL attribute type enum: " + glEnum);
        }
    }
    
    public final String name;
    private ByteDataStream bytes = null;
    private int index; //This attribute's index on the currently bound shader
    private Type type; //OpenGL vertex attribute type enum
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
        
        type = typeFromGLEnum(GL20.glGetActiveAttribType(shaderID, index));
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
    
    private void ensureType(Type type)
    {
        if (this.type != type) throw new IllegalArgumentException(
                "Expected type " + this.type + ", got " + type + "!");
    }
    
    int getIndex()
    {
        ensureActive();
        return index;
    }
    
    Type getType()
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
        ensureType(Type.FLOAT);
        bytes.reset();
        bytes.writeFloat(x);
    }
    
    public void set(float x, float y)
    {
        ensureActive();
        ensureType(Type.FLOAT_VEC2);
        bytes.reset();
        bytes.writeFloat(x);
        bytes.writeFloat(y);
    }
    
    public void set(float x, float y, float z)
    {
        ensureActive();
        ensureType(Type.FLOAT_VEC3);
        bytes.reset();
        bytes.writeFloat(x);
        bytes.writeFloat(y);
        bytes.writeFloat(z);
    }
    
    public void set(float x, float y, float z, float w)
    {
        ensureActive();
        ensureType(Type.FLOAT_VEC4);
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
        ensureType(Type.INT);
        bytes.reset();
        bytes.writeInt(x);
    }
    
    public void set(int x, int y)
    {
        ensureActive();
        ensureType(Type.INT_VEC2);
        bytes.reset();
        bytes.writeInt(x);
        bytes.writeInt(y);
    }
    
    public void set(int x, int y, int z)
    {
        ensureActive();
        ensureType(Type.INT_VEC3);
        bytes.reset();
        bytes.writeInt(x);
        bytes.writeInt(y);
        bytes.writeInt(z);
    }
    
    public void set(int x, int y, int z, int w)
    {
        ensureActive();
        ensureType(Type.INT_VEC4);
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
        ensureType(Type.DOUBLE);
        bytes.reset();
        bytes.writeDouble(x);
    }
    
    public void set(double x, double y)
    {
        ensureActive();
        ensureType(Type.DOUBLE_VEC2);
        bytes.reset();
        bytes.writeDouble(x);
        bytes.writeDouble(y);
    }
    
    public void set(double x, double y, double z)
    {
        ensureActive();
        ensureType(Type.DOUBLE_VEC3);
        bytes.reset();
        bytes.writeDouble(x);
        bytes.writeDouble(y);
        bytes.writeDouble(z);
    }
    
    public void set(double x, double y, double z, double w)
    {
        ensureActive();
        ensureType(Type.DOUBLE_VEC4);
        bytes.reset();
        bytes.writeDouble(x);
        bytes.writeDouble(y);
        bytes.writeDouble(z);
        bytes.writeDouble(w);
    }
}
