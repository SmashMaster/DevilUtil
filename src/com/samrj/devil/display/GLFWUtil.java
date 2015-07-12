package com.samrj.devil.display;

import com.samrj.devil.io.BufferUtil;
import com.samrj.devil.math.Vec2i;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import org.lwjgl.glfw.GLFW;

/**
 * Utility class for GLFW. GLFW must be initialized for most of these methods to
 * work.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class GLFWUtil
{
    public static Vec2i getWindowPos(long window)
    {
        BufferUtil.clearPublicBuffers();
        GLFW.glfwGetWindowPos(window, BufferUtil.pubBufA, BufferUtil.pubBufB);
        
        return new Vec2i(BufferUtil.pubBufA.getInt(),
                         BufferUtil.pubBufB.getInt());
    }
    
    public static Vec2i getWindowSize(long window)
    {
        BufferUtil.clearPublicBuffers();
        GLFW.glfwGetWindowSize(window, BufferUtil.pubBufA, BufferUtil.pubBufB);
        
        return new Vec2i(BufferUtil.pubBufA.getInt(),
                         BufferUtil.pubBufB.getInt());
    }
    
    public static final Vec2i getFramebufferSize(long window)
    {
        BufferUtil.clearPublicBuffers();
        GLFW.glfwGetFramebufferSize(window, BufferUtil.pubBufA, BufferUtil.pubBufB);
        
        return new Vec2i(BufferUtil.pubBufA.getInt(),
                         BufferUtil.pubBufB.getInt());
    }
    
    public static final FrameSize getWindowFrameSize(long window)
    {
        BufferUtil.clearPublicBuffers();
        GLFW.glfwGetWindowFrameSize(window, BufferUtil.pubBufA, BufferUtil.pubBufB,
                                        BufferUtil.pubBufC, BufferUtil.pubBufD);
        
        return new FrameSize(BufferUtil.pubBufA.getInt(),
                             BufferUtil.pubBufB.getInt(),
                             BufferUtil.pubBufC.getInt(),
                             BufferUtil.pubBufD.getInt());
    }
    
    public static Vec2i getMonitorPos(long monitor)
    {
        BufferUtil.clearPublicBuffers();
        GLFW.glfwGetMonitorPos(monitor, BufferUtil.pubBufA, BufferUtil.pubBufB);
        
        return new Vec2i(BufferUtil.pubBufA.getInt(),
                         BufferUtil.pubBufB.getInt());
    }
    
    public static Vec2i getMonitorPhysicalSize(long monitor)
    {
        BufferUtil.clearPublicBuffers();
        GLFW.glfwGetMonitorPhysicalSize(monitor, BufferUtil.pubBufA, BufferUtil.pubBufB);
        
        return new Vec2i(BufferUtil.pubBufA.getInt(),
                         BufferUtil.pubBufB.getInt());
    }
    
    public static LinkedList<VideoMode> getMonitorVideoModes(long monitor)
    {
        return VideoMode.getAll(monitor);
    }
    
    public static VideoMode getMonitorVideoMode(long monitor)
    {
        ByteBuffer buffer = GLFW.glfwGetVideoMode(monitor);
        return new VideoMode(buffer);
    }
    
    public static VideoMode getPrimaryMonitorVideoMode()
    {
        return getMonitorVideoMode(GLFW.glfwGetPrimaryMonitor());
    }
    
    public static GammaRamp getMonitorGammaRamp(long monitor)
    {
        return new GammaRamp(GLFW.glfwGetGammaRamp(monitor));
    }
    
    public static void setMonitorGammaRamp(long monitor, GammaRamp gammaRamp)
    {
        GLFW.glfwSetGammaRamp(monitor, gammaRamp.toBuffer());
    }
    
    private GLFWUtil()
    {
    }
}
