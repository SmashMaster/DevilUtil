package com.samrj.devilgl;

import static com.samrj.devil.io.BufferUtil.memUtil;
import com.samrj.devil.io.Bufferable;
import com.samrj.devil.io.Memory;
import com.samrj.devil.io.Memory.Block;
import com.samrj.devil.math.Vec2;
import com.samrj.devil.math.Vec3;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

public class VertexData
{
    private static enum State
    {
        EMPTY, INCOMPLETE, COMPLETE, DESTROYED;
    }
    
    private final Memory memory;
    private final int maxVertices, maxIndices;
    private final ArrayList<Attribute> attributes;
    private final HashMap<String, Attribute> attMap;
    private State state;
    
    //Fields for incomplete state
    private int vertexSize;
    private Block vertexBlock, indexBlock;
    private ByteBuffer vertexBuffer, indexBuffer;
    private int numVertices, numIndices;
    
    //Fields for complete state
    private int glVertexArray;
    private int glVBO, glEBO;
    
    public VertexData(Memory memory, int maxVertices, int maxIndices)
    {
        if (memory == null) throw new NullPointerException();
        if (maxVertices < 1) throw new IllegalArgumentException();
        this.memory = memory;
        this.maxVertices = maxVertices;
        this.maxIndices = maxIndices;
        attributes = new ArrayList<>(16);
        attMap = new HashMap<>();
        state = State.EMPTY;
        vertexSize = 0;
    }
    
    public VertexData(Memory memory, int maxVertices)
    {
        this(memory, maxVertices, -1);
    }
    
    public VertexData(int maxVertices, int maxIndices)
    {
        this(memUtil, maxVertices, maxIndices);
    }
    
    public VertexData(int maxVertices)
    {
        this(memUtil, maxVertices, -1);
    }
    
    private void ensureState(State state)
    {
        if (this.state != state) throw new IllegalStateException(
                "Expected state '" + state + "', is actually " + this.state);
    }
    
    private void ensureAttNotReg(String name)
    {
        ensureState(State.EMPTY);
        if (attMap.containsKey(name)) throw new IllegalArgumentException(
                "Attribute '" + name + "' already registered.");
    }
    
    private <T extends Bufferable> T regAtt(T obj, String name, AttributeType type)
    {
        Attribute a = new BufferableAttribute(obj, type, vertexSize);
        vertexSize += type.size*type.layers;
        attributes.add(a);
        attMap.put(name, a);
        return obj;
    }
    
    /**
     * If one doesn't already exist, registers a new Vec2 attribute with the
     * given name. Then returns the attribute object corresponding with that
     * name.
     * 
     * @param name The name of the attribute to register.
     * @return The attribute object corresponding with the given name.
     * @throws java.lang.IllegalStateException If any vertices have been emitted
     *         by this.
     */
    public Vec2 vec2(String name)
    {
        ensureAttNotReg(name);
        return regAtt(new Vec2(), name, AttributeType.VEC2);
    }
    
    /**
     * If one doesn't already exist, registers a new Vec3 attribute with the
     * given name. Then returns the attribute object corresponding with that
     * name.
     * 
     * @param name The name of the attribute to register.
     * @return The attribute object corresponding with the given name.
     * @throws java.lang.IllegalStateException If any vertices have been emitted
     *         by this.
     */
    public Vec3 vec3(String name)
    {
        ensureAttNotReg(name);
        return regAtt(new Vec3(), name, AttributeType.VEC3);
    }
    
    /**
     * Finalizes this VertexData's attributes and allocates memory for vertex
     * and index data.
     */
    public void begin()
    {
        ensureState(State.EMPTY);
        state = State.INCOMPLETE;
        
        if (attributes.isEmpty()) throw new IllegalStateException(
                "Must have at least one registered attribute.");
        
        vertexBlock = memory.alloc(maxVertices*vertexSize);
        vertexBuffer = vertexBlock.read();
        
        if (maxIndices > 0)
        {
            indexBlock = memory.alloc(maxIndices*4);
            indexBuffer = indexBlock.read();
        }
    }
    
    /**
     * Emits a new vertex with all currently active attributes, and returns its
     * index.
     * 
     * @return The index of the emitted vertex.
     */
    public int vertex()
    {
        ensureState(State.INCOMPLETE);
        if (numVertices >= maxVertices) throw new IllegalStateException(
                "Vertex capacity reached.");
        
        int index = numVertices++;
        for (Attribute attribute : attributes) attribute.write(vertexBuffer);
        return index;
    }
    
    /**
     * Emits an index.
     * 
     * @param index The index to emit.
     */
    public void index(int index)
    {
        ensureState(State.INCOMPLETE);
        if (numIndices >= maxIndices) throw new IllegalStateException(
                "Index capacity reached.");
        if (index < 0 || index >= numIndices) throw new ArrayIndexOutOfBoundsException();
        
        indexBuffer.putInt(index);
    }
    
    /**
     * Completes this vertex data, sending it to the GPU so that it is ready to
     * be rendered.
     */
    public void end()
    {
        ensureState(State.INCOMPLETE);
        state = State.COMPLETE;
        
        glVertexArray = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(glVertexArray);
        
        glVBO = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, glVBO);
        GL15.nglBufferData(GL15.GL_ARRAY_BUFFER, numVertices*vertexSize, vertexBlock.address(), GL15.GL_STATIC_DRAW);
        vertexBlock.free();
        vertexBlock = null;
        vertexBuffer = null;

        if (maxIndices > 0)
        {
            glEBO = GL15.glGenBuffers();
            GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, glEBO);
            GL15.nglBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, numIndices*4, indexBlock.address(), GL15.GL_STATIC_DRAW);
            indexBlock.free();
            indexBlock = null;
            indexBuffer = null;
        }
    }
    
    /**
     * Binds this VertexData to the given shader program, enabling attributes it
     * shares in common with this.
     * 
     * @param shader The shader to bind with.
     */
    public void bind(ShaderProgram shader)
    {
        ensureState(State.COMPLETE);
        GL30.glBindVertexArray(glVertexArray);
        
        for (ShaderProgram.Attribute satt : shader.getAttributes())
        {
            Attribute att = attMap.get(satt.name);
            if (att != null && att.type == satt.type)
            {
                GL20.glEnableVertexAttribArray(satt.location);
                GL20.glVertexAttribPointer(satt.location,
                                           satt.type.components,
                                           satt.type.glComponent,
                                           false,
                                           vertexSize,
                                           att.offset);
            }
            else GL20.glDisableVertexAttribArray(satt.location);
        }
    }
    
    /**
     * Draws this vertex data. Assumes that the correct shader is bound.
     * 
     * @param mode An OpenGL primitive drawing mode.
     */
    public void draw(int mode)
    {
        ensureState(State.COMPLETE);
        GL30.glBindVertexArray(glVertexArray);
        if (maxIndices > 0) GL11.glDrawElements(mode, numVertices, GL11.GL_UNSIGNED_INT, 0);
        else GL11.glDrawArrays(mode, 0, numVertices);
    }
    
    /**
     * Releases any resources associated with this vertex data.
     */
    public void destroy()
    {
        attributes.clear();
        attMap.clear();
        
        if (state == State.INCOMPLETE)
        {
            vertexBlock.free();
            vertexBlock = null;
            vertexBuffer = null;
            
            if (maxIndices > 0)
            {
                indexBlock.free();
                indexBlock = null;
                indexBuffer = null;
            }
        }
        else if (state == State.COMPLETE)
        {
            GL30.glDeleteVertexArrays(glVertexArray);
            GL15.glDeleteBuffers(glVBO);
            if (maxIndices > 0) GL15.glDeleteBuffers(glEBO);
        }
        
        state = State.DESTROYED;
    }
    
    private abstract class Attribute
    {
        private final AttributeType type;
        private final int offset;
        
        private Attribute(AttributeType type, int offset)
        {
            this.type = type;
            this.offset = offset;
        }
        
        abstract void write(ByteBuffer buffer);
    }
    
    private class BufferableAttribute extends Attribute
    {
        private final Bufferable obj;
        
        private BufferableAttribute(Bufferable obj, AttributeType type, int offset)
        {
            super(type, offset);
            this.obj = obj;
        }

        @Override
        void write(ByteBuffer buffer)
        {
            obj.write(buffer);
        }
    }
}
