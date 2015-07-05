package com.samrj.devil.io;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Interface for mutable objects that can be serialized manually to and from
 * data streams. The {@code read()} and {@code write()} methods should read and
 * write the exact same number of bytes, respectively.
 * 
 * @author SmashMaster
 * @copyright 2015 Samuel Johnson
 */
public interface Streamable
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
