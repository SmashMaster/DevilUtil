package com.samrj.devil.gl;

public class ShaderProgram
{
    public ShaderProgram(Shader... shaders)
    {
    }
    
    public ShaderProgram(String vertPath, String fragPath)
    {
        this(new Shader(vertPath, Shader.Type.VERTEX),
             new Shader(fragPath, Shader.Type.FRAGMENT));
    }
}
