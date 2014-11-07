package com.samrj.devil.gl;

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
