package com.samrj.devil.graphics;

import com.samrj.devil.buffer.IntBuffer;
import static com.samrj.devil.buffer.PublicBuffers.fbuffer;
import static com.samrj.devil.buffer.PublicBuffers.ibuffer;
import com.samrj.devil.math.Matrix2f;
import com.samrj.devil.math.Matrix3f;
import com.samrj.devil.math.Matrix4f;
import com.samrj.devil.res.Resource;
import java.io.*;
import org.lwjgl.BufferUtils;
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
        
        java.nio.IntBuffer shadBuf = BufferUtils.createIntBuffer(2);
        shadBuf.limit(2); shadBuf.position(0);
        GL20.glGetAttachedShaders(id, new IntBuffer(1).write(1), shadBuf);
        
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
    
    private int id, vertid, fragid;
    
    private GLShader(int id, int vertid, int fragid)
    {
        this.id = id;
        this.vertid = vertid;
        this.fragid = fragid;
    }
    
    public GLShader(Resource vert, Resource frag, boolean shouldComplete) throws IOException, ShaderCompileException
    {
        if (vert == null || frag == null) throw new NullPointerException();
        id = GL20.glCreateProgram();
        vertid = loadShader(vert, GL20.GL_VERTEX_SHADER);
        fragid = loadShader(frag, GL20.GL_FRAGMENT_SHADER);
        
        GL20.glAttachShader(id, vertid);
        GL20.glAttachShader(id, fragid);

        if (shouldComplete) glComplete();
    }
    
    public GLShader(Resource vert, Resource frag) throws IOException, ShaderCompileException
    {
        this(vert, frag, true);
    }
    
    public GLShader(String vertPath, String fragPath, boolean shouldComplete) throws IOException, ShaderCompileException
    {
        this(Resource.find(vertPath), Resource.find(fragPath), shouldComplete);
    }
    
    public GLShader(String vertPath, String fragPath) throws IOException, ShaderCompileException
    {
        this(vertPath, fragPath, true);
    }
    
    public GLShader(String path, boolean shouldComplete) throws IOException, ShaderCompileException
    {
        this(path + ".vert", path + ".frag", shouldComplete);
    }
    
    public GLShader(String path) throws IOException, ShaderCompileException
    {
        this(path, true);
    }
    
    public void glComplete() throws ShaderCompileException
    {
        GL20.glLinkProgram(id);
        checkProgramStatus(GL20.GL_LINK_STATUS);
        GL20.glValidateProgram(id);
        checkProgramStatus(GL20.GL_VALIDATE_STATUS);
    }
    
    private void checkProgramStatus(int type) throws ShaderCompileException
    {
        if (GL20.glGetProgrami(id, type) != GL11.GL_TRUE)
        {
            String log = GL20.glGetProgramInfoLog(id, GL20.glGetProgrami(id, GL20.GL_INFO_LOG_LENGTH));
            throw new ShaderCompileException(log);
        }
    }
    
    private int loadShader(Resource path, int type) throws IOException, ShaderCompileException
    {
        InputStream in = path.open();
        if (in == null) throw new FileNotFoundException(path.path);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        
        String source = "";
        String line;
        while ((line=reader.readLine()) != null) source += line + '\n';
        reader.close();
        in.close();
        
        int shader = GL20.glCreateShader(type);
        if (shader == 0) return 0;
        
        GL20.glShaderSource(shader, source);
        GL20.glCompileShader(shader);
        
        if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) != GL11.GL_TRUE)
        {
            String log = GL20.glGetShaderInfoLog(shader, GL20.glGetShaderi(shader, GL20.GL_INFO_LOG_LENGTH));
            throw new ShaderCompileException(log);
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
        return GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM) == id;
    }
    
    public void glBindFragDataLocation(int colorNumber, String name)
    {
        GL30.glBindFragDataLocation(id, colorNumber, name);
    }
    
    // <editor-fold defaultstate="collapsed" desc="Uniform Methods">
    private int getUniLoc(String name)
    {
        if (!glInUse()) throw new IllegalStateException();
        
        int out = GL20.glGetUniformLocation(id, name);
        
        if (out < 0) throw new UniformNotFoundException(name);
        return out;
    }
    
    private void glUniformi(int loc, int elementSize)
    {
        switch (elementSize)
        {
            case 1: GL20.glUniform1(loc, ibuffer.get()); break;
            case 2: GL20.glUniform2(loc, ibuffer.get()); break;
            case 3: GL20.glUniform3(loc, ibuffer.get()); break;
            case 4: GL20.glUniform4(loc, ibuffer.get()); break;
            default: throw new IllegalArgumentException();
        }
    }
    
    private void glUniformf(int loc, int elementSize)
    {
        switch (elementSize)
        {
            case 1: GL20.glUniform1(loc, fbuffer.get()); break;
            case 2: GL20.glUniform2(loc, fbuffer.get()); break;
            case 3: GL20.glUniform3(loc, fbuffer.get()); break;
            case 4: GL20.glUniform4(loc, fbuffer.get()); break;
            default: throw new IllegalArgumentException();
        }
    }
    
    public void glUniform(int elementSize, String name, int... data)
    {
        ibuffer.clear();
        ibuffer.put(data);
        glUniformi(getUniLoc(name), elementSize);
    }
    
    public void glUniform(String name, int... data)
    {
        glUniform(1, name, data);
    }
    
    public void glUniform(String name, boolean... data)
    {
        ibuffer.clear();
        for (boolean b : data) ibuffer.put(b ? 1 : 0);
        glUniformi(getUniLoc(name), 1);
    }
    
    public void glUniform(int elementSize, String name, float... data)
    {
        fbuffer.clear();
        fbuffer.put(data);
        glUniformf(getUniLoc(name), elementSize);
    }
    
    public void glUniform(String name, float... data)
    {
        glUniform(1, name, data);
    }
    
//    public void glUniform(String name, Bufferable<FloatBuffer>... data)
//    {
//        if (data.length == 0) throw new IllegalArgumentException();
//        
//        int elementSize = data[0].size();
//        int loc = getUniLoc(name);
//        
//        fbuffer.clear();
//        for (Bufferable b : data) b.putIn(fbuffer);
//        glUniformf(loc, elementSize);
//    }
    
    public void glUniform(String name, Matrix2f m)
    {
        fbuffer.clear();
        m.putIn(fbuffer);
        GL20.glUniformMatrix2(getUniLoc(name), false, fbuffer.get());
    }
    
    public void glUniform(String name, Matrix3f m)
    {
        fbuffer.clear();
        m.putIn(fbuffer);
        GL20.glUniformMatrix3(getUniLoc(name), false, fbuffer.get());
    }
    
    public void glUniform(String name, Matrix4f m)
    {
        fbuffer.clear();
        m.putIn(fbuffer);
        GL20.glUniformMatrix4(getUniLoc(name), false, fbuffer.get());
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Attribute Methods">
    public int glGetAttribLocation(String name)
    {
        if (!glInUse()) throw new IllegalStateException();
        
        int out = GL20.glGetAttribLocation(id, name);
        
        if (out < 0) throw new UniformNotFoundException();
        return out;
    }
    
    public void glAttribute(String name, float... data)
    {
        if (!glInUse()) throw new IllegalStateException();
        
        final int loc = glGetAttribLocation(name);
        
        switch (data.length)
        {
            case 1: GL20.glVertexAttrib1f(loc, data[0]); break;
            case 2: GL20.glVertexAttrib2f(loc, data[0], data[1]); break;
            case 3: GL20.glVertexAttrib3f(loc, data[0], data[1], data[2]); break;
            case 4: GL20.glVertexAttrib4f(loc, data[0], data[1], data[2], data[3]); break;
            default: throw new IllegalArgumentException();
        }
    }
    // </editor-fold>
    
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