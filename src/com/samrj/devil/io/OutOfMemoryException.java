package com.samrj.devil.io;

/**
 * Exception thrown when unable to allocate a memory block.
 *
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class OutOfMemoryException extends RuntimeException
{
    public OutOfMemoryException()
    {
        super();
    }
    
    public OutOfMemoryException(String message)
    {
        super(message);
    }

    public OutOfMemoryException(Throwable cause)
    {
        super(cause);
    }

    public OutOfMemoryException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
