package com.samrj.devil.buffer;

import java.nio.Buffer;

/**
 * Abstract class for all 32 bit, 2 component vectors.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @param <BUFFER_TYPE> The type of buffer this object can be written to.
 */
public interface Bufferable<BUFFER_TYPE extends Buffer>
{
    /**
     * Writes this object's data into the given buffer.
     */
    public void writeTo(BUFFER_TYPE buffer);
    
    /**
     * Returns this size of this object's data in elements.
     */
    public int bufferSize();
}
