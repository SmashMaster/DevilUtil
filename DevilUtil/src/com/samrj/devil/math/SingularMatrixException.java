package com.samrj.devil.math;

public class SingularMatrixException extends RuntimeException
{
    public SingularMatrixException()
    {
        super();
    }
    
    public SingularMatrixException(String message)
    {
        super(message);
    }

    public SingularMatrixException(Throwable cause)
    {
        super(cause);
    }

    public SingularMatrixException(String message, Throwable cause)
    {
        super(message, cause);
    }
}