package com.samrj.devilgl;

import static com.samrj.devil.io.BufferUtil.memUtil;
import com.samrj.devil.io.Bufferable;
import com.samrj.devil.io.Memory;
import com.samrj.devil.io.Memory.Block;
import com.samrj.devil.math.Mat2;
import com.samrj.devil.math.Mat3;
import com.samrj.devil.math.Mat4;
import com.samrj.devil.math.Vec2;
import com.samrj.devil.math.Vec2i;
import com.samrj.devil.math.Vec3;
import com.samrj.devil.math.Vec3i;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;

/**
 * Vertex builder for unmodifiable vertex data. Suitable for data that is built
 * once on CPU, uploaded to the GPU, then drawn many times.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilGL/blob/master/LICENSE
 */
public class VertexData
{
    public static enum State
    {
        /**
         * The vertex builder may register attributes, but it is not ready to
         * emit vertices or indices, or be drawn.
         */
        NEW,
        
        /**
         * The vertex builder is ready to emit new vertices and indices. It may
         * or may not be ready to draw. It can no longer register attributes.
         */
        READY,
        
        /**
         * The vertex builder is ready to be drawn, but may not emit any new
         * vertices or indices.
         */
        COMPLETE,
        
        /**
         * The vertex builder may not register new attributes, emit vertices or
         * indices, or be drawn. All associated resources have been released.
         */
        DESTROYED;
    }
    
    private final Memory memory;
    private final int maxVertices, maxIndices;
    private final ArrayList<Attribute> attributes;
    private final HashMap<String, Attribute> attMap;
    private final boolean streamed;
    private State state;
    
    //Fields for incomplete state
    private int vertexSize;
    private Block vertexBlock, indexBlock;
    private ByteBuffer vertexBuffer, indexBuffer;
    private int bufferedVerts, bufferedInds;
    private int uploadedVerts, uploadedInds;
    
    //Fields for complete state
    private VertexArrayObject vao;
    private int glVBO, glEBO;
    
    public VertexData(Memory memory, int maxVertices, int maxIndices, boolean streamed)
    {
        if (memory == null) throw new NullPointerException();
        if (maxVertices < 1) throw new IllegalArgumentException();
        this.memory = memory;
        this.maxVertices = maxVertices;
        this.maxIndices = maxIndices;
        attributes = new ArrayList<>(16);
        attMap = new HashMap<>();
        state = State.NEW;
        vertexSize = 0;
        this.streamed = streamed;
    }
    
    public VertexData(Memory memory, int maxVertices, boolean streamed)
    {
        this(memory, maxVertices, -1, streamed);
    }
    
    public VertexData(int maxVertices, int maxIndices, boolean streamed)
    {
        this(memUtil, maxVertices, maxIndices, streamed);
    }
    
    public VertexData(int maxVertices, boolean streamed)
    {
        this(memUtil, maxVertices, -1, streamed);
    }
    
    /**
     * @return Whether this vertex data is indexed.
     */
    public boolean isIndexed()
    {
        return maxIndices > 0;
    }
    
    /**
     * @return Whether this vertex data is streamed.
     */
    public boolean isStreamed()
    {
        return streamed;
    }
    
    private void ensureState(State state)
    {
        if (this.state != state) throw new IllegalStateException(
                "Expected state '" + state + "', is actually '" + this.state + "'");
    }
    
    private void ensureAttNotReg(String name)
    {
        ensureState(State.NEW);
        if (attMap.containsKey(name)) throw new IllegalArgumentException(
                "Attribute '" + name + "' already registered.");
    }
    
    private <T extends Attribute> T regAtt(String name, T att)
    {
        vertexSize += att.type.size*att.type.layers;
        attributes.add(att);
        attMap.put(name, att);
        return att;
    }
    
    private <T extends Bufferable> T regAtt(T obj, String name, AttributeType type)
    {
        regAtt(name, new BufferableAttribute(obj, type, vertexSize));
        return obj;
    }
    
    /**
     * Registers a new floating-point attribute for this vertex data.
     * 
     * @param name The name of the attribute to register.
     * @return A new attribute data object.
     */
    public FloatAttribute afloat(String name)
    {
        ensureAttNotReg(name);
        return regAtt(name, new FloatAttribute(vertexSize));
    }
    
    /**
     * Registers a 2d vector attribute for this vertex data.
     * 
     * @param name The name of the attribute to register.
     * @return A new attribute data object.
     */
    public Vec2 vec2(String name)
    {
        ensureAttNotReg(name);
        return regAtt(new Vec2(), name, AttributeType.VEC2);
    }
    
    /**
     * Registers a 3d vector attribute for this vertex data.
     * 
     * @param name The name of the attribute to register.
     * @return A new attribute data object.
     */
    public Vec3 vec3(String name)
    {
        ensureAttNotReg(name);
        return regAtt(new Vec3(), name, AttributeType.VEC3);
    }
    
    /**
     * Registers a 2x2 matrix attribute for this vertex data.
     * 
     * @param name The name of the attribute to register.
     * @return A new attribute data object.
     */
    public Mat2 mat2(String name)
    {
        ensureAttNotReg(name);
        return regAtt(new Mat2(), name, AttributeType.MAT2);
    }
    
    /**
     * Registers a 3x3 matrix attribute for this vertex data.
     * 
     * @param name The name of the attribute to register.
     * @return A new attribute data object.
     */
    public Mat3 mat3(String name)
    {
        ensureAttNotReg(name);
        return regAtt(new Mat3(), name, AttributeType.MAT3);
    }
    
    /**
     * Registers a 4x4 matrix attribute for this vertex data.
     * 
     * @param name The name of the attribute to register.
     * @return A new attribute data object.
     */
    public Mat4 mat4(String name)
    {
        ensureAttNotReg(name);
        return regAtt(new Mat4(), name, AttributeType.MAT4);
    }
    
    /**
     * Registers a new integer attribute for this vertex data.
     * 
     * @param name The name of the attribute to register.
     * @return A new attribute data object.
     */
    public IntAttribute aint(String name)
    {
        ensureAttNotReg(name);
        return regAtt(name, new IntAttribute(vertexSize));
    }
    
    /**
     * Registers a 2d integer vector attribute for this vertex data.
     * 
     * @param name The name of the attribute to register.
     * @return A new attribute data object.
     */
    public Vec2i vec2i(String name)
    {
        ensureAttNotReg(name);
        return regAtt(new Vec2i(), name, AttributeType.VEC2I);
    }
    
    /**
     * Registers a 3d integer vector attribute for this vertex data.
     * 
     * @param name The name of the attribute to register.
     * @return A new attribute data object.
     */
    public Vec3i vec3i(String name)
    {
        ensureAttNotReg(name);
        return regAtt(new Vec3i(), name, AttributeType.VEC3I);
    }
    
    private void initGLBuffers()
    {
        vao = VertexArrayObject.gen();
        vao.bind();
        
        glVBO = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, glVBO);
        int vertCapacity = streamed ? maxVertices : bufferedVerts;
        int usage = streamed ? GL15.GL_DYNAMIC_DRAW : GL15.GL_STATIC_DRAW;
        GL15.nglBufferData(GL15.GL_ARRAY_BUFFER, vertCapacity*vertexSize, vertexBlock.address(), usage);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        
        if (!streamed)
        {
            vertexBlock.free();
            vertexBlock = null;
            vertexBuffer = null;
        }
        
        if (maxIndices > 0)
        {
            glEBO = GL15.glGenBuffers();
            vao.bindElementArrayBuffer(glEBO);
            int indCapacity = streamed ? maxIndices : bufferedInds;
            GL15.nglBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indCapacity*4, indexBlock.address(), usage);
            
            if (!streamed)
            {
                indexBlock.free();
                indexBlock = null;
                indexBuffer = null;
            }
        }
        
        vao.unbind();
    }
    
    /**
     * Finalizes this VertexData's attributes and allocates memory for vertex
     * and index data.
     */
    public void begin()
    {
        ensureState(State.NEW);
        if (attributes.isEmpty()) throw new IllegalStateException(
                "Must have at least one registered attribute.");
        
        vertexBlock = memory.alloc(maxVertices*vertexSize);
        vertexBuffer = vertexBlock.read();
        
        if (maxIndices > 0)
        {
            indexBlock = memory.alloc(maxIndices*4);
            indexBuffer = indexBlock.read();
        }
        
        if (streamed) initGLBuffers();
        state = State.READY;
    }
    
    /**
     * Emits a new vertex with all currently active attributes, and returns its
     * index.
     * 
     * @return The index of the emitted vertex.
     */
    public int vertex()
    {
        ensureState(State.READY);
        
        if (bufferedVerts >= maxVertices) throw new IllegalStateException(
                "Vertex capacity reached.");
        
        int index = bufferedVerts++;
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
        ensureState(State.READY);
        
        if (bufferedInds >= maxIndices) throw new IllegalStateException(
                "Index capacity reached.");
        if (index < 0 || index >= bufferedVerts) throw new ArrayIndexOutOfBoundsException();
        
        bufferedInds++;
        indexBuffer.putInt(index);
    }
    
    /**
     * Clears this vertex data.
     */
    public void clear()
    {
        ensureState(State.READY);
        bufferedVerts = 0;
        bufferedInds = 0;
    }
    
    /**
     * Completes this vertex data, sending it to the GPU so that it is ready to
     * be rendered.
     */
    public void upload()
    {
        ensureState(State.READY);
        
        if (streamed)
        {
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, glVBO);
            GL15.nglBufferSubData(GL15.GL_ARRAY_BUFFER, 0, bufferedVerts*vertexSize, vertexBlock.address());
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
            vertexBuffer.reset();
            uploadedVerts = bufferedVerts;
            
            if (maxIndices > 0)
            {
                GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, glEBO);
                GL15.nglBufferSubData(GL15.GL_ELEMENT_ARRAY_BUFFER, 0, bufferedInds*4, indexBlock.address());
                GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
                indexBuffer.reset();
                uploadedInds = bufferedInds;
            }
        }
        else
        {
            initGLBuffers();
            uploadedVerts = bufferedVerts;
            uploadedInds = bufferedInds;
            state = State.COMPLETE;
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
        if (streamed) ensureState(State.READY);
        else ensureState(State.COMPLETE);
        
        vao.bind();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, glVBO);
        
        for (ShaderProgram.Attribute satt : shader.getAttributes())
        {
            AttributeType type  = satt.type;
            Attribute att = attMap.get(satt.name);
            
            if (att != null && att.type == type) for (int layer=0; layer<type.layers; layer++)
            {
                int location = satt.location + layer;
                vao.enableVertexAttribArray(location);
                vao.vertexAttribPointer(location,
                                        satt.type.components,
                                        satt.type.glComponent,
                                        false,
                                        vertexSize,
                                        att.offset + layer*type.size);
            }
            else vao.disableVertexAttribArray(satt.location);
        }
        
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        vao.unbind();
    }
    
    /**
     * Draws this vertex data. Assumes that the correct shader is bound.
     * 
     * @param mode An OpenGL primitive drawing mode.
     */
    public void draw(int mode)
    {
        if (streamed) ensureState(State.READY);
        else ensureState(State.COMPLETE);
        
        vao.bind();
        if (maxIndices <= 0) GL11.glDrawArrays(mode, 0, uploadedVerts);
        else GL11.glDrawElements(mode, uploadedInds, GL11.GL_UNSIGNED_INT, 0);
        vao.unbind();
    }
    
    /**
     * Releases any resources associated with this vertex data.
     */
    public void destroy()
    {
        attributes.clear();
        attMap.clear();
        
        if (streamed || state == State.READY)
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
        
        if ((streamed && state == State.READY) || (!streamed && state == State.COMPLETE))
        {
            vao.delete();
            GL15.glDeleteBuffers(glVBO);
            if (maxIndices > 0) GL15.glDeleteBuffers(glEBO);
        }
        
        state = State.DESTROYED;
    }
    
    private abstract class Attribute
    {
        protected final AttributeType type;
        private final int offset;
        
        private Attribute(AttributeType type, int offset)
        {
            this.type = type;
            this.offset = offset;
        }
        
        abstract void write(ByteBuffer buffer);
    }
    
    public final class FloatAttribute extends Attribute
    {
        public float x;
        
        private FloatAttribute(int offset)
        {
            super(AttributeType.FLOAT, offset);
        }

        @Override
        void write(ByteBuffer buffer)
        {
            buffer.putFloat(x);
        }
    }
    
    public final class IntAttribute extends Attribute
    {
        public int x;
        
        private IntAttribute(int offset)
        {
            super(AttributeType.INT, offset);
        }

        @Override
        void write(ByteBuffer buffer)
        {
            buffer.putInt(x);
        }
    }
    
    private final class BufferableAttribute extends Attribute
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
