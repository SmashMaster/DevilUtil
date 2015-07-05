package com.samrj.devil.math;

/**
 * Runtime exception thrown when an attempt is made to invert a singular matrix.
 * 
 * @author SmashMaster
 * @copyright 2015 Samuel Johnson
 */
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
