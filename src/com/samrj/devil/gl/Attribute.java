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
            default: return "";
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
            default: return -1;
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
            default: return -1;
        }
    }
    
    private final ByteDataStream bytes;
    private final int index, type;
    boolean active = true;
    
    Attribute(int index, int size, int bytesPerElem, int type)
    {
        this.index = index;
        this.type = type;
        
        bytes = new ByteDataStream(size*bytesPerElem);
    }
    
    private void ensureActive()
    {
        if (!active) throw new IllegalStateException("This attribute is inactive.");
    }
    
    private void ensureType(int type)
    {
        ensureActive();
        if (this.type != type) throw new IllegalArgumentException(
                "Expected type " + typeName(this.type) + ", got " + typeName(type));
    }
    
    int getIndex()
    {
        ensureActive();
        return index;
    }
    
    //FLOATS
    
    public void set(float x)
    {
        ensureType(GL11.GL_FLOAT);
        bytes.reset();
        bytes.writeFloat(x);
    }
    
    public void set(float x, float y)
    {
        ensureType(GL20.GL_FLOAT_VEC2);
        bytes.reset();
        bytes.writeFloat(x);
        bytes.writeFloat(y);
    }
    
    public void set(float x, float y, float z)
    {
        ensureType(GL20.GL_FLOAT_VEC3);
        bytes.reset();
        bytes.writeFloat(x);
        bytes.writeFloat(y);
        bytes.writeFloat(z);
    }
    
    public void set(float x, float y, float z, float w)
    {
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
        ensureType(GL11.GL_INT);
        bytes.reset();
        bytes.writeInt(x);
    }
    
    public void set(int x, int y)
    {
        ensureType(GL20.GL_INT_VEC2);
        bytes.reset();
        bytes.writeInt(x);
        bytes.writeInt(y);
    }
    
    public void set(int x, int y, int z)
    {
        ensureType(GL20.GL_INT_VEC3);
        bytes.reset();
        bytes.writeInt(x);
        bytes.writeInt(y);
        bytes.writeInt(z);
    }
    
    public void set(int x, int y, int z, int w)
    {
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
        ensureType(GL11.GL_DOUBLE);
        bytes.reset();
        bytes.writeDouble(x);
    }
    
    public void set(double x, double y)
    {
        ensureType(GL40.GL_DOUBLE_VEC2);
        bytes.reset();
        bytes.writeDouble(x);
        bytes.writeDouble(y);
    }
    
    public void set(double x, double y, double z)
    {
        ensureType(GL40.GL_DOUBLE_VEC3);
        bytes.reset();
        bytes.writeDouble(x);
        bytes.writeDouble(y);
        bytes.writeDouble(z);
    }
    
    public void set(double x, double y, double z, double w)
    {
        ensureType(GL40.GL_DOUBLE_VEC4);
        bytes.reset();
        bytes.writeDouble(x);
        bytes.writeDouble(y);
        bytes.writeDouble(z);
        bytes.writeDouble(w);
    }
}
