package com.samrj.devil.gl;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL32;

public final class Shader
{
    public static enum Type
    {
        VERTEX(GL20.GL_VERTEX_SHADER),
        FRAGMENT(GL20.GL_FRAGMENT_SHADER),
        GEOMETRY(GL32.GL_GEOMETRY_SHADER);
        
        public final int glEnum;
        
        private Type(int glEnum)
        {
            this.glEnum = glEnum;
        }
    }
    
    private int id;
    private Type type;
    
    public Shader(String path, Type type) throws ShaderException, IOException
    {
        if (path == null || type == null) throw new NullPointerException();
        
        //Load source
        URL url = ClassLoader.getSystemResource(path);
        InputStream in = url.openStream();
        if (in == null) throw new FileNotFoundException(path);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        
        String source = "";
        String line;
        while ((line=reader.readLine()) != null) source += line + '\n';
        reader.close();
        in.close();
        
        //Compile source
        id = GL20.glCreateShader(type.glEnum);
        GL20.glShaderSource(id, source);
        GL20.glCompileShader(id);
        
        if (GL20.glGetShaderi(id, GL20.GL_COMPILE_STATUS) != GL11.GL_TRUE)
        {
            int logLength =  GL20.glGetShaderi(id, GL20.GL_INFO_LOG_LENGTH);
            String log = GL20.glGetShaderInfoLog(id, logLength);
            throw new ShaderException(log);
        }
    }
    
    public int getID()
    {
        return id;
    }
    
    public Type getType()
    {
        return type;
    }
}
