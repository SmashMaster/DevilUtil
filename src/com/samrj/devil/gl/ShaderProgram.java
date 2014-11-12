package com.samrj.devil.gl;

import java.io.IOException;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

public final class ShaderProgram
{
    private final int id;
    
    public ShaderProgram(Shader... shaders) throws ShaderException
    {
        id = GL20.glCreateProgram();
        
        for (Shader shader : shaders)
            GL20.glAttachShader(id, shader.getID());
        
        GL20.glLinkProgram(id);
        checkStatus(GL20.GL_LINK_STATUS);
        GL20.glValidateProgram(id);
        checkStatus(GL20.GL_VALIDATE_STATUS);
    }
    
    public ShaderProgram(String vertPath, String fragPath)
            throws ShaderException, IOException
    {
        this(new Shader(vertPath, Shader.Type.VERTEX),
             new Shader(fragPath, Shader.Type.FRAGMENT));
    }
    
    private void checkStatus(int type) throws ShaderException
    {
        if (GL20.glGetProgrami(id, type) != GL11.GL_TRUE)
        {
            int logLength = GL20.glGetProgrami(id, GL20.GL_INFO_LOG_LENGTH);
            String log = GL20.glGetProgramInfoLog(id, logLength);
            throw new ShaderException(log);
        }
    }
    
    int getID()
    {
        return id;
    }
}
