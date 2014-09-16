package com.samrj.devil.graphics;

import com.samrj.devil.buffer.FloatBuffer;
import com.samrj.devil.buffer.IntBuffer;
import static com.samrj.devil.math.Util.PrimType.FLOAT;
import static com.samrj.devil.math.Util.PrimType.INT;
import static com.samrj.devil.math.Util.sizeof;
import java.util.ArrayList;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class GLVertexPool
{
    private FloatBuffer vertices;
    private int vertexSize;
    
    private IntBuffer indices;
    
    private GLShader shader;
    private int vao, vbo, ibo;
    private ArrayList<Attribute> attribs;
    /**
     * The VBO stride length, in bytes.
     */
    private int stride = 0;
    private int polyMode;
    
    /**
     * @param shader The shader that will be used when drawing this pool.
     * @param vertCapacity The maximum number of vertices this pool can contain.
     * @param indCapacity The maximum number of indices this pool can contain.
     * @param vertexSize The data size of each vertex in floats.
     * @param polyMode The OpenGL polygon mode enum.
     * @param usage The OpenGL buffer usage enum.
     */
    public GLVertexPool(GLShader shader, int vertCapacity, int indCapacity, int vertexSize, int polyMode, int usage)
    {
        if (shader == null) throw new NullPointerException();
        this.shader = shader;
        this.polyMode = polyMode;
        
        attribs = new ArrayList<>();
        
        vao = GL30.glGenVertexArrays();
        vbo = GL15.glGenBuffers();
        ibo = GL15.glGenBuffers();
        
        this.vertexSize = vertexSize;
        
        vertices = new FloatBuffer(vertCapacity*vertexSize);
        indices = new IntBuffer(indCapacity);
        
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertices.capacity()*sizeof(FLOAT), usage);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, ibo);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indices.capacity()*sizeof(INT), usage);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
    }
    
    public GLVertexPool(GLShader shader, int vertCapacity, int indCapacity, int vertexSize, int polyMode)
    {
        this(shader, vertCapacity, indCapacity, vertexSize, polyMode, GL15.GL_DYNAMIC_DRAW);
    }
    
    /**
     * 
     * @param name The name of this attribute in the shader.
     * @param normalized Whether this attribute is normalized.
     * @param size The size of this attribute, in floats.
     */
    public void addAttribute(String name, boolean normalized, int size)
    {
        int loc = shader.glGetAttribLocation(name);
        
        Attribute attrib = new Attribute();
        attrib.name = name;
        attrib.loc = loc;
        attrib.normalized = normalized;
        attrib.size = size;
        attrib.offset = stride;
        stride += size*4;
        
        attribs.add(attrib);
    }
    
    public int addVertices(float... data)
    {
        if (data.length % vertexSize != 0) throw new IllegalArgumentException();
        
        int oldSize = vertices.size();
        vertices.put(data);
        
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, oldSize*sizeof(FLOAT), vertices.get(oldSize, data.length));
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        
        return oldSize/vertexSize;
    }
    
    public int getNumVerts()
    {
        return vertices.size()/vertexSize;
    }
    
    public int getNumIndices()
    {
        return indices.size();
    }
    
    public void addIndices(int... data)
    {
        int oldSize = indices.size();
        indices.put(data);
        
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, ibo);
        GL15.glBufferSubData(GL15.GL_ELEMENT_ARRAY_BUFFER, oldSize*sizeof(INT), indices.get(oldSize, data.length));
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
    }
    
    public void clear()
    {
        vertices.clear();
        indices.clear();
    }
    
    private class Attribute
    {
        private String name;
        private int loc;
        private boolean normalized;
        private int size, offset;
    }
    
    public void setShader(GLShader shader)
    {
        this.shader = shader;
        shader.glUse();
        for (Attribute attrib : attribs)
            attrib.loc = shader.glGetAttribLocation(attrib.name);
        shader.glUnuse();
    }
    
    public void draw()
    {
        GL30.glBindVertexArray(vao);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, ibo);
        
        shader.glUse();
        
        for (Attribute attrib : attribs)
        {
            GL20.glEnableVertexAttribArray(attrib.loc);
            GL20.glVertexAttribPointer(attrib.loc, attrib.size,
                                       GL11.GL_FLOAT, attrib.normalized,
                                       stride, attrib.offset);
        }
        
        GL20.glVertexAttrib4f(ibo, vao, vao, vao, vao);
        
        GL11.glDrawElements(polyMode, indices.size(), GL11.GL_UNSIGNED_INT, 0);
        
        for (Attribute attrib : attribs) GL20.glDisableVertexAttribArray(attrib.loc);
        
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);
    }
    
    public void glDelete()
    {
        GL30.glDeleteVertexArrays(vao);
        GL15.glDeleteBuffers(vbo);
        GL15.glDeleteBuffers(ibo);
        
        vao = -1; vbo = -1; ibo = -1;
    }
    
    public boolean isDeleted()
    {
        return vao == -1;
    }
}
