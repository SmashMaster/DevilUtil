package com.samrj.devil.display;

import com.samrj.devil.io.Memory;
import com.samrj.devil.math.Vec2i;
import java.nio.ByteBuffer;
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
        Memory xBlock = new Memory(4);;
        Memory yBlock = new Memory(4);;
        
        GLFW.nglfwGetWindowPos(window, xBlock.address, yBlock.address);
        
        Vec2i out = new Vec2i(xBlock.readInt(), yBlock.readInt());
        xBlock.free();
        yBlock.free();
        return out;
    }
    
    public static Vec2i getWindowSize(long window)
    {
        Memory widthBlock = new Memory(4);
        Memory heightBlock = new Memory(4);
        
        GLFW.nglfwGetWindowSize(window, widthBlock.address, heightBlock.address);
        
        Vec2i out = new Vec2i(widthBlock.readInt(), heightBlock.readInt());
        widthBlock.free();
        heightBlock.free();
        return out;
    }
    
    public static final Vec2i getFramebufferSize(long window)
    {
        Memory widthBlock = new Memory(4);
        Memory heightBlock = new Memory(4);
        
        GLFW.nglfwGetFramebufferSize(window, widthBlock.address, heightBlock.address);
        
        Vec2i out = new Vec2i(widthBlock.readInt(), heightBlock.readInt());
        widthBlock.free();
        heightBlock.free();
        return out;
    }
    
    public static final FrameSize getWindowFrameSize(long window)
    {
        Memory leftBlock = new Memory(4);
        Memory topBlock = new Memory(4);
        Memory rightBlock = new Memory(4);
        Memory bottomBlock = new Memory(4);
        
        GLFW.nglfwGetWindowFrameSize(window, leftBlock.address, topBlock.address,
                                    rightBlock.address, bottomBlock.address);
        
        FrameSize out =  new FrameSize(leftBlock.readInt(),
                                       topBlock.readInt(),
                                       rightBlock.readInt(),
                                       bottomBlock.readInt());
        leftBlock.free();
        topBlock.free();
        rightBlock.free();
        bottomBlock.free();
        return out;
    }
    
    public static Vec2i getMonitorPos(long monitor)
    {
        Memory xBlock = new Memory(4);
        Memory yBlock = new Memory(4);
        
        GLFW.nglfwGetMonitorPos(monitor, xBlock.address, yBlock.address);
        
        Vec2i out = new Vec2i(xBlock.readInt(), yBlock.readInt());
        xBlock.free();
        yBlock.free();
        return out;
    }
    
    public static Vec2i getMonitorPhysicalSize(long monitor)
    {
        Memory widthBlock = new Memory(4);
        Memory heightBlock = new Memory(4);
        
        GLFW.nglfwGetMonitorPhysicalSize(monitor, widthBlock.address, heightBlock.address);
        
        Vec2i out = new Vec2i(widthBlock.readInt(), heightBlock.readInt());
        widthBlock.free();
        heightBlock.free();
        return out;
    }
    
    public static VideoMode[] getMonitorVideoModes(long monitor)
    {
        Memory countBlock = new Memory(4);
        ByteBuffer buffer = GLFW.glfwGetVideoModes(monitor, countBlock.buffer);
        int count = countBlock.readInt();
        countBlock.free();
        
        VideoMode[] out = new VideoMode[count];
        for (int i=0; i<count; i++) out[i] = new VideoMode(buffer);
        return out;
    }
    
    public static VideoMode getMonitorVideoMode(long monitor)
    {
        return new VideoMode(GLFW.glfwGetVideoMode(monitor));
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
        Memory[] blocks = gammaRamp.allocate();
        GLFW.nglfwSetGammaRamp(monitor, blocks[3].address);
        for (Memory mem : blocks) mem.free();
    }
    
    private GLFWUtil()
    {
    }
}
