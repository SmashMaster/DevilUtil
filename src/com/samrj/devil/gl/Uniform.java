package com.samrj.devil.gl;

import com.samrj.devil.gl.texture.Texture2D;
import org.lwjgl.opengl.GL20;

public class Uniform
{
    public final String name;
    private int index;
    private VarType type; //OpenGL vertex attribute type enum
    private boolean enabled = false;
    
    Uniform(String name)
    {
        if (name == null) throw new NullPointerException();
        this.name = name;
    }
    
    boolean enableSoft(ShaderProgram shader)
    {
        if (shader == null)
        {
            disable();
            return false;
        }
        
        int shaderID = shader.getID();
        
        index = GL20.glGetUniformLocation(shaderID, name);
        if (index == -1)
        {
            disable();
            return false;
        }
        
        type = VarType.fromGLEnum(GL20.glGetActiveUniformType(shaderID, index));
        enabled = true;
        return true;
    }
    
    void disable()
    {
        index = -1;
        type = null;
        enabled = false;
    }
    
//    FLOAT       (GL11.GL_FLOAT,        1, Primitive.FLOAT),
//    FLOAT_VEC2  (GL20.GL_FLOAT_VEC2,   2, Primitive.FLOAT),
//    FLOAT_VEC3  (GL20.GL_FLOAT_VEC3,   3, Primitive.FLOAT),
//    FLOAT_VEC4  (GL20.GL_FLOAT_VEC4,   4, Primitive.FLOAT),
//    DOUBLE      (GL11.GL_DOUBLE,       1, Primitive.DOUBLE),
//    DOUBLE_VEC2 (GL40.GL_DOUBLE_VEC2,  2, Primitive.DOUBLE),
//    DOUBLE_VEC3 (GL40.GL_DOUBLE_VEC3,  3, Primitive.DOUBLE),
//    DOUBLE_VEC4 (GL40.GL_DOUBLE_VEC4,  4, Primitive.DOUBLE),
//    INT         (GL11.GL_INT,          1, Primitive.INT),
//    INT_VEC2    (GL20.GL_INT_VEC2,     2, Primitive.INT),
//    INT_VEC3    (GL20.GL_INT_VEC3,     3, Primitive.INT),
//    INT_VEC4    (GL20.GL_INT_VEC4,     4, Primitive.INT),
//    BOOL        (GL20.GL_BOOL,         1, Primitive.INT),
//    BOOL_VEC2   (GL20.GL_BOOL_VEC2,    2, Primitive.INT),
//    BOOL_VEC3   (GL20.GL_BOOL_VEC3,    3, Primitive.INT),
//    BOOL_VEC4   (GL20.GL_BOOL_VEC4,    4, Primitive.INT),
//    FLOAT_MAT2  (GL20.GL_FLOAT_MAT2,   2, Primitive.FLOAT, 2),
//    FLOAT_MAT3  (GL20.GL_FLOAT_MAT3,   3, Primitive.FLOAT, 3),
//    FLOAT_MAT4  (GL20.GL_FLOAT_MAT4,   4, Primitive.FLOAT, 4),
//    SAMPLER_1D  (GL20.GL_SAMPLER_1D,   1, Primitive.INT),
//    SAMPLER_2D  (GL20.GL_SAMPLER_2D,   1, Primitive.INT),
//    SAMPLER_3D  (GL20.GL_SAMPLER_3D,   1, Primitive.INT),
//    SAMPLER_CUBE(GL20.GL_SAMPLER_CUBE, 1, Primitive.INT);
    
    private void ensureEnabled()
    {
        if (!enabled) throw new IllegalStateException(
                "No such shader variable: '" + name + "'");
    }
    
    private void ensureType(VarType type)
    {
        if (this.type != type) throw new IllegalArgumentException(
                "Expected type " + this.type + ", got " + type + "!");
    }
    
    //FLOAT
    
    public void set(float x)
    {
        ensureEnabled();
        ensureType(VarType.FLOAT);
        GL20.glUniform1f(index, x);
    }
    
    public void set(float x, float y)
    {
        ensureEnabled();
        ensureType(VarType.FLOAT_VEC2);
        GL20.glUniform2f(index, x, y);
    }
    
    public void set(float x, float y, float z)
    {
        ensureEnabled();
        ensureType(VarType.FLOAT_VEC3);
        GL20.glUniform3f(index, x, y, z);
    }
    
    public void set(float x, float y, float z, float w)
    {
        ensureEnabled();
        ensureType(VarType.FLOAT_VEC4);
        GL20.glUniform4f(index, x, y, z, w);
    }
    
    //INT
    
    public void set(int x)
    {
        ensureEnabled();
        ensureType(VarType.INT);
        GL20.glUniform1i(index, x);
    }
    
    public void set(int x, int y)
    {
        ensureEnabled();
        ensureType(VarType.INT_VEC2);
        GL20.glUniform2i(index, x, y);
    }
    
    public void set(int x, int y, int z)
    {
        ensureEnabled();
        ensureType(VarType.INT_VEC3);
        GL20.glUniform3i(index, x, y, z);
    }
    
    public void set(int x, int y, int z, int w)
    {
        ensureEnabled();
        ensureType(VarType.INT_VEC4);
        GL20.glUniform4i(index, x, y, z, w);
    }
    
    //BOOLEAN
    
    private static int btoi(boolean b)
    {
        return b ? 1 : 0;
    }
    
    public void set(boolean x)
    {
        ensureEnabled();
        ensureType(VarType.BOOL);
        GL20.glUniform1i(index, btoi(x));
    }
    
    public void set(boolean x, boolean y)
    {
        ensureEnabled();
        ensureType(VarType.BOOL_VEC2);
        GL20.glUniform2i(index, btoi(x), btoi(y));
    }
    
    public void set(boolean x, boolean y, boolean z)
    {
        ensureEnabled();
        ensureType(VarType.BOOL_VEC3);
        GL20.glUniform3i(index, btoi(x), btoi(y), btoi(z));
    }
    
    public void set(boolean x, boolean y, boolean z, boolean w)
    {
        ensureEnabled();
        ensureType(VarType.BOOL_VEC4);
        GL20.glUniform4i(index, btoi(x), btoi(y), btoi(z), btoi(w));
    }
    
    //SAMPLERS
    
    public void bind(Texture2D tex)
    {
        ensureEnabled();
        ensureType(VarType.SAMPLER_2D);
        //Bind texture to first available texture unit, assign uniform to unit index
    }
}
