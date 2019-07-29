package com.samrj.devil.gl;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL20C.*;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2019 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public enum AttributeType
{
    FLOAT(4,  1, 1, GL_FLOAT,      GL_FLOAT),
    VEC2 (8,  2, 1, GL_FLOAT_VEC2, GL_FLOAT),
    VEC3 (12, 3, 1, GL_FLOAT_VEC3, GL_FLOAT),
    VEC4 (16, 4, 1, GL_FLOAT_VEC4, GL_FLOAT),
    MAT2 (8,  2, 2, GL_FLOAT_MAT2, GL_FLOAT),
    MAT3 (12, 3, 3, GL_FLOAT_MAT3, GL_FLOAT),
    MAT4 (16, 4, 4, GL_FLOAT_MAT4, GL_FLOAT),
    INT  (4,  1, 1, GL_INT,        GL_INT),
    VEC2I(8,  2, 1, GL_INT_VEC2,   GL_INT),
    VEC3I(12, 3, 1, GL_INT_VEC3,   GL_INT),
    VEC4I(16, 4, 1, GL_INT_VEC4,   GL_INT),
    NONE(0, 0, 0, GL_FALSE, GL_FALSE);
    
    public static final AttributeType get(int glEnum)
    {
        switch (glEnum)
        {
            case GL_FLOAT:      return AttributeType.FLOAT;
            case GL_FLOAT_VEC2: return AttributeType.VEC2;
            case GL_FLOAT_VEC3: return AttributeType.VEC3;
            case GL_FLOAT_VEC4: return AttributeType.VEC4;
            case GL_FLOAT_MAT2: return AttributeType.MAT2;
            case GL_FLOAT_MAT3: return AttributeType.MAT3;
            case GL_FLOAT_MAT4: return AttributeType.MAT4;
            case GL_INT:        return AttributeType.INT;
            case GL_INT_VEC2:   return AttributeType.VEC2I;
            case GL_INT_VEC3:   return AttributeType.VEC3I;
            case GL_INT_VEC4:   return AttributeType.VEC4I;
            default: return null;
        }
    }

    /**
     * The size, in bytes, of one layer of this attribute type.
     */
    public final int size;
    
    /**
     * The number of components per layer for this attribute type.
     */
    public final int components;

    /**
     * The number of locations this attribute type occupies.
     */
    public final int layers;
    
    /**
     * The OpenGL enumerator for this attribute type.
     */
    public final int glEnum;
    
    /**
     * The OpenGL enumerator for this attribute's component type.
     */
    public final int glComponent;
    
    /**
     * Whether or not this type is an integer type.
     */
    public final boolean isInteger;

    private AttributeType(int size, int components, int layers, int glEnum, int glComponent)
    {
        this.size = size;
        this.components = components;
        this.layers = layers;
        this.glEnum = glEnum;
        this.glComponent = glComponent;
        isInteger = glComponent == GL_INT;
    }
}
