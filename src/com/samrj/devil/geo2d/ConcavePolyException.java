package com.samrj.devil.geo2d;

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