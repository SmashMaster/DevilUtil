package com.samrj.devil.gl;

import com.samrj.devil.gl.util.Primitive;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL40;

public enum VarType
{
    FLOAT       (GL11.GL_FLOAT,        1, Primitive.FLOAT),
    FLOAT_VEC2  (GL20.GL_FLOAT_VEC2,   2, Primitive.FLOAT),
    FLOAT_VEC3  (GL20.GL_FLOAT_VEC3,   3, Primitive.FLOAT),
    FLOAT_VEC4  (GL20.GL_FLOAT_VEC4,   4, Primitive.FLOAT),
    DOUBLE      (GL11.GL_DOUBLE,       1, Primitive.DOUBLE),
    DOUBLE_VEC2 (GL40.GL_DOUBLE_VEC2,  2, Primitive.DOUBLE),
    DOUBLE_VEC3 (GL40.GL_DOUBLE_VEC3,  3, Primitive.DOUBLE),
    DOUBLE_VEC4 (GL40.GL_DOUBLE_VEC4,  4, Primitive.DOUBLE),
    INT         (GL11.GL_INT,          1, Primitive.INT),
    INT_VEC2    (GL20.GL_INT_VEC2,     2, Primitive.INT),
    INT_VEC3    (GL20.GL_INT_VEC3,     3, Primitive.INT),
    INT_VEC4    (GL20.GL_INT_VEC4,     4, Primitive.INT),
    BOOL        (GL20.GL_BOOL,         1, Primitive.INT),
    BOOL_VEC2   (GL20.GL_BOOL_VEC2,    2, Primitive.INT),
    BOOL_VEC3   (GL20.GL_BOOL_VEC3,    3, Primitive.INT),
    BOOL_VEC4   (GL20.GL_BOOL_VEC4,    4, Primitive.INT),
    FLOAT_MAT2  (GL20.GL_FLOAT_MAT2,   2, Primitive.FLOAT, 2),
    FLOAT_MAT3  (GL20.GL_FLOAT_MAT3,   3, Primitive.FLOAT, 3),
    FLOAT_MAT4  (GL20.GL_FLOAT_MAT4,   4, Primitive.FLOAT, 4),
    SAMPLER_1D  (GL20.GL_SAMPLER_1D,   1, Primitive.INT),
    SAMPLER_2D  (GL20.GL_SAMPLER_2D,   1, Primitive.INT),
    SAMPLER_3D  (GL20.GL_SAMPLER_3D,   1, Primitive.INT),
    SAMPLER_CUBE(GL20.GL_SAMPLER_CUBE, 1, Primitive.INT);

    static VarType fromGLEnum(int glEnum)
    {
        switch (glEnum)
        {
            case GL11.GL_FLOAT:        return FLOAT;
            case GL20.GL_FLOAT_VEC2:   return FLOAT_VEC2;
            case GL20.GL_FLOAT_VEC3:   return FLOAT_VEC3;
            case GL20.GL_FLOAT_VEC4:   return FLOAT_VEC4;
            case GL11.GL_DOUBLE:       return DOUBLE;
            case GL40.GL_DOUBLE_VEC2:  return DOUBLE_VEC2;
            case GL40.GL_DOUBLE_VEC3:  return DOUBLE_VEC3;
            case GL40.GL_DOUBLE_VEC4:  return DOUBLE_VEC4;
            case GL11.GL_INT:          return INT;
            case GL20.GL_INT_VEC2:     return INT_VEC2;
            case GL20.GL_INT_VEC3:     return INT_VEC3;
            case GL20.GL_INT_VEC4:     return INT_VEC4;
            case GL20.GL_BOOL:         return BOOL;
            case GL20.GL_BOOL_VEC2:    return BOOL_VEC2;
            case GL20.GL_BOOL_VEC3:    return BOOL_VEC3;
            case GL20.GL_BOOL_VEC4:    return BOOL_VEC4;
            case GL20.GL_FLOAT_MAT2:   return FLOAT_MAT2;
            case GL20.GL_FLOAT_MAT3:   return FLOAT_MAT3;
            case GL20.GL_FLOAT_MAT4:   return FLOAT_MAT4;
            case GL20.GL_SAMPLER_1D:   return SAMPLER_1D;
            case GL20.GL_SAMPLER_2D:   return SAMPLER_2D;
            case GL20.GL_SAMPLER_3D:   return SAMPLER_3D;
            case GL20.GL_SAMPLER_CUBE: return SAMPLER_CUBE;
            default: throw new IllegalArgumentException(
                    "Attribute type #" + glEnum + " not supported by DevilGL. "
                    + "Please open an issue on GitHub.");
        }
    }

    public final int glEnum;
    public final int size, numAttributes;
    public final Primitive dataType;
    
    private VarType(int glEnum, int size, Primitive dataType, int numAttributes)
    {
        this.glEnum = glEnum;
        this.size = size;
        this.numAttributes = numAttributes;
        this.dataType = dataType;
    }
    
    private VarType(int glEnum, int size, Primitive dataType)
    {
        this(glEnum, size, dataType, 1);
    }
}