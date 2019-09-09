package com.samrj.devil.util;

import java.nio.ByteBuffer;
import org.lwjgl.system.MemoryStack;

/**
 * Interface for anything than can be written to memory. (Everything)
 *
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2019 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public interface Bufferable
{
    /**
     * Reads data from the given buffer and sets this object's fields accordingly.
     * @param buffer The buffer to read from.
     */
    public void read(ByteBuffer buffer);
    
    /**
     * Writes this object's data into the given buffer.
     * @param buffer The buffer to write into.
     */
    public void write(ByteBuffer buffer);
    
    /**
     * @return The size of this Bufferable, in bytes.
     */
    public int bufferSize();
    
    /**
     * Allocates a new ByteBuffer that can contain this bufferable, writes this
     * bufferable to it, and then prepares it for a series of read operations
     * and returns it.
     * 
     * @param stack The stack to allocate on.
     * @return A new ByteBuffer containing this bufferable.
     */
    public default ByteBuffer malloc(MemoryStack stack)
    {
        ByteBuffer buffer = stack.malloc(bufferSize());
        write(buffer);
        buffer.flip();
        return buffer;
    }
}
