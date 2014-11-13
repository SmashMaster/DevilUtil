package com.samrj.devil.gl.util;

import org.lwjgl.opengl.GL11;

public enum Primitive
{
    BYTE  (GL11.GL_UNSIGNED_BYTE,  Byte.BYTES),
    SHORT (GL11.GL_UNSIGNED_SHORT, Short.BYTES),
    INT   (GL11.GL_UNSIGNED_INT,   Integer.BYTES),
    FLOAT (GL11.GL_FLOAT,          Float.BYTES),
    DOUBLE(GL11.GL_DOUBLE,         Double.BYTES);
    
    public final int glEnum;
    
    /**
     * Size of this primitive type, measured in bytes.
     */
    public final int size;
    
    private Primitive(int glEnum, int size)
    {
        this.glEnum = glEnum;
        this.size = size;
    }
    
    public static int sizeof(byte value)
    {
        return BYTE.size;
    }
    
    public static int sizeof(byte[] array)
    {
        return array.length;
    }
    
    public static int sizeof(short value)
    {
        return SHORT.size;
    }
    
    public static int sizeof(short[] array)
    {
        return array.length*SHORT.size;
    }
    
    public static int sizeof(int value)
    {
        return INT.size;
    }
    
    public static int sizeof(int[] array)
    {
        return array.length*INT.size;
    }
    
    public static int sizeof(float value)
    {
        return FLOAT.size;
    }
    
    public static int sizeof(float[] array)
    {
        return array.length*FLOAT.size;
    }
    
    public static int sizeof(double value)
    {
        return DOUBLE.size;
    }
    
    public static int sizeof(double[] array)
    {
        return array.length*DOUBLE.size;
    }
}
