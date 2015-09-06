package com.samrj.devil.gl;

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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.lwjgl.opengl.GL15;

/**
 * Vertex data abstract class. Used to keep track of vertex attributes, indices,
 * etc. and prepare vertex data for OpenGL rendering.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public abstract class VertexBuilder extends DGLObj implements VertexData
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
    
    private final ArrayList<AttWriter> attributes;
    private final Map<String, AttWriter> attMap;
    private int vertexSize;
    
    VertexBuilder()
    {
        attributes = new ArrayList<>(16);
        attMap = new HashMap<>();
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
    
    private <T extends AttWriter> T regAtt(String name, T att)
    {
        vertexSize += att.type.size*att.type.layers;
        attributes.add(att);
        attMap.put(name, att);
        return att;
    }
    
    private <T extends Bufferable> T regAtt(String name, AttributeType type, T obj)
    {
        regAtt(name, new BufferableAttribute(name, type, vertexSize, obj));
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
        return regAtt(name, new FloatAttribute(name, vertexSize));
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
        return regAtt(name, AttributeType.VEC2, new Vec2());
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
        return regAtt(name, AttributeType.VEC3, new Vec3());
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
        return regAtt(name, AttributeType.MAT2, new Mat2());
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
        return regAtt(name, AttributeType.MAT3, new Mat3());
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
        return regAtt(name, AttributeType.MAT4, new Mat4());
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
        return regAtt(name, new IntAttribute(name, vertexSize));
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
        return regAtt(name, AttributeType.VEC2I, new Vec2i());
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
        return regAtt(name, AttributeType.VEC3I, new Vec3i());
    }
    // </editor-fold>
    
    @Override
    public Iterable<Attribute> attributes()
    {
        return Collections.unmodifiableCollection(attributes);
    }
    
    @Override
    public Attribute getAttribute(String name)
    {
        return attMap.get(name);
    }
    
    @Override
    public int vertexSize()
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
        for (AttWriter attribute : attributes) attribute.write(buffer);
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
    
    final void bind()
    {
        if (!canBind()) throw new IllegalStateException("Vertex data not ready to bind.");
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo());
    }
    
    final void unbind()
    {
        if (!canBind()) throw new IllegalStateException("Vertex data not ready to unbind.");
        //Throwing this exception might seem really dumb, but it's there to
        //prevent even dumber programming mistakes.
        
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }
    
    @Override
    public abstract int vbo();
    
    @Override
    public abstract int ibo();
    
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
     * Releases any resources associated with this vertex data. Does not delete
     * any VAOs created by linking this data to shaders.
     */
    @Override
    final void delete()
    {
        if (getState() == State.DELETED) return;
        
        attributes.clear();
        attMap.clear();
        vertexSize = 0;
        onDelete();
    }
    
    abstract class AttWriter extends Attribute
    {
        private AttWriter(String name, AttributeType type, int offset)
        {
            super(name, type, offset);
        }
        
        abstract void write(ByteBuffer buffer);
    }
    
    public final class FloatAttribute extends AttWriter
    {
        public float x;
        
        private FloatAttribute(String name, int offset)
        {
            super(name, AttributeType.FLOAT, offset);
        }

        @Override
        void write(ByteBuffer buffer)
        {
            buffer.putFloat(x);
        }
    }
    
    public final class IntAttribute extends AttWriter
    {
        public int x;
        
        private IntAttribute(String name, int offset)
        {
            super(name, AttributeType.INT, offset);
        }

        @Override
        void write(ByteBuffer buffer)
        {
            buffer.putInt(x);
        }
    }
    
    private final class BufferableAttribute extends AttWriter
    {
        private final Bufferable obj;
        
        private BufferableAttribute(String name, AttributeType type, int offset, Bufferable obj)
        {
            super(name, type, offset);
            this.obj = obj;
        }

        @Override
        void write(ByteBuffer buffer)
        {
            obj.write(buffer);
        }
    }
}
