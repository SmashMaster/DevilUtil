package com.samrj.devil.gl;

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
    
    private final Type type;
    private ByteDataStream vBytes = null, iBytes = null;
    private final Attribute[] attributes;
    private final int vaoID, vboID, eboID;
    private final int usage;
    
    private boolean complete = false;
    private boolean destroyed = false;
    private int vertexCount = 0, indexCount = 0;
    
    public Mesh(Type type, int usage, Map<Integer, Attribute> attributes)
    {
        this.type = type;
        this.attributes = new Attribute[attributes.size()];
        attributes.values().toArray(this.attributes);
        
        this.usage = usage;
        vBytes = new ByteDataStream();
        if (type == Type.INDEXED) iBytes = new ByteDataStream();
        vaoID = GL30.glGenVertexArrays();
        vboID = GL15.glGenBuffers();
        eboID = (type == Type.INDEXED) ? GL15.glGenBuffers() : -1;
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
    
    void index(int index)
    {
        ensureIncomplete();
        if (type != Type.INDEXED) throw new IllegalStateException(
                "Cannot call index() for raw meshes!");
        if (++indexCount == 0) throw new RuntimeException("Index count overflow!");
        iBytes.writeInt(index);
    }
    
    void complete()
    {
        ensureIncomplete();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vBytes.toBuffer(), usage);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        
        if (type == Type.INDEXED)
        {
            GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, eboID);
            GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, iBytes.toBuffer(), usage);
            GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
        }
        
        vBytes = null;
        iBytes = null;
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
        
        switch (type)
        {
            case RAW:
                GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, vertexCount);
                break;
            case INDEXED:
                GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, eboID);
                GL11.glDrawElements(GL11.GL_TRIANGLES, indexCount, GL11.GL_UNSIGNED_INT, 0);
                GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
                break;
        }
        
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);
    }
    
    public void destroy()
    {
        if (destroyed) return;
        GL30.glDeleteVertexArrays(vaoID);
        GL15.glDeleteBuffers(vboID);
        if (type == Type.INDEXED) GL15.glDeleteBuffers(eboID);
        destroyed = true;
    }
}
