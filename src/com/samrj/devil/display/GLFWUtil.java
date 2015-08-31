package com.samrj.devil.display;

import com.samrj.devil.io.Memory.Block;
import static com.samrj.devil.io.Memory.memUtil;
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
        IntBlock xBlock = new IntBlock();
        IntBlock yBlock = new IntBlock();
        
        GLFW.glfwGetWindowPos(window, xBlock.buf(), yBlock.buf());
        
        Vec2i out = new Vec2i(xBlock.read(), yBlock.read());
        xBlock.free();
        yBlock.free();
        return out;
    }
    
    public static Vec2i getWindowSize(long window)
    {
        IntBlock widthBlock = new IntBlock();
        IntBlock heightBlock = new IntBlock();
        
        GLFW.glfwGetWindowSize(window, widthBlock.buf(), heightBlock.buf());
        
        Vec2i out = new Vec2i(widthBlock.read(), heightBlock.read());
        widthBlock.free();
        heightBlock.free();
        return out;
    }
    
    public static final Vec2i getFramebufferSize(long window)
    {
        IntBlock widthBlock = new IntBlock();
        IntBlock heightBlock = new IntBlock();
        
        GLFW.glfwGetFramebufferSize(window, widthBlock.buf(), heightBlock.buf());
        
        Vec2i out = new Vec2i(widthBlock.read(), heightBlock.read());
        widthBlock.free();
        heightBlock.free();
        return out;
    }
    
    public static final FrameSize getWindowFrameSize(long window)
    {
        IntBlock leftBlock = new IntBlock();
        IntBlock topBlock = new IntBlock();
        IntBlock rightBlock = new IntBlock();
        IntBlock bottomBlock = new IntBlock();
        
        GLFW.glfwGetWindowFrameSize(window, leftBlock.buf(), topBlock.buf(),
                                    rightBlock.buf(), bottomBlock.buf());
        
        FrameSize out =  new FrameSize(leftBlock.read(),
                                       topBlock.read(),
                                       rightBlock.read(),
                                       bottomBlock.read());
        leftBlock.free();
        topBlock.free();
        rightBlock.free();
        bottomBlock.free();
        return out;
    }
    
    public static Vec2i getMonitorPos(long monitor)
    {
        IntBlock xBlock = new IntBlock();
        IntBlock yBlock = new IntBlock();
        
        GLFW.glfwGetMonitorPos(monitor, xBlock.buf(), yBlock.buf());
        
        Vec2i out = new Vec2i(xBlock.read(), yBlock.read());
        xBlock.free();
        yBlock.free();
        return out;
    }
    
    public static Vec2i getMonitorPhysicalSize(long monitor)
    {
        IntBlock widthBlock = new IntBlock();
        IntBlock heightBlock = new IntBlock();
        
        GLFW.glfwGetMonitorPhysicalSize(monitor, widthBlock.buf(), heightBlock.buf());
        
        Vec2i out = new Vec2i(widthBlock.read(), heightBlock.read());
        widthBlock.free();
        heightBlock.free();
        return out;
    }
    
    public static VideoMode[] getMonitorVideoModes(long monitor)
    {
        IntBlock countBlock = new IntBlock();
        ByteBuffer buffer = GLFW.glfwGetVideoModes(monitor, countBlock.buf());
        int count = countBlock.read();
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
        Block[] blocks = gammaRamp.allocate();
        GLFW.glfwSetGammaRamp(monitor, blocks[3].read());
        for (Block block : blocks) block.free();
    }
    
    private GLFWUtil()
    {
    }
    
    private static class IntBlock
    {
        private final Block block;
        
        private IntBlock()
        {
            block = memUtil.alloc(4);
        }
        
        private ByteBuffer buf()
        {
            return block.read();
        }
        
        private int read()
        {
            return block.readInt();
        }
        
        private void free()
        {
            block.free();
        }
    }
}
