package com.samrj.devil.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Interface for mutable objects that can be written to and read from data
 * streams.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2019 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public interface DataStreamable
{
    /**
     * Reads data from the given input stream and sets this object's fields
     * accordingly.
     * 
     * @param in The {@code DataInputStream} to read data from.
     * @throws java.io.IOException Whenever the given stream fails to read.
     */
    public void read(DataInputStream in) throws IOException;
    
    /**
     * Writes data from this object's fields into the given output stream.
     * 
     * @param out The {@code DataOutputStream} to write data to.
     * @throws java.io.IOException Whenever the given stream fails to write.
     */
    public void write(DataOutputStream out) throws IOException;
}
