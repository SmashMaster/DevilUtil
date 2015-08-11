package com.samrj.devil.io;

import com.samrj.devil.math.Util;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import org.lwjgl.system.MemoryUtil;

/**
 * Memory-managed direct buffer class. Allows allocation and freeing of blocks
 * of memory via buddy memory allocation.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class Memory
{
    /**
     * Enum for different memory block types.
     */
    public static enum Type
    {
        FREE(true), PARENT(false), ALLOCATED(false), WASTED(true);

        public final boolean mergable;

        private Type(boolean mergable)
        {
            this.mergable = mergable;
        }
    }
    
    /**
     * The capacity of this memory block, in bytes.
     */
    public final int capacity;
    
    /**
     * The backing buffer for this memory block.
     */
    public final ByteBuffer buffer;
    
    /**
     * The root memory block.
     */
    public final Block root;
    
    /**
     * The address of this memory block. Inherently unsafe to use.
     */
    public final long address;
    
    /**
     * Creates a new direct byte buffer with the given capacity, and sets up
     * memory management for the buffer.
     * 
     * @param capacity The capacity, in bytes, of this managed memory.
     */
    public Memory(int capacity)
    {
        if (!Util.isPower2(capacity)) throw new IllegalArgumentException(
            "Main buffer capacity must be power of two.");
        
        this.capacity = capacity;
        buffer = ByteBuffer.allocateDirect(capacity).order(ByteOrder.nativeOrder());
        root = new Block(capacity);
        address = MemoryUtil.memAddress0(buffer);
    }
    
    /**
     * Allocates a new block of memory with the desired size. Will fail if there
     * is not enough memory, or the memory is too fragmented to allocate.
     * 
     * @param size The size of the block to allocate, in bytes.
     * @return A newly allocated block of memory.
     * @throws OutOfMemoryException If there is no free block of memory as large
     *                              as or larger than the desired size.
     *                          
     */
    public Block alloc(int size)
    {
        if (size <= 0) throw new IllegalArgumentException();
        
        Block parent = root.search(Util.nextPower2(size));
        
        if (parent == null) throw new OutOfMemoryException();
        
        if (parent.size == size)
        {
            parent.type = Type.ALLOCATED;
            return parent;
        }
        
        parent.type = Type.PARENT;
        parent.left = new Block(parent, parent.offset, size, Type.ALLOCATED);
        parent.right = new Block(parent, parent.offset + size, parent.size - size, Type.WASTED);
        return parent.left;
    }
    
    /**
     * Wraps the given array of bytes into a newly allocated block of memory.
     * 
     * @param array An array of bytes to wrap.
     * @return A newly allocated block of memory containing the given bytes.
     */
    public Block wrap(byte... array)
    {
        Block block = alloc(array.length);
        buffer.clear();
        buffer.position(block.offset);
        buffer.put(array);
        return block;
    }
    
    /**
     * Wraps the given array of shorts into a newly allocated block of memory.
     * 
     * @param array An array of shorts to wrap.
     * @return A newly allocated block of memory containing the given shorts.
     */
    public Block wraps(short... array)
    {
        Block block = alloc(array.length*2);
        buffer.clear();
        buffer.position(block.offset);
        for (short s : array) buffer.putShort(s);
        return block;
    }
    
    /**
     * Wraps the given array of ints into a newly allocated block of memory.
     * 
     * @param array An array of ints to wrap.
     * @return A newly allocated block of memory containing the given ints.
     */
    public Block wrapi(int... array)
    {
        Block block = alloc(array.length*4);
        buffer.clear();
        buffer.position(block.offset);
        for (int i : array) buffer.putInt(i);
        return block;
    }
    
    /**
     * Wraps the given array of floats into a newly allocated block of memory.
     * 
     * @param array An array of floats to wrap.
     * @return A newly allocated block of memory containing the given floats.
     */
    public Block wrapf(float... array)
    {
        Block block = alloc(array.length*4);
        buffer.clear();
        buffer.position(block.offset);
        for (float f : array) buffer.putFloat(f);
        return block;
    }
    
    /**
     * Writes the given Bufferable into a newly allocated block of memory.
     * 
     * @param obj A bufferable to write.
     * @return A newly allocated block of memory containing the bufferable.
     */
    public Block alloc(Bufferable obj)
    {
        Block block = alloc(obj.bufferSize());
        buffer.clear();
        buffer.position(block.offset);
        obj.write(buffer);
        return block;
    }
    
    /**
    * Class representing a block of memory within a ByteBuffer.
    */
    public final class Block
    {
        /**
         * The parent of this memory block.
         */
        public final Block parent;
        
        /**
         * The offset of this memory block, in bytes.
         */
        public final int offset;
        
        /**
         * The size of this memory block, in bytes.
         */
        public final int size;
        
        private Type type;
        private Block left, right;

        private Block(Block parent, int offset, int size, Type type)
        {
            this.parent = parent;
            this.offset = offset;
            this.size = size;
            this.type = type;
        }

        private Block(Block parent, int offset, int size)
        {
            this(parent, offset, size, Type.FREE);
        }

        private Block(int size)
        {
            this(null, 0, size, Type.FREE);
        }
        
        private void merge()
        {
            if (left.type.mergable && right.type.mergable)
            {
                left = null;
                right = null;
                type = Type.FREE;
                if (parent != null) parent.merge();
            }
        }
        
        private Block search(int blockSize)
        {
            if (type == Type.FREE)
            {
                if (size == blockSize) return this;
                else if (size > blockSize)
                {
                    int childSize = size >> 1;
                    type = Type.PARENT;
                    left = new Block(this, offset, childSize);
                    right = new Block(this, offset + childSize, childSize);
                }
            }

            if (type == Type.PARENT)
            {
                Block block = left.search(blockSize);
                if (block != null) return block;
                return right.search(blockSize);
            }

            return null;
        }
        
        /**
         * Frees this memory block, allowing the memory to be recycled.
         */
        public void free()
        {
            if (type != Type.ALLOCATED) throw new IllegalArgumentException();
            type = Type.FREE;
            if (parent != null) parent.merge();
        }
        
        /**
         * @return A new ByteBuffer representing this memory block.
         */
        public ByteBuffer read()
        {
            buffer.clear(); //Don't know if this is necessary
            ByteBuffer b = buffer.slice();
            b.order(ByteOrder.nativeOrder()); //One can only wonder...
            b.limit(offset + size);
            b.position(offset);
            b.mark();
            return b;
        }
        
        /**
         * @return The backing buffer for this memory block, *not* a copy.
         */
        public ByteBuffer readUnsafe()
        {
            buffer.limit(offset + size);
            buffer.position(offset);
            buffer.mark();
            return buffer;
        }
        
        /**
         * @return The first byte in this memory block.
         */
        public byte readByte()
        {
            return buffer.get(offset);
        }
        
        /**
         * @return The first short in this memory block.
         */
        public short readShort()
        {
            if (size < 2) throw new BufferOverflowException();
            return buffer.getShort(offset);
        }
        
        /**
         * @return The first integer in this memory block.
         */
        public int readInt()
        {
            if (size < 4) throw new BufferOverflowException();
            return buffer.getInt(offset);
        }
        
        /**
         * @return The first long in this memory block.
         */
        public long readLong()
        {
            if (size < 8) throw new BufferOverflowException();
            return buffer.getLong(offset);
        }
        
        /**
         * @return The first float in this memory block.
         */
        public float readFloat()
        {
            if (size < 4) throw new BufferOverflowException();
            return buffer.getFloat(offset);
        }
        
        /**
         * @return The first long in this memory block.
         */
        public double readDouble()
        {
            if (size < 8) throw new BufferOverflowException();
            return buffer.getDouble(offset);
        }
        
        /**
         * @return The first long in this memory block.
         */
        public char readChar()
        {
            if (size < 2) throw new BufferOverflowException();
            return buffer.getChar(offset);
        }
        
        /**
         * Returns the address for this memory block. Inherently unsafe.
         * 
         * @return The address for this memory block.
         */
        public long address()
        {
            return address + offset;
        }

        /**
         * @return The type of this memory block.
         */
        public Type type()
        {
            return type;
        }

        /**
         * @return The lower-addressed child of this block.
         */
        public Block left()
        {
            return left;
        }

        /**
         * @return The higher-addressed child of this block.
         */
        public Block right()
        {
            return right;
        }
    }
}
