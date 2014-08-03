package com.samrj.devil.geo2d;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class ConcavePolyException extends RuntimeException
{
    public ConcavePolyException()
    {
        super();
    }
    
    public ConcavePolyException(String message)
    {
        super(message);
    }

    public ConcavePolyException(Throwable cause)
    {
        super(cause);
    }

    public ConcavePolyException(String message, Throwable cause)
    {
        super(message, cause);
    }
}