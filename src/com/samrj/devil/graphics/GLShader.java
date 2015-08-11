package com.samrj.devil.graphics;

import com.samrj.devil.io.Block;
import com.samrj.devil.io.BufferUtil;
import com.samrj.devil.math.Mat2;
import com.samrj.devil.math.Mat3;
import com.samrj.devil.math.Mat4;
import com.samrj.devil.math.Vec3;
import com.samrj.devil.res.Resource;
import java.io.*;
import java.nio.ByteBuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class GLShader
{
    public static GLShader getCurrentShader()
    {
        int id = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
        if (id == 0) return null;
        
        int numShaders = GL20.glGetProgrami(id, GL20.GL_ATTACHED_SHADERS);
        if (numShaders != 2) return null;
        
        Block countBlock = BufferUtil.wrapi(1);
        Block shadBlock = BufferUtil.alloc(8);
        ByteBuffer shadBuf = BufferUtil.read(shadBlock);
        
        GL20.glGetAttachedShaders(id, BufferUtil.read(countBlock).asIntBuffer(), shadBuf.asIntBuffer());
        
        BufferUtil.free(countBlock);
        shadBuf.reset();
        int vertid = shadBuf.getInt();
        int fragid = shadBuf.getInt();
        BufferUtil.free(shadBlock);
        
        if (GL20.glGetShaderi(fragid, GL20.GL_SHADER_TYPE) == GL20.GL_VERTEX_SHADER)
        {
            int temp = vertid;
            vertid = fragid;
            fragid = temp;
        }
        
        return new GLShader(id, vertid, fragid);
    }
    
    private int id, vertid, fragid;
    
    private GLShader(int id, int vertid, int fragid)
    {
        this.id = id;
        this.vertid = vertid;
        this.fragid = fragid;
    }
    
    public GLShader(Resource vert, Resource frag, boolean shouldComplete) throws IOException, ShaderException
    {
        if (vert == null || frag == null) throw new NullPointerException();
        id = GL20.glCreateProgram();
        vertid = loadShader(vert, GL20.GL_VERTEX_SHADER);
        fragid = loadShader(frag, GL20.GL_FRAGMENT_SHADER);
        
        GL20.glAttachShader(id, vertid);
        GL20.glAttachShader(id, fragid);

        if (shouldComplete) glComplete();
    }
    
    GLShader(ShaderSource vert, ShaderSource frag, boolean shouldComplete) throws ShaderException
    {
        if (vert == null || frag == null) throw new NullPointerException();
        id = GL20.glCreateProgram();
        vertid = loadShader(vert.getSource(), GL20.GL_VERTEX_SHADER);
        fragid = loadShader(frag.getSource(), GL20.GL_FRAGMENT_SHADER);
        
        GL20.glAttachShader(id, vertid);
        GL20.glAttachShader(id, fragid);

        if (shouldComplete) glComplete();
    }
    
    public GLShader(Resource vert, Resource frag) throws IOException, ShaderException
    {
        this(vert, frag, true);
    }
    
    public GLShader(String vertPath, String fragPath, boolean shouldComplete) throws IOException, ShaderException
    {
        this(Resource.find(vertPath), Resource.find(fragPath), shouldComplete);
    }
    
    public GLShader(String vertPath, String fragPath) throws IOException, ShaderException
    {
        this(vertPath, fragPath, true);
    }
    
    public GLShader(String path, boolean shouldComplete) throws IOException, ShaderException
    {
        this(path + ".vert", path + ".frag", shouldComplete);
    }
    
    public GLShader(String path) throws IOException, ShaderException
    {
        this(path, true);
    }
    
    public void glComplete() throws ShaderException
    {
        glLink();
        glValidate();
    }
    
    public void glLink() throws ShaderException
    {
        GL20.glLinkProgram(id);
        checkProgramStatus(GL20.GL_LINK_STATUS);
    }
    
    public void glValidate() throws ShaderException
    {
        GL20.glValidateProgram(id);
        checkProgramStatus(GL20.GL_VALIDATE_STATUS);
    }
    
    private void checkProgramStatus(int type) throws ShaderException
    {
        if (GL20.glGetProgrami(id, type) != GL11.GL_TRUE)
        {
            String log = GL20.glGetProgramInfoLog(id, GL20.glGetProgrami(id, GL20.GL_INFO_LOG_LENGTH));
            throw new ShaderException(log);
        }
    }
    
    private int loadShader(Resource path, int type) throws IOException, ShaderException
    {
        InputStream in = path.open();
        if (in == null) throw new FileNotFoundException(path.path);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        
        String source = "";
        String line;
        while ((line=reader.readLine()) != null) source += line + '\n';
        reader.close();
        in.close();
        
        return loadShader(source, type);
    }
    
    private int loadShader(String source, int type) throws ShaderException
    {
        if (source == null) throw new NullPointerException();
        
        int shader = GL20.glCreateShader(type);
        if (shader == 0) return 0;
        
        GL20.glShaderSource(shader, source);
        GL20.glCompileShader(shader);
        
        if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) != GL11.GL_TRUE)
        {
            String log = GL20.glGetShaderInfoLog(shader, GL20.glGetShaderi(shader, GL20.GL_INFO_LOG_LENGTH));
            throw new ShaderException(log);
        }
        
        return shader;
    }
    
    public void glUse()
    {
        GL20.glUseProgram(id);
    }
    
    public void glUnuse()
    {
        GL20.glUseProgram(0);
    }
    
    public boolean glInUse()
    {
        boolean out = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM) == id;
        return out;
    }
    
    public void glBindFragDataLocation(int colorNumber, String name)
    {
        GL30.glBindFragDataLocation(id, colorNumber, name);
    }
    
    public int glGetAttribLocation(String name)
    {
        if (!glInUse()) throw new IllegalStateException();
        int out = GL20.glGetAttribLocation(id, name);
        if (out < 0) throw new LocationNotFoundException();
        return out;
    }
    
    public int glGetUniformLocation(String name)
    {
        if (!glInUse()) throw new IllegalStateException();
        int out = GL20.glGetUniformLocation(id, name);
        if (out < 0) throw new LocationNotFoundException();
        return out;
    }
    
    public void glUniform1i(String name, int x)
    {
        int loc = glGetUniformLocation(name);
        GL20.glUniform1i(loc, x);
    }
    
    public void glUniform1f(String name, float x)
    {
        int loc = glGetUniformLocation(name);
        GL20.glUniform1f(loc, x);
    }
    
    public void glUniform2f(String name, float x, float y)
    {
        int loc = glGetUniformLocation(name);
        GL20.glUniform2f(loc, x, y);
    }
    
    public void glUniform3f(String name, float x, float y, float z)
    {
        int loc = glGetUniformLocation(name);
        GL20.glUniform3f(loc, x, y, z);
    }
    
    public void glUniform3f(String name, Vec3 v)
    {
        glUniform3f(name, v.x, v.y, v.z);
    }
    
    public void glUniform4f(String name, float x, float y, float z, float w)
    {
        int loc = glGetUniformLocation(name);
        GL20.glUniform4f(loc, x, y, z, w);
    }
    
    public void glUniformMatrix2f(String name, Mat2 matrix)
    {
        Block b = BufferUtil.alloc(matrix);
        int loc = glGetUniformLocation(name);
        GL20.glUniformMatrix2fv(loc, false, BufferUtil.read(b).asFloatBuffer());
        BufferUtil.free(b);
    }
    
    public void glUniformMatrix3f(String name, Mat3 matrix)
    {
        Block b = BufferUtil.alloc(matrix);
        int loc = glGetUniformLocation(name);
        GL20.glUniformMatrix3fv(loc, false, BufferUtil.read(b).asFloatBuffer());
        BufferUtil.free(b);
    }
    
    public void glUniformMatrix4f(String name, Mat4 matrix)
    {
        Block b = BufferUtil.alloc(matrix);
        int loc = glGetUniformLocation(name);
        GL20.glUniformMatrix4fv(loc, false, BufferUtil.read(b).asFloatBuffer());
        BufferUtil.free(b);
    }
    
    public int id()
    {
        return id;
    }
    
    public void glDelete()
    {
        GL20.glDeleteProgram(id);
        GL20.glDeleteShader(vertid);
        GL20.glDeleteShader(fragid);
        id = -1;
        vertid = -1;
        fragid = -1;
    }
}
