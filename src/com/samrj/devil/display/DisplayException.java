package com.samrj.devil.display;

/**
 * RuntimeException for GLFW windowing errors.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 */
public class DisplayException extends RuntimeException
{
    public DisplayException()
    {
        super();
    }
    
    public DisplayException(String message)
    {
        super(message);
    }

    public DisplayException(Throwable cause)
    {
        super(cause);
    }

    public DisplayException(String message, Throwable cause)
    {
        super(message, cause);
    }
}