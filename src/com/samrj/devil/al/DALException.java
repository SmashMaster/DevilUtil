package com.samrj.devil.al;

import static org.lwjgl.openal.AL10.*;

/**
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2019 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class DALException extends RuntimeException
{
    private static String getString(int errorCode)
    {
        switch (errorCode)
        {
            case AL_NO_ERROR: return "AL_NO_ERROR";
            case AL_INVALID_NAME: return "AL_INVALID_NAME";
            case AL_INVALID_ENUM: return "AL_INVALID_ENUM";
            case AL_INVALID_VALUE: return "AL_INVALID_VALUE";
            case AL_INVALID_OPERATION: return "AL_INVALID_OPERATION";
            case AL_OUT_OF_MEMORY: return "AL_OUT_OF_MEMORY";
            default: return "Unknown error.";
        }
    }
    
    private static String getDescription(int errorCode)
    {
        switch (errorCode)
        {
            case AL_NO_ERROR: return "There is no current error.";
            case AL_INVALID_NAME: return "Invalid name parameter.";
            case AL_INVALID_ENUM: return "Invalid parameter.";
            case AL_INVALID_VALUE: return "Invalid enum parameter value.";
            case AL_INVALID_OPERATION: return "Illegal call.";
            case AL_OUT_OF_MEMORY: return "Unable to allocate memory.";
            default: return "Unknown error.";
        }
    }
    
    DALException(int errorCode)
    {
        super("DevilUtil (DAL) - " + getString(errorCode) + ": " + getDescription(errorCode));
    }

    DALException(DALException e, String message)
    {
        super(message, e);
    }
}
