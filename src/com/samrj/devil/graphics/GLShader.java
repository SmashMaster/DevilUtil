package com.samrj.devil.graphics;

import com.samrj.devil.io.BufferUtil;
import static com.samrj.devil.io.BufferUtil.pubBufA;
import static com.samrj.devil.io.BufferUtil.pubBufB;
import com.samrj.devil.io.Bufferable;
import com.samrj.devil.math.Mat2;
import com.samrj.devil.math.Mat3;
import com.samrj.devil.math.Mat4;
import com.samrj.devil.res.Resource;
import java.io.*;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
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
        int id = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM); GLDebug.check();
        if (id == 0) return null;
        
        int numShaders = GL20.glGetProgrami(id, GL20.GL_ATTACHED_SHADERS); GLDebug.check();
        if (numShaders != 2) return null;
        
        BufferUtil.clearPublicBuffers();
        IntBuffer shadBuf = pubBufA.asIntBuffer();
        IntBuffer countBuf = pubBufB.asIntBuffer();
        countBuf.put(1);
        countBuf.rewind();
        GL20.glGetAttachedShaders(id, countBuf, shadBuf); GLDebug.check();
        
        int vertid = shadBuf.get(0);
        int fragid = shadBuf.get(1);
        
        if (GL20.glGetShaderi(fragid, GL20.GL_SHADER_TYPE) == GL20.GL_VERTEX_SHADER)
        {
            int temp = vertid;
            vertid = fragid;
            fragid = temp;
        }
        
        return new GLShader(id, vertid, fragid);
    }
    
    private static FloatBuffer buffer(Bufferable<FloatBuffer> data)
    {
        FloatBuffer out = pubBufA.asFloatBuffer();
        out.clear();
        data.write(out);
        out.rewind();
        return out;
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
        id = GL20.glCreateProgram(); GLDebug.check();
        vertid = loadShader(vert, GL20.GL_VERTEX_SHADER);
        fragid = loadShader(frag, GL20.GL_FRAGMENT_SHADER);
        
        GL20.glAttachShader(id, vertid); GLDebug.check();
        GL20.glAttachShader(id, fragid); GLDebug.check();

        if (shouldComplete) glComplete();
    }
    
    GLShader(ShaderSource vert, ShaderSource frag, boolean shouldComplete) throws ShaderException
    {
        if (vert == null || frag == null) throw new NullPointerException();
        id = GL20.glCreateProgram();
        vertid = loadShader(vert.getSource(), GL20.GL_VERTEX_SHADER);
        fragid = loadShader(frag.getSource(), GL20.GL_FRAGMENT_SHADER);
        
        GL20.glAttachShader(id, vertid); GLDebug.check();
        GL20.glAttachShader(id, fragid); GLDebug.check();

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
        GL20.glLinkProgram(id); GLDebug.check();
        checkProgramStatus(GL20.GL_LINK_STATUS);
    }
    
    public void glValidate() throws ShaderException
    {
        GL20.glValidateProgram(id); GLDebug.check();
        checkProgramStatus(GL20.GL_VALIDATE_STATUS);
    }
    
    private void checkProgramStatus(int type) throws ShaderException
    {
        if (GL20.glGetProgrami(id, type) != GL11.GL_TRUE)
        {
            GLDebug.check();
            String log = GL20.glGetProgramInfoLog(id, GL20.glGetProgrami(id, GL20.GL_INFO_LOG_LENGTH)); GLDebug.check();
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
        
        int shader = GL20.glCreateShader(type); GLDebug.check();
        if (shader == 0) return 0;
        
        GL20.glShaderSource(shader, source); GLDebug.check();
        GL20.glCompileShader(shader); GLDebug.check();
        
        if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) != GL11.GL_TRUE)
        {
            GLDebug.check();
            String log = GL20.glGetShaderInfoLog(shader, GL20.glGetShaderi(shader, GL20.GL_INFO_LOG_LENGTH)); GLDebug.check();
            throw new ShaderException(log);
        }
        
        return shader;
    }
    
    public void glUse()
    {
        GL20.glUseProgram(id); GLDebug.check();
    }
    
    public void glUnuse()
    {
        GL20.glUseProgram(0); GLDebug.check();
    }
    
    public boolean glInUse()
    {
        boolean out = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM) == id; GLDebug.check();
        return out;
    }
    
    public void glBindFragDataLocation(int colorNumber, String name)
    {
        GL30.glBindFragDataLocation(id, colorNumber, name); GLDebug.check();
    }
    
    public int glGetAttribLocation(String name)
    {
        if (!glInUse()) throw new IllegalStateException();
        int out = GL20.glGetAttribLocation(id, name); GLDebug.check();
        if (out < 0) throw new LocationNotFoundException();
        return out;
    }
    
    public int glGetUniformLocation(String name)
    {
        if (!glInUse()) throw new IllegalStateException();
        int out = GL20.glGetUniformLocation(id, name); GLDebug.check();
        if (out < 0) throw new LocationNotFoundException();
        return out;
    }
    
    public void glUniform1i(String name, int x)
    {
        int loc = glGetUniformLocation(name);
        GL20.glUniform1i(loc, x); GLDebug.check();
    }
    
    public void glUniform1f(String name, float x)
    {
        int loc = glGetUniformLocation(name);
        GL20.glUniform1f(loc, x); GLDebug.check();
    }
    
    public void glUniform2f(String name, float x, float y)
    {
        int loc = glGetUniformLocation(name);
        GL20.glUniform2f(loc, x, y); GLDebug.check();
    }
    
    public void glUniform3f(String name, float x, float y, float z)
    {
        int loc = glGetUniformLocation(name);
        GL20.glUniform3f(loc, x, y, z); GLDebug.check();
    }
    
    public void glUniform4f(String name, float x, float y, float z, float w)
    {
        int loc = glGetUniformLocation(name);
        GL20.glUniform4f(loc, x, y, z, w); GLDebug.check();
    }
    
    public void glUniformMatrix2f(String name, Mat2 matrix)
    {
        int loc = glGetUniformLocation(name);
        GL20.glUniformMatrix2fv(loc, false, buffer(matrix)); GLDebug.check();
    }
    
    public void glUniformMatrix3f(String name, Mat3 matrix)
    {
        int loc = glGetUniformLocation(name);
        GL20.glUniformMatrix3fv(loc, false, buffer(matrix)); GLDebug.check();
    }
    
    public void glUniformMatrix4f(String name, Mat4 matrix)
    {
        int loc = glGetUniformLocation(name);
        GL20.glUniformMatrix4fv(loc, false, buffer(matrix)); GLDebug.check();
    }
    
    public int id()
    {
        return id;
    }
    
    public void glDelete()
    {
        GL20.glDeleteProgram(id); GLDebug.check();
        GL20.glDeleteShader(vertid); GLDebug.check();
        GL20.glDeleteShader(fragid); GLDebug.check();
        id = -1;
        vertid = -1;
        fragid = -1;
    }
}
