package com.samrj.devil.io;

import com.samrj.devil.math.Util;
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
    private static Block allocSearch(Block parent, int blockSize)
    {
        if (parent.type == Block.Type.FREE)
        {
            if (parent.size == blockSize) return parent;
            else if (parent.size > blockSize)
            {
                int size = parent.size >> 1;
                parent.type = Block.Type.PARENT;
                parent.left = new Block(parent, parent.offset, size);
                parent.right = new Block(parent, parent.offset + size, size);
            }
        }
        
        if (parent.type == Block.Type.PARENT)
        {
            Block block = allocSearch(parent.left, blockSize);
            if (block != null) return block;
            return allocSearch(parent.right, blockSize);
        }
        
        return null;
    }
    
    private static void merge(Block block)
    {
        if (block != null && block.left.type.mergable && block.right.type.mergable)
        {
            block.left = null;
            block.right = null;
            block.type = Block.Type.FREE;
            merge(block.parent);
        }
    }
    
    private final ByteBuffer buffer;
    private final Block root;
    private final long address;
    
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
        
        buffer = ByteBuffer.allocateDirect(capacity).order(ByteOrder.nativeOrder());
        root = new Block(capacity);
        address = MemoryUtil.memAddress0(buffer);
    }
    
    /**
     * @return The capacity, in bytes, of this memory.
     */
    public int capacity()
    {
        return root.size;
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
        Block parent = allocSearch(root, Util.nextPower2(size));
        
        if (parent == null) throw new OutOfMemoryException();
        
        if (parent.size == size)
        {
            parent.type = Block.Type.ALLOCATED;
            return parent;
        }
        
        parent.type = Block.Type.PARENT;
        parent.left = new Block(parent, parent.offset, size, Block.Type.ALLOCATED);
        parent.right = new Block(parent, parent.offset + size, parent.size - size, Block.Type.WASTED);
        return parent.left;
    }
    
    /**
     * Frees the given block of memory
     * @param block 
     */
    public void free(Block block)
    {
        if (block.type != Block.Type.ALLOCATED) throw new IllegalArgumentException();
        block.type = Block.Type.FREE;
        merge(block.parent);
    }
    
    /**
     * Reads a block of memory as a new buffer. Its mark is set to the beginning
     * of the block, so you can use reset() to read the block multiple times.
     * 
     * @param block The block of memory to read.
     * @return A new ByteBuffer for the given block.
     */
    public ByteBuffer read(Block block)
    {
        ByteBuffer b = buffer.slice();
        b.order(ByteOrder.nativeOrder()); //One can only wonder...
        b.limit(block.offset + block.size);
        b.position(block.offset);
        b.mark();
        return b;
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
        read(block).put(array);
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
        read(block).asShortBuffer().put(array);
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
        read(block).asIntBuffer().put(array);
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
        read(block).asFloatBuffer().put(array);
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
        obj.write(read(block));
        return block;
    }
    
    /**
     * Returns the address of this memory block. Is inherently unsafe.
     * 
     * @return The address of this memory block.
     */
    public long address()
    {
        return address;
    }
    
    /**
     * Returns the address of the given memory block. Is inherently unsafe.
     * 
     * @param block The block to return the memory address of.
     * @return The address of the given memory block.
     */
    public long address(Block block)
    {
        return address + block.offset;
    }
    
    /**
     * @return The root block for this memory.
     */
    public Block root()
    {
        return root;
    }
}
