package com.samrj.devil.graphics;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class UniformNotFoundException extends RuntimeException
{
    public UniformNotFoundException()
    {
        super();
    }
    
    public UniformNotFoundException(String message)
    {
        super(message);
    }

    public UniformNotFoundException(Throwable cause)
    {
        super(cause);
    }

    public UniformNotFoundException(String message, Throwable cause)
    {
        super(message, cause);
    }
}