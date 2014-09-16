package com.samrj.devil.res;

import java.io.IOException;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2014 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
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
