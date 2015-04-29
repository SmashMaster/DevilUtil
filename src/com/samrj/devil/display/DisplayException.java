package com.samrj.devil.display;

import org.lwjgl.glfw.GLFW;

/**
 * RuntimeException subclass for GLFW windowing and context errors.
 * 
 * See http://www.glfw.org/docs/latest/group__errors.html for more detail on
 * individual error types.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 */
public class DisplayException extends RuntimeException
{
    /**
     * Returns the error name for the given GLFW error code.
     */
    public static final String getName(int errorCode)
    {
        switch (errorCode)
        {
            case GLFW.GLFW_NOT_INITIALIZED: return "GLFW_NOT_INITIALIZED";
            case GLFW.GLFW_NO_CURRENT_CONTEXT: return "GLFW_NO_CURRENT_CONTEXT";
            case GLFW.GLFW_INVALID_ENUM: return "GLFW_INVALID_ENUM";
            case GLFW.GLFW_INVALID_VALUE: return "GLFW_INVALID_VALUE";
            case GLFW.GLFW_OUT_OF_MEMORY: return "GLFW_OUT_OF_MEMORY";
            case GLFW.GLFW_API_UNAVAILABLE: return "GLFW_API_UNAVAILABLE";
            case GLFW.GLFW_VERSION_UNAVAILABLE: return "GLFW_VERSION_UNAVAILABLE";
            case GLFW.GLFW_PLATFORM_ERROR: return "GLFW_PLATFORM_ERROR";
            case GLFW.GLFW_FORMAT_UNAVAILABLE: return "GLFW_FORMAT_UNAVAILABLE";
            default: return "UNKNOWN_ERROR";
        }
    }
    
    public final int code;
    
    DisplayException(GLFWError error)
    {
        super(getName(error.code) + ": " + error.message);
        code = error.code;
    }
}
