package com.samrj.devil.gl;

import com.samrj.devil.gl.util.Primitive;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL40;

//Have the following:
//GL_FLOAT
//GL_FLOAT_VEC2
//GL_FLOAT_VEC3
//GL_FLOAT_VEC4
//GL_DOUBLE
//GL_DOUBLE_VEC2
//GL_DOUBLE_VEC3
//GL_DOUBLE_VEC4
//GL_INT
//GL_INT_VEC2
//GL_INT_VEC3
//GL_INT_VEC4
//GL_BOOL
//GL_BOOL_VEC2
//GL_BOOL_VEC3
//GL_BOOL_VEC4
//GL_FLOAT_MAT2
//GL_FLOAT_MAT3
//GL_FLOAT_MAT4
//GL_SAMPLER_1D
//GL_SAMPLER_2D
//GL_SAMPLER_3D
//GL_SAMPLER_CUBE

public enum VarType
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
    DOUBLE_VEC4(GL40.GL_DOUBLE_VEC4, 4, Primitive.DOUBLE),
    FLOAT_MAT2 (GL20.GL_FLOAT_MAT2,  4, Primitive.FLOAT),
    FLOAT_MAT3 (GL20.GL_FLOAT_MAT3,  9, Primitive.FLOAT),
    FLOAT_MAT4 (GL20.GL_FLOAT_MAT4, 16, Primitive.FLOAT);

    static VarType fromGLEnum(int glEnum)
    {
        switch (glEnum)
        {
            case GL11.GL_FLOAT:       return FLOAT;
            case GL20.GL_FLOAT_VEC2:  return FLOAT_VEC2;
            case GL20.GL_FLOAT_VEC3:  return FLOAT_VEC3;
            case GL20.GL_FLOAT_VEC4:  return FLOAT_VEC4;
            case GL11.GL_INT:         return INT;
            case GL20.GL_INT_VEC2:    return INT_VEC2;
            case GL20.GL_INT_VEC3:    return INT_VEC3;
            case GL20.GL_INT_VEC4:    return INT_VEC4;
            case GL11.GL_DOUBLE:      return DOUBLE;
            case GL40.GL_DOUBLE_VEC2: return DOUBLE_VEC2;
            case GL40.GL_DOUBLE_VEC3: return DOUBLE_VEC3;
            case GL40.GL_DOUBLE_VEC4: return DOUBLE_VEC4;
            case GL20.GL_FLOAT_MAT2:  return FLOAT_MAT2;
            case GL20.GL_FLOAT_MAT3:  return FLOAT_MAT3;
            case GL20.GL_FLOAT_MAT4:  return FLOAT_MAT4;
            default: throw new IllegalArgumentException(
                    "Attribute type #" + glEnum + " not supported by DevilGL. "
                    + "Please open an issue on GitHub.");
        }
    }

    public final int glEnum;
    public final int size;
    public final Primitive dataType;

    private VarType(int glEnum, int size, Primitive dataType)
    {
        this.glEnum = glEnum;
        this.size = size;
        this.dataType = dataType;
    }
}