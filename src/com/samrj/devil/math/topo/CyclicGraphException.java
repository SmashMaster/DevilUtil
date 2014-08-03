package com.samrj.devil.math.topo;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class CyclicGraphException extends RuntimeException
{
    public CyclicGraphException()
    {
        super();
    }
    
    public CyclicGraphException(String message)
    {
        super(message);
    }

    public CyclicGraphException(Throwable cause)
    {
        super(cause);
    }

    public CyclicGraphException(String message, Throwable cause)
    {
        super(message, cause);
    }
}