package com.samrj.devil.game;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.memUTF8;

/**
 * Exception class for GLFW errors.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2019 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class DisplayException extends RuntimeException
{
    public static final String getName(int errorCode)
    {
       switch (errorCode)
       {
           case GLFW_NOT_INITIALIZED: return "GLFW_NOT_INITIALIZED";
           case GLFW_NO_CURRENT_CONTEXT: return "GLFW_NO_CURRENT_CONTEXT";
           case GLFW_INVALID_ENUM: return "GLFW_INVALID_ENUM";
           case GLFW_INVALID_VALUE: return "GLFW_INVALID_VALUE";
           case GLFW_OUT_OF_MEMORY: return "GLFW_OUT_OF_MEMORY";
           case GLFW_API_UNAVAILABLE: return "GLFW_API_UNAVAILABLE";
           case GLFW_VERSION_UNAVAILABLE: return "GLFW_VERSION_UNAVAILABLE";
           case GLFW_PLATFORM_ERROR: return "GLFW_PLATFORM_ERROR";
           case GLFW_FORMAT_UNAVAILABLE: return "GLFW_FORMAT_UNAVAILABLE";
           default: return "UNKNOWN_ERROR";
       }
    }
    
    public static final void glfwThrow(int error, long description)
    {
        throw new DisplayException(getName(error) + ": " + memUTF8(description));
    }
    
    private DisplayException(String message)
    {
        super(message);
    }
}
