package com.samrj.devil.math.topo;

public class CyclicGraphException extends RuntimeException
{
    public CyclicGraphException()
    {
        super();
    }
    
    public CyclicGraphException(String message)
    {
        super(message);
    }

    public CyclicGraphException(Throwable cause)
    {
        super(cause);
    }

    public CyclicGraphException(String message, Throwable cause)
    {
        super(message, cause);
    }
}