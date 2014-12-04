package com.samrj.devil.gl;

import com.samrj.devil.gl.util.ByteDataStream;
import java.util.Arrays;
import java.util.Set;
import org.lwjgl.opengl.*;

public class Mesh
{
    public static enum Type
    {
        RAW, INDEXED;
    }
    
    public static enum RenderMode
    {
        POINTS                  (GL11.GL_POINTS),
        LINE_STRIP              (GL11.GL_LINE_STRIP),
        LINE_LOOP               (GL11.GL_LINE_LOOP),
        LINES                   (GL11.GL_LINES),
        LINE_STRIP_ADJACENCY    (GL32.GL_LINE_STRIP_ADJACENCY),
        LINES_ADJACENCY         (GL32.GL_LINES_ADJACENCY),
        TRIANGLE_STRIP          (GL11.GL_TRIANGLE_STRIP),
        TRIANGLE_FAN            (GL11.GL_TRIANGLE_FAN),
        TRIANGLES               (GL11.GL_TRIANGLES),
        TRIANGLE_STRIP_ADJACENCY(GL32.GL_TRIANGLE_STRIP_ADJACENCY),
        TRIANGLES_ADJACENCY     (GL32.GL_TRIANGLES_ADJACENCY);
        
        public final int glEnum;
        
        private RenderMode(int glEnum)
        {
            this.glEnum = glEnum;
        }
    }
    
    public static enum Usage
    {
        GL_STREAM_DRAW (GL15.GL_STREAM_DRAW),
        GL_STATIC_DRAW (GL15.GL_STATIC_DRAW),
        GL_DYNAMIC_DRAW(GL15.GL_DYNAMIC_DRAW);
        
        public final int glEnum;
        
        private Usage(int glEnum)
        {
            this.glEnum = glEnum;
        }
    }
    
    private final Type type;
    private ByteDataStream vBytes = null, iBytes = null;
    private final Attribute[] attributes;
    private final int vaoID, vboID, eboID;
    private final int usage, renderMode;
    
    private boolean complete = false;
    private boolean destroyed = false;
    private int vertexCount = 0, indexCount = 0;
    
    Mesh(Type type, Usage usage, RenderMode renderMode, Attribute[] attributes)
    {
        this.type = type;
        this.attributes = Arrays.copyOf(attributes, attributes.length);
        
        this.usage = usage.glEnum;
        this.renderMode = renderMode.glEnum;
        vBytes = new ByteDataStream();
        if (type == Type.INDEXED) iBytes = new ByteDataStream();
        vaoID = GL30.glGenVertexArrays();
        vboID = GL15.glGenBuffers();
        eboID = (type == Type.INDEXED) ? GL15.glGenBuffers() : -1;
    }
    
    Mesh(Type type, Usage usage, RenderMode renderMode, Set<Attribute> attributes)
    {
        this(type, usage, renderMode, attributes.toArray(new Attribute[attributes.size()]));
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
        
        if (type == Type.INDEXED)
        {
            GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, eboID);
            GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, iBytes.toBuffer(), usage);
            GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
        }
        
        GL30.glBindVertexArray(vaoID);
        int stride = 0;
        if (attributes.length > 1) //If our vertices are not tightly packed
            for (Attribute att : attributes) stride += att.getByteLength();
        
        int offset = 0;
        for (Attribute att : attributes)
        {
            int attIndex = att.getIndex();
            VarType attType = att.getType();
            GL20.glEnableVertexAttribArray(attIndex);
            GL20.glVertexAttribPointer(attIndex, attType.size, attType.dataType.glEnum, false, stride, offset);
            
            offset += att.getByteLength();
        }
        
        GL30.glBindVertexArray(0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        
        vBytes = null;
        iBytes = null;
        complete = true;
    }
    
    void draw()
    {
        ensureAlive();
        if (!complete) throw new IllegalStateException("Mesh is not complete!");
        
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID);
        GL30.glBindVertexArray(vaoID);
        
        switch (type)
        {
            case RAW:
                GL11.glDrawArrays(renderMode, 0, vertexCount);
                break;
            case INDEXED:
                GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, eboID);
                GL11.glDrawElements(renderMode, indexCount, GL11.GL_UNSIGNED_INT, 0);
                GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
                break;
        }
        
        GL30.glBindVertexArray(0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }
    
    public void destroy()
    {
        if (destroyed) return;
        if (!complete)
        {
            vBytes = null;
            iBytes = null;
        }
        
        GL30.glDeleteVertexArrays(vaoID);
        GL15.glDeleteBuffers(vboID);
        if (type == Type.INDEXED) GL15.glDeleteBuffers(eboID);
        destroyed = true;
    }
}
