package com.samrj.devil.gl;

import java.nio.FloatBuffer;
import java.util.Map;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

public class Mesh
{
    public static enum Type
    {
        RAW, INDEXED;
    }
    
    private ByteDataStream vBytes = null;
    private final Attribute[] attributes;
    private final int vaoID, vboID;
    private final int usage;
    private boolean complete = false;
    private boolean destroyed = false;
    private int vertexCount = 0;
    
    public Mesh(Type type, int usage, Map<Integer, Attribute> attributes)
    {
        this.attributes = new Attribute[attributes.size()];
        attributes.values().toArray(this.attributes);
        
        this.usage = usage;
        vBytes = new ByteDataStream();
        vaoID = GL30.glGenVertexArrays();
        vboID = GL15.glGenBuffers();
    }
    
    private void ensureAlive()
    {
        if (destroyed) throw new IllegalStateException("Mesh is destroyed!");
    }
    
    private void ensureIncomplete()
    {
        if (complete) throw new IllegalStateException("Mesh is already complete!");
    }
    
    int vertex()
    {
        ensureIncomplete();
        for (Attribute attribute : attributes)
            attribute.writeTo(vBytes);
        return vertexCount++;
    }
    
    void complete()
    {
        ensureIncomplete();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vBytes.toBuffer(), usage);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        
        vBytes = null;
        complete = true;
    }
    
    void draw()
    {
        ensureAlive();
        if (!complete) throw new IllegalStateException("Mesh is not complete!");
        
        GL30.glBindVertexArray(vaoID);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID);
        
        int stride = 0;
        if (attributes.length > 1) //If our vertices are not tightly packed
            for (Attribute att : attributes) stride += att.getByteLength();
        
        int offset = 0;
        for (Attribute att : attributes)
        {
            int index = att.getIndex();
            int size = att.getSize();
            int type = att.getType();
            int dataType = Attribute.typeDataType(type);
            
            GL20.glEnableVertexAttribArray(index);
            GL20.glVertexAttribPointer(index, size, dataType, false, stride, offset);
            
            offset += att.getByteLength();
        }
        
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, vertexCount);
        
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);
    }
    
    public void destroy()
    {
        if (destroyed) return;
        GL30.glDeleteVertexArrays(vaoID);
        GL15.glDeleteBuffers(vboID);
        destroyed = true;
    }
}
