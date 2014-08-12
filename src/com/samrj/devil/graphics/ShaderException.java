package com.samrj.devil.graphics;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class ShaderException extends Exception
{
    public ShaderException()
    {
        super();
    }
    
    public ShaderException(String message)
    {
        super(message);
    }

    public ShaderException(Throwable cause)
    {
        super(cause);
    }

    public ShaderException(String message, Throwable cause)
    {
        super(message, cause);
    }
}