package com.samrj.devil.graphics;

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