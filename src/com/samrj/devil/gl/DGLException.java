package com.samrj.devil.gl;

import org.lwjgl.opengl.AMDDebugOutput;
import org.lwjgl.opengl.ARBDebugOutput;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL43;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.opengl.GLDebugMessageAMDCallback;
import org.lwjgl.opengl.GLDebugMessageARBCallback;
import org.lwjgl.opengl.GLDebugMessageCallback;
import org.lwjgl.opengl.GLUtil;
import org.lwjgl.opengl.KHRDebug;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.libffi.Closure;

public class DGLException extends RuntimeException
{
    private static Closure closure;
    
    private static String memUTF8(long address, int length)
    {
        return MemoryUtil.memDecodeUTF8(MemoryUtil.memByteBuffer(address, length));
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
            throw new DGLException(getSeverity(severity) + " severity " +
                    memUTF8(message, length) + " from " + getSource(source));
    }
    
    private static void error(int id, int category, int severity, int length, long message, long userParam)
    {
        if (category == AMDDebugOutput.GL_DEBUG_CATEGORY_API_ERROR_AMD)
            throw new DGLException(memUTF8(message, length));
    }
    
    private static Closure setupCallback()
    {
        GLCapabilities caps = GL.getCapabilities();

        if (caps.OpenGL43)
        {
            GLDebugMessageCallback proc = GLDebugMessageCallback.create(DGLException::error);
            GL43.glDebugMessageCallback(proc, MemoryUtil.NULL);
            if ((GL11.glGetInteger(GL30.GL_CONTEXT_FLAGS) & GL43.GL_CONTEXT_FLAG_DEBUG_BIT) == 0)
                GL11.glEnable(GL43.GL_DEBUG_OUTPUT);
            return proc;
        }

        if (caps.GL_KHR_debug)
        {
            GLDebugMessageCallback proc = GLDebugMessageCallback.create(DGLException::error);
            KHRDebug.glDebugMessageCallback(proc, MemoryUtil.NULL);
            if (caps.OpenGL30 && (GL11.glGetInteger(GL30.GL_CONTEXT_FLAGS) & GL43.GL_CONTEXT_FLAG_DEBUG_BIT) == 0)
                GL11.glEnable(GL43.GL_DEBUG_OUTPUT);
            return proc;
        }

        if (caps.GL_ARB_debug_output)
        {
            GLDebugMessageARBCallback proc = GLDebugMessageARBCallback.create(DGLException::error);
            ARBDebugOutput.glDebugMessageCallbackARB(proc, MemoryUtil.NULL);
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
        if (closure != null) throw new IllegalStateException("Already initialized.");
        closure = setupCallback();
    }
    
    static void terminate()
    {
        if (closure == null) throw new IllegalStateException("Already terminated.");
        closure.free();
        closure = null;
    }
    
    private DGLException(String message)
    {
        super(message);
    }
}
