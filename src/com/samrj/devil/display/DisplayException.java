package com.samrj.devil.display;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryUtil;

/**
 * Exception class for GLFW errors.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class DisplayException extends RuntimeException
{
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
    
    public static final void glfwThrow(int error, long description)
    {
        throw new DisplayException(getName(error) + ": " + MemoryUtil.memUTF8(description));
    }
    
    private DisplayException(String message)
    {
        super(message);
    }
}
