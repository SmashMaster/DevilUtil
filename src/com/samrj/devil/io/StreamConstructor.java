package com.samrj.devil.io;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Interface for functions which can construct objects from input streams.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @param <TYPE> The type of object this function constructs.
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
@FunctionalInterface
public interface StreamConstructor<TYPE>
{
    TYPE construct(DataInputStream in) throws IOException;
}
