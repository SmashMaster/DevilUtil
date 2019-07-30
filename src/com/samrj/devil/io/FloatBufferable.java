package com.samrj.devil.io;

import java.nio.FloatBuffer;
import org.lwjgl.system.MemoryStack;

/**
 * Interface for anything than can be written to memory. (Everything)
 *
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2019 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public interface FloatBufferable extends Bufferable
{
    /**
     * Reads data from the given buffer and sets this object's fields accordingly.
     * @param buffer The buffer to read from.
     */
    public void read(FloatBuffer buffer);
    
    /**
     * Writes this object's data into the given buffer.
     * @param buffer The buffer to write into.
     */
    public void write(FloatBuffer buffer);
    
    /**
     * Allocates a new FloatBuffer that can contain this bufferable, writes this
     * bufferable to it, and then prepares it for a series of read operations
     * and returns it.
     * 
     * @param stack The stack to allocate on.
     * @return A new FloatBuffer containing this bufferable.
     */
    public default FloatBuffer mallocFloat(MemoryStack stack)
    {
        FloatBuffer buffer = stack.mallocFloat(bufferSize() >> 2);
        write(buffer);
        buffer.flip();
        return buffer;
    }
}
