/*
 * Copyright (c) 2022 Sam Johnson
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.samrj.devil.gl;

import com.samrj.devil.math.*;
import com.samrj.devil.util.Bufferable;

import java.nio.ByteBuffer;
import java.util.*;

import static org.lwjgl.system.MemoryUtil.memAddress0;
import static org.lwjgl.system.MemoryUtil.memByteBuffer;

/**
 * Vertex data abstract class. Used to keep track of vertex attributes, indices,
 * etc. and prepare vertex data for OpenGL rendering.
 * 
 * @author Samuel Johnson (SmashMaster)
 */
public abstract class VertexBuilder extends DGLObj implements VertexData
{
    public static ByteBuffer viewBuffer(ByteBuffer buffer)
    {
        if (buffer == null) return null;
        ByteBuffer viewBuffer = memByteBuffer(memAddress0(buffer), buffer.capacity());
        viewBuffer.position(buffer.position());
        viewBuffer.limit(buffer.limit());
        return viewBuffer;
    }

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
        DELETED;
    }
    
    private final ArrayList<AttWriter> attributes;
    private final List<Attribute> attributesUnmod;
    private final Map<String, AttWriter> attMap;
    private int vertexSize;
    
    VertexBuilder()
    {
        attributes = new ArrayList<>(16);
        attributesUnmod = Collections.unmodifiableList(attributes);
        attMap = new HashMap<>();
        vertexSize = 0;
    }

    public abstract ByteBuffer newVertexBufferView();
    public abstract ByteBuffer newIndexBufferView();

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
        vertexSize += att.getType().size*att.getType().layers;
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
     * Registers a 3d vector attribute for this vertex data.
     * 
     * @param name The name of the attribute to register.
     * @return A new attribute data object.
     */
    public final Vec4 vec4(String name)
    {
        ensureAttNotReg(name);
        return regAtt(name, AttributeType.VEC4, new Vec4());
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
    public List<Attribute> attributes()
    {
        return attributesUnmod;
    }
    
    @Override
    public Attribute getAttribute(String name)
    {
        return attMap.get(name);
    }
    
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
//        if (attributes.isEmpty()) throw new IllegalStateException(
//                "Must have at least one registered attribute.");
        
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
     * Emits each of the given indices, in the order they are given.
     * 
     * @param array An array of indices to emit.
     */
    public final void indices(int... array)
    {
        for (int i : array) index(i);
    }
    
    @Override
    public abstract int vbo();
    
    @Override
    public abstract int ibo();
    
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
    
    abstract class AttWriter implements Attribute
    {
        private final String name;
        private final AttributeType type;
        private final int offset;
        
        private AttWriter(String name, AttributeType type, int offset)
        {
            this.name = name;
            this.type = type;
            this.offset = offset;
        }

        @Override
        public String getName()
        {
            return name;
        }
        
        @Override
        public AttributeType getType()
        {
            return type;
        }
        
        @Override
        public int getStride()
        {
            return vertexSize;
        }
        
        @Override
        public int getOffset()
        {
            return offset;
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
