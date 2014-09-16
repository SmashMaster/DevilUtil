package com.samrj.devil.math;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
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
