package com.samrj.devil.display;

import java.util.LinkedList;
import java.util.Queue;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryUtil;

/**
 * GLFW error callback handling class.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 */
class GLFWError
{
    private static final Queue<GLFWError> errors = new LinkedList<>();
    
    static void init()
    {
        GLFW.glfwSetErrorCallback(GLFW.GLFWErrorCallback(GLFWError::onError));
    }
    
    private static void onError(int errorCode, long descPointer)
    {
        String desc = MemoryUtil.memDecodeUTF8(descPointer);
        errors.add(new GLFWError(errorCode, desc));
    }
    
    /**
     * Flushes any errors stored by onError() callback, and throws the first
     * error found. Should be called after most GLFW library calls.
     */
    static void flushErrors()
    {
        GLFWError error = errors.poll();
        if (error == null) return;
        errors.clear();
        throw new DisplayException(error);
    }
    
    /**
     * Clears the error queue without throwing an exception.
     */
    static void clearErrors()
    {
        errors.clear();
    }
    
    final int code;
    final String message;
    
    private GLFWError(int code, String message)
    {
        this.code = code;
        this.message = message;
    }
}
