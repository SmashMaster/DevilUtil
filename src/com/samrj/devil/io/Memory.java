package com.samrj.devil.io;

import com.samrj.devil.math.Util;
import com.samrj.devil.util.SortedArray;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Comparator;
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
    
    private Block firstBlock;
    private final SortedArray<Block> blockArray;
    
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
        address = MemoryUtil.memAddress0(buffer);
        firstBlock = new Block(capacity);
        blockArray = new SortedArray<>(32, new BlockSizeComparator());
        blockArray.insert(firstBlock);
    }
    
    /**
     * Returns the index of the first block larger than or equal to the given
     * size.
     * 
     * @param size The minimum size of block to search for.
     * @return The index of the first block larger than the given size.
     */
    private int search(int size)
    {
        int low = 0;
        int high = blockArray.size() - 1;
        
        while (low <= high)
        {
            int mid = (low + high) >>> 1;
            int midValue = blockArray.get(mid).size;
            
            if (midValue < size) low = mid + 1;
            else if (midValue > size) high = mid - 1;
            else return mid;
        }
        
        return low;
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
        
        int index = search(size);
        if (index >= blockArray.size()) throw new OutOfMemoryException();
        
        Block block = blockArray.get(index);
        if (block.size == size)
        {
            block.allocated = true;
            blockArray.remove(index);
            return block;
        }
        else
        {
            Block out = new Block(block.offset, size, true);
            out.prev = block.prev;
            out.next = block;
            
            if (out.prev != null) out.prev.next = out;
            else firstBlock = out;
            
            block.prev = out;
            block.offset += size;
            block.size -= size;
            
            if (block.prev != out || out.next != block) throw new Error("how?");
            
            blockArray.resort();
            return out;
        }
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
     * @return The first block in this memory.
     */
    public Block firstBlock()
    {
        return firstBlock;
    }
    
    /**
    * Class representing a block of memory within a ByteBuffer.
    */
    public final class Block
    {
        private int offset, size;
        private boolean allocated;
        private Block prev, next;

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
            
            if (next != null && !next.allocated)
            {
                size += next.size;
                blockArray.remove(next);
                next = next.next;
                
                if (next != null) next.prev = this;
            }
            
            if (prev == null)
            {
                firstBlock = this;
                blockArray.insert(this);
            }
            else if (!prev.allocated)
            {
                prev.size += size;
                prev.next = next;
                
                if (next != null) next.prev = prev;
            }
            else blockArray.insert(this);
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
         * @return The previous block, or null if no such block exists.
         */
        public Block prev()
        {
            return prev;
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
    
    private final class BlockSizeComparator implements Comparator<Block>
    {
        @Override
        public int compare(Block o1, Block o2)
        {
            if (o1 == o2) return 0;
            if (o1 == null) return 1;
            if (o2 == null) return -1;
            
            int sizeDiff = o1.size - o2.size;
            if (sizeDiff == 0) //Same size, must have consistent order.
                return System.identityHashCode(o1) - System.identityHashCode(o2);
            return sizeDiff;
        }
    }
}
