package com.samrj.devil.graphics;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class LocationNotFoundException extends RuntimeException
{
    public LocationNotFoundException()
    {
        super();
    }
    
    public LocationNotFoundException(String message)
    {
        super(message);
    }

    public LocationNotFoundException(Throwable cause)
    {
        super(cause);
    }

    public LocationNotFoundException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
