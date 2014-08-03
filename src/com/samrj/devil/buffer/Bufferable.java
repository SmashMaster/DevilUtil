package com.samrj.devil.buffer;

/**
 * Used to put wrapped data (like vectors) into Buffers.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 * @param <T> The type of Buffer that this Bufferable can put data in.
 */

public interface Bufferable<T extends Buffer>
{
    /**
     * Puts this Bufferable's data inside a Buffer.
     * 
     * @param buf the Buffer to put this Bufferable's data in.
     */
    public void putIn(T buf);
    
    /**
     * Returns the number of elements that will place placed in a buffer by
     * <code>putIn()</code>
     */
    public int size();
}