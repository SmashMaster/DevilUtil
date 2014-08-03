package com.samrj.devil.graphics;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class ShaderCompileException extends Exception
{
    public ShaderCompileException()
    {
        super();
    }
    
    public ShaderCompileException(String message)
    {
        super(message);
    }

    public ShaderCompileException(Throwable cause)
    {
        super(cause);
    }

    public ShaderCompileException(String message, Throwable cause)
    {
        super(message, cause);
    }
}