package com.samrj.devil.res;

import java.io.IOException;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
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
