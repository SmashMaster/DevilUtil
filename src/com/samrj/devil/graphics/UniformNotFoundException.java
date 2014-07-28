package com.samrj.devil.graphics;

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