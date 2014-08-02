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

public class GLVertexPool
{
    private FloatBuffer vertices;
    private int vertexSize;
    
    private IntBuffer indices;
    
    private GLShader shader;
    private int vao, vbo, ibo;
    private ArrayList<Attribute> attribs;
    private int stride = 0;
    private int polyMode;
    
    public GLVertexPool(GLShader shader, int vertCapacity, int indCapacity, int vertexSize, int polyMode)
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
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertices.capacity()*sizeof(FLOAT), GL15.GL_DYNAMIC_DRAW);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, ibo);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indices.capacity()*sizeof(INT), GL15.GL_DYNAMIC_DRAW);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
    }
    
    public void addAttribute(String name, boolean normalized, int size)
    {
        int loc = GL20.glGetAttribLocation(shader.id(), name);
        if (loc < 0) return;
        
        Attribute attrib = new Attribute();
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
    
    private class Attribute
    {
        private int loc;
        private boolean normalized;
        private int size, offset;
    }
    
    public void setShader(GLShader shader)
    {
        this.shader = shader;
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
    
    public void delete()
    {
        GL30.glDeleteVertexArrays(vao);
        GL15.glDeleteBuffers(vbo);
        GL15.glDeleteBuffers(ibo);
    }
}