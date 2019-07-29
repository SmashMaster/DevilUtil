package com.samrj.devil.gl;

import org.lwjgl.opengl.*;
import org.lwjgl.system.Callback;
import org.lwjgl.system.MemoryUtil;

import static org.lwjgl.opengl.GL11C.*;

/**
 * Automatically handles OpenGL debugging and exceptions.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2019 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class DGLException extends RuntimeException
{
    private static Callback callback;
    
    private static String memUTF8(long address, int length)
    {
        return MemoryUtil.memUTF8(MemoryUtil.memByteBuffer(address, length));
    }
    
    private static String getSeverity(int severity)
    {
        switch (severity)
        {
            case GL43.GL_DEBUG_SEVERITY_HIGH: return "HIGH";
            case GL43.GL_DEBUG_SEVERITY_MEDIUM: return "MEDIUM";
            case GL43.GL_DEBUG_SEVERITY_LOW: return "LOW";
            case GL43.GL_DEBUG_SEVERITY_NOTIFICATION: return "NOTIFICATION";
            default: return "UNKNOWN";
        }
    }
    
    private static String getSource(int source)
    {
        switch (source)
        {
            case GL43.GL_DEBUG_SOURCE_API: return "API";
            case GL43.GL_DEBUG_SOURCE_WINDOW_SYSTEM: return "WINDOW SYSTEM";
            case GL43.GL_DEBUG_SOURCE_SHADER_COMPILER: return "SHADER COMPILER";
            case GL43.GL_DEBUG_SOURCE_THIRD_PARTY: return "THIRD PARTY";
            case GL43.GL_DEBUG_SOURCE_APPLICATION: return "APPLICATION";
            case GL43.GL_DEBUG_SOURCE_OTHER: return "OTHER";
            default: return "UNKNOWN";
        }
    }
    
    private static void error(int source, int type, int id, int severity, int length, long message, long userParam)
    {
        if (source == GL43.GL_DEBUG_SOURCE_API && type == GL43.GL_DEBUG_TYPE_ERROR)
            throw new DGLException("from " + getSource(source) + ": " + getSeverity(severity) + " severity " + memUTF8(message, length));
    }
    
    private static void error(int id, int category, int severity, int length, long message, long userParam)
    {
        if (category == AMDDebugOutput.GL_DEBUG_CATEGORY_API_ERROR_AMD)
            throw new DGLException(memUTF8(message, length));
    }
    
    private static Callback setupCallback()
    {
        GLCapabilities caps = GL.getCapabilities();

        if (caps.OpenGL43)
        {
            GLDebugMessageCallback proc = GLDebugMessageCallback.create(DGLException::error);
            GL43.glDebugMessageCallback(proc, MemoryUtil.NULL);
            glEnable(GL43.GL_DEBUG_OUTPUT);
            glEnable(GL43.GL_DEBUG_OUTPUT_SYNCHRONOUS);
            return proc;
        }

        if (caps.GL_KHR_debug)
        {
            GLDebugMessageCallback proc = GLDebugMessageCallback.create(DGLException::error);
            KHRDebug.glDebugMessageCallback(proc, MemoryUtil.NULL);
            glEnable(KHRDebug.GL_DEBUG_OUTPUT);
            glEnable(KHRDebug.GL_DEBUG_OUTPUT_SYNCHRONOUS);
            return proc;
        }

        if (caps.GL_ARB_debug_output)
        {
            GLDebugMessageARBCallback proc = GLDebugMessageARBCallback.create(DGLException::error);
            ARBDebugOutput.glDebugMessageCallbackARB(proc, MemoryUtil.NULL);
            glEnable(ARBDebugOutput.GL_DEBUG_OUTPUT_SYNCHRONOUS_ARB);
            return proc;
        }

        if (caps.GL_AMD_debug_output)
        {
            GLDebugMessageAMDCallback proc = GLDebugMessageAMDCallback.create(DGLException::error);
            AMDDebugOutput.glDebugMessageCallbackAMD(proc, MemoryUtil.NULL);
            return proc;
        }
        
        return null;
    }
    
    static void init()
    {
        if (callback != null) throw new IllegalStateException("Already initialized.");
        callback = setupCallback();
    }
    
    static void terminate()
    {
        if (callback == null) throw new IllegalStateException("Already terminated.");
        callback.free();
        callback = null;
    }
    
    private DGLException(String message)
    {
        super(message);
    }
}
