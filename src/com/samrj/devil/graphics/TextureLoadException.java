package com.samrj.devil.graphics;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class TextureLoadException extends RuntimeException
{
    public TextureLoadException()
    {
        super();
    }
    
    public TextureLoadException(String message)
    {
        super(message);
    }

    public TextureLoadException(Throwable cause)
    {
        super(cause);
    }

    public TextureLoadException(String message, Throwable cause)
    {
        super(message, cause);
    }
}