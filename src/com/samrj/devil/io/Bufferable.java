package com.samrj.devil.io;

import java.nio.Buffer;

/**
 * Interface for anything than can be written to memory. (Everything)
 *
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 * @param <BUFFER_TYPE> The type of buffer this object can be written to.
 */
public interface Bufferable<BUFFER_TYPE extends Buffer>
{
    /**
     * Reads data from the given buffer and sets this object's fields accordingly.
     */
    public void read(BUFFER_TYPE buffer);
    
    /**
     * Writes this object's data into the given buffer.
     */
    public void write(BUFFER_TYPE buffer);
}
