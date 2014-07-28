package com.samrj.devil.res;

import java.io.IOException;

public class AbsoluteFileException extends IOException
{
    public AbsoluteFileException()
    {
        super();
    }
    
    public AbsoluteFileException(String message)
    {
        super(message);
    }

    public AbsoluteFileException(Throwable cause)
    {
        super(cause);
    }

    public AbsoluteFileException(String message, Throwable cause)
    {
        super(message, cause);
    }
}