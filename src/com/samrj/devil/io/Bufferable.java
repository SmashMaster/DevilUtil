package com.samrj.devil.io;

import java.nio.ByteBuffer;

/**
 * Interface for anything than can be written to memory. (Everything)
 *
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
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
     * @return The size of this Bufferable, in elements.
     */
    public int bufferSize();
}
