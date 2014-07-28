package com.samrj.devil.res;

import java.io.IOException;

public class ResourceConflictException extends IOException
{
    public ResourceConflictException()
    {
        super();
    }
    
    public ResourceConflictException(String message)
    {
        super(message);
    }

    public ResourceConflictException(Throwable cause)
    {
        super(cause);
    }

    public ResourceConflictException(String message, Throwable cause)
    {
        super(message, cause);
    }
}