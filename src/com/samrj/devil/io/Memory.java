package com.samrj.devil.io;

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
     * The capacity of this memory block, in bytes.
     */
    public final int capacity;
    
    /**
     * The backing buffer for this memory block.
     */
    public final ByteBuffer buffer;
    
    /**
     * The address of this memory block. Inherently unsafe to use.
     */
    public final long address;
    
    /**
     * The first block in this memory.
     */
    public final Block root;
    
    /**
     * Creates a new direct byte buffer with the given capacity, and sets up
     * memory management for the buffer.
     * 
     * @param capacity The capacity, in bytes, of this managed memory.
     */
    public Memory(int capacity)
    {
        if (capacity <= 0) throw new IllegalArgumentException();
        
        this.capacity = capacity;
        buffer = ByteBuffer.allocateDirect(capacity).order(ByteOrder.nativeOrder());
        address = MemoryUtil.memAddress0(buffer);
        root = new Block(capacity);
    }
    
    //Single merge
    private void merge(Block block)
    {
        if (block.allocated) return;
        while (block.next != null && !block.next.allocated)
        {
            block.size += block.next.size;
            block.next = block.next.next;
        }
    }
    
    //Total merge
    private void merge()
    {
        Block block = root;
        
        while (block != null)
        {
            merge(block);
            block = block.next;
        }
    }
    
    /**
     * Allocates a new block of memory with the desired size. Will fail if there
     * is not enough memory, or the memory is too fragmented to allocate.
     * 
     * @param size The size of the block to allocate, in bytes.
     * @return A newly allocated block of memory.
     * @throws OutOfMemoryException If there is no free block of memory as large
     *                              as or larger than the desired size.
     */
    public Block alloc(int size)
    {
        if (size <= 0) throw new IllegalArgumentException();
        
        Block block = root;
        while (block != null && (block.allocated || block.size < size))
            block = block.next;
        if (block == null) throw new OutOfMemoryException();
        
        block.allocated = true;
        
        if (block.size != size)
        {
            Block remaining = new Block(block.offset + size, block.size - size);
            remaining.next = block.next;
            merge(remaining);
            block.next = remaining;
            block.size = size;
        }
        
        return block;
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
        private int offset, size;
        private boolean allocated;
        private Block next;

        private Block(int offset, int size, boolean allocated)
        {
            this.offset = offset;
            this.size = size;
            this.allocated = allocated;
        }

        private Block(int offset, int size)
        {
            this(offset, size, false);
        }

        private Block(int size)
        {
            this(0, size, false);
        }
        
        /**
         * Frees this memory block, allowing the memory to be recycled.
         */
        public void free()
        {
            if (!allocated) throw new IllegalStateException("Already free.");
            allocated = false;
            merge();
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
         * @return The offset, in bytes, of this block.
         */
        public int offset()
        {
            return offset;
        }
        
        /**
         * @return The size, in bytes, of this block.
         */
        public int size()
        {
            return size;
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
         * @return Whether this block has been allocated or not.
         */
        public boolean allocated()
        {
            return allocated;
        }
        
        /**
         * @return The next block, or null if no such block exists.
         */
        public Block next()
        {
            return next;
        }
        
        @Override
        public String toString()
        {
            return "offs: " + offset + " size: " + size + " allc: " + allocated;
        }
    }
}
