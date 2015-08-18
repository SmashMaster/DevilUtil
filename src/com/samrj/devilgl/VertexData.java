package com.samrj.devilgl;

import com.samrj.devil.io.Bufferable;
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
import java.util.Map;
import org.lwjgl.opengl.GL15;

/**
 * Vertex data abstract class. Used to keep track of vertex attributes, indices,
 * etc. and prepare vertex data for OpenGL rendering.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilGL/blob/master/LICENSE
 */
public abstract class VertexData
{
    public static enum State
    {
        /**
         * The vertex builder may register attributes, but it is not ready to
         * emit vertices or indices, or be drawn.
         *//**
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
        DELETED;
    }
    
    private final ArrayList<Attribute> attributes;
    private final Map<String, Attribute> attMap;
    private final VAO vao;
    private int vertexSize;
    
    VertexData()
    {
        attributes = new ArrayList<>(16);
        attMap = new HashMap<>();
        vao = DGL.genVAO();
        vertexSize = 0;
    }
    
    /**
     * @return The state of this vertex data.
     */
    public abstract State getState();
    
    final void ensureState(State state)
    {
        State curState = getState();
        if (curState != state) throw new IllegalStateException(
                "Expected state '" + state + "', is actually '" + curState + "'");
    }
    
    // <editor-fold defaultstate="collapsed" desc="Attribute registration">
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
    public final FloatAttribute afloat(String name)
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
    public final Vec2 vec2(String name)
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
    public final Vec3 vec3(String name)
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
    public final Mat2 mat2(String name)
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
    public final Mat3 mat3(String name)
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
    public final Mat4 mat4(String name)
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
    public final IntAttribute aint(String name)
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
    public final Vec2i vec2i(String name)
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
    public final Vec3i vec3i(String name)
    {
        ensureAttNotReg(name);
        return regAtt(new Vec3i(), name, AttributeType.VEC3I);
    }
    // </editor-fold>
    
    int getVertexSize()
    {
        return vertexSize;
    }
    
    /**
     * Called when this vertex data finalizes its attributes and prepares to
     * emit vertex data.
     */
    abstract void onBegin();
    
    /**
     * Finalizes this vertex data's attributes and allocates memory for vertex
     * and index data.
     */
    public final void begin()
    {
        ensureState(State.NEW);
        if (attributes.isEmpty()) throw new IllegalStateException(
                "Must have at least one registered attribute.");
        
        onBegin();
    }
    
    final void bufferVertex(ByteBuffer buffer)
    {
        for (Attribute attribute : attributes) attribute.write(buffer);
    }
    
    /**
     * Emits a new vertex with all active attributes, and returns its index.
     * 
     * @return The index of the emitted vertex.
     */
    public abstract int vertex();
    
    /**
     * Emits an index.
     * 
     * @param index The index to emit.
     */
    public abstract void index(int index);
    
    /**
     * @return Whether this vertex data can be bound--whether it has memory
     *         allocated on the GPU and is ready to upload data.
     */
    abstract boolean canBind();
    
    abstract int getVBO();
    abstract int getEBO();
    
    final void bind()
    {
        if (!canBind()) throw new IllegalStateException("Vertex data not ready to bind.");
        DGL.bindVAO(vao);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, getVBO());
        vao.bindElementArrayBuffer(getEBO());
    }
    
    final void unbind()
    {
        if (!canBind()) throw new IllegalStateException("Vertex data not ready to unbind.");
        //Throwing this exception might seem really dumb, but it's there to
        //prevent even dumber programming mistakes.
        
        DGL.bindVAO(null);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }
    
    /**
     * Links this VertexData to the given shader program, enabling attributes it
     * shares in common with this.
     * 
     * @param shader The shader to bind with.
     */
    public void link(ShaderProgram shader)
    {
        if (DGL.currentData() != this) throw new IllegalStateException(
                "Vertex data must be bound to link shader.");
        
        for (ShaderProgram.Attribute satt : shader.getAttributes())
        {
            AttributeType type  = satt.type;
            Attribute att = attMap.get(satt.name);
            
            if (att != null && att.type == type) for (int layer=0; layer<type.layers; layer++)
            {
                int location = satt.location + layer;
                vao.enableVertexAttribArray(location);
                vao.vertexAttribPointer(location,
                                        type.components,
                                        type.glComponent,
                                        false,
                                        vertexSize,
                                        att.offset + layer*type.size);
            }
            else vao.disableVertexAttribArray(satt.location);
        }
    }
    
    /**
     * Draws this vertex data.
     * 
     * @param mode An OpenGL primitive drawing mode.
     */
    abstract void draw(int mode);
    
    /**
     * Called when deleting this vertex data.
     */
    abstract void onDelete();
    
    /**
     * Releases any resources associated with this vertex data.
     */
    final void delete()
    {
        if (getState() == State.DELETED) return;
        
        DGL.deleteVAO(vao);
        attributes.clear();
        attMap.clear();
        vertexSize = 0;
        onDelete();
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
