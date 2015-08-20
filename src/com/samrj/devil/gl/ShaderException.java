package com.samrj.devil.gl;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilGL/blob/master/LICENSE
 */
public class ShaderException extends RuntimeException
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
