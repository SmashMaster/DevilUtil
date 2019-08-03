package com.samrj.devil.gl;

import java.nio.IntBuffer;
import org.lwjgl.opengl.*;
import org.lwjgl.system.Callback;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL43C.*;
import static org.lwjgl.system.MemoryUtil.*;

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
    private static boolean initialized;
    
    private static String getSource(int source)
    {
        switch (source)
        {
            case GL_DEBUG_SOURCE_API: return "API";
            case GL_DEBUG_SOURCE_WINDOW_SYSTEM: return "WINDOW SYSTEM";
            case GL_DEBUG_SOURCE_SHADER_COMPILER: return "SHADER COMPILER";
            case GL_DEBUG_SOURCE_THIRD_PARTY: return "THIRD PARTY";
            case GL_DEBUG_SOURCE_APPLICATION: return "APPLICATION";
            case GL_DEBUG_SOURCE_OTHER: return "OTHER";
            default: return "UNKNOWN";
        }
    }
    
    private static String getType(int type)
    {
        switch (type)
        {
            case GL_DEBUG_TYPE_ERROR: return "ERROR";
            case GL_DEBUG_TYPE_DEPRECATED_BEHAVIOR: return "DEPRECATED BEHAVIOR";
            case GL_DEBUG_TYPE_UNDEFINED_BEHAVIOR: return "UNDEFINED BEHAVIOR";
            case GL_DEBUG_TYPE_PORTABILITY: return "PORTABILITY";
            case GL_DEBUG_TYPE_PERFORMANCE: return "PERFORMANCE";
            case GL_DEBUG_TYPE_MARKER: return "MARKER";
            case GL_DEBUG_TYPE_PUSH_GROUP: return "PUSH_GROUP";
            case GL_DEBUG_TYPE_POP_GROUP: return "POP_GROUP";
            case GL_DEBUG_TYPE_OTHER: return "OTHER";
            default: return "UNKNOWN";
        }
    }
    
    private static String getSeverity(int severity)
    {
        switch (severity)
        {
            case GL_DEBUG_SEVERITY_HIGH: return "HIGH";
            case GL_DEBUG_SEVERITY_MEDIUM: return "MEDIUM";
            case GL_DEBUG_SEVERITY_LOW: return "LOW";
            case GL_DEBUG_SEVERITY_NOTIFICATION: return "NOTIFICATION";
            default: return "UNKNOWN";
        }
    }
    
    private static String utf8(long address, int length)
    {
        return memUTF8(memByteBuffer(address, length));
    }
    
    private static String getMessage(int source, int type, int severity, int length, long msgAddr)
    {
        return "from: " + getSource(source) + ", type: " + getType(type) + ", severity: " + getSeverity(severity) + " - \"" + utf8(msgAddr, length) + "\"";
    }
    
    private static void error(int source, int type, int id, int severity, int length, long msgAddr, long userParam)
    {
        String message = getMessage(source, type, severity, length, msgAddr);
        if (severity == GL_DEBUG_SEVERITY_HIGH) throw new DGLException(message);
        new Throwable("DevilUtil (DGL) - " + message).printStackTrace();
    }
    
    private static void errorKHR(int id, int category, int severity, int length, long message, long userParam)
    {
        if (category == AMDDebugOutput.GL_DEBUG_CATEGORY_API_ERROR_AMD)
            throw new DGLException(utf8(message, length));
    }
    
    private static Callback setupCallback()
    {
        GLCapabilities caps = GL.getCapabilities();

        if (caps.OpenGL43)
        {
            System.out.println("DevilUtil (DGL) - OpenGL 4.3 debug enabled.");
            
            GLDebugMessageCallback proc = GLDebugMessageCallback.create(DGLException::error);
            glDebugMessageCallback(proc, NULL);
            glEnable(GL_DEBUG_OUTPUT);
            glEnable(GL_DEBUG_OUTPUT_SYNCHRONOUS);
            glDebugMessageControl(GL_DONT_CARE, GL_DONT_CARE, GL_DONT_CARE, (IntBuffer)null, false);
            glDebugMessageControl(GL_DONT_CARE, GL_DONT_CARE, GL_DEBUG_SEVERITY_MEDIUM, (IntBuffer)null, true);
            glDebugMessageControl(GL_DONT_CARE, GL_DONT_CARE, GL_DEBUG_SEVERITY_HIGH, (IntBuffer)null, true);
            return proc;
        }

        if (caps.GL_KHR_debug)
        {
            System.out.println("DevilUtil (DGL) - KHR debug enabled.");
            
            GLDebugMessageCallback proc = GLDebugMessageCallback.create(DGLException::error);
            KHRDebug.glDebugMessageCallback(proc, NULL);
            glEnable(GL_DEBUG_OUTPUT);
            glEnable(GL_DEBUG_OUTPUT_SYNCHRONOUS);
            KHRDebug.glDebugMessageControl(GL_DONT_CARE, GL_DONT_CARE, GL_DONT_CARE, (IntBuffer)null, false);
            KHRDebug.glDebugMessageControl(GL_DONT_CARE, GL_DONT_CARE, GL_DEBUG_SEVERITY_MEDIUM, (IntBuffer)null, true);
            KHRDebug.glDebugMessageControl(GL_DONT_CARE, GL_DONT_CARE, GL_DEBUG_SEVERITY_HIGH, (IntBuffer)null, true);
            return proc;
        }

        if (caps.GL_ARB_debug_output)
        {
            System.out.println("DevilUtil (DGL) - ARB debug enabled.");
            
            GLDebugMessageARBCallback proc = GLDebugMessageARBCallback.create(DGLException::error);
            ARBDebugOutput.glDebugMessageCallbackARB(proc, NULL);
            glEnable(ARBDebugOutput.GL_DEBUG_OUTPUT_SYNCHRONOUS_ARB);
            return proc;
        }

        if (caps.GL_AMD_debug_output)
        {
            System.out.println("DevilUtil (DGL) - AMD debug enabled.");
            
            GLDebugMessageAMDCallback proc = GLDebugMessageAMDCallback.create(DGLException::errorKHR);
            AMDDebugOutput.glDebugMessageCallbackAMD(proc, NULL);
            return proc;
        }
        
        System.out.println("DevilUtil (DGL) - No debug implementation found.");
        
        return null;
    }
    
    static void init(boolean debug)
    {
        if (initialized) throw new IllegalStateException("Already initialized.");
        if (debug) callback = setupCallback();
        initialized = true;
    }
    
    static void terminate()
    {
        if (!initialized) throw new IllegalStateException("Already terminated.");
        if (callback != null)
        {
            callback.free();
            callback = null;
        }
        initialized = false;
    }
    
    private DGLException(String message)
    {
        super(message);
    }
}
