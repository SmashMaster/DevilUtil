package com.samrj.devil.display;

import com.samrj.devil.buffer.BufferUtil;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.LinkedList;
import org.lwjgl.glfw.GLFW;

/**
 * Video mode class.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class VideoMode
{
    static LinkedList<VideoMode> getAll(long monitor)
    {
        IntBuffer countBuffer = BufferUtil.createIntBuffer(1);
        countBuffer.put(0);
        countBuffer.rewind();
        
        IntBuffer buffer = GLFW.glfwGetVideoModes(monitor, countBuffer).asIntBuffer();
        LinkedList<VideoMode> modes = new LinkedList<>();
        while (buffer.hasRemaining()) modes.add(new VideoMode(buffer));
        return modes;
    }
    
    /**
     * The width, in screen coordinates, of the video mode.
     */
    public final int width;
    
    /**
     * The height, in screen coordinates, of the video mode.
     */
    public final int height;
    
    /**
     * The bit depth of the red channel of the video mode.
     */
    public final int redBits;
    
    /**
     * The bit depth of the green channel of the video mode.
     */
    public final int greenBits;
    
    /**
     * The bit depth of the blue channel of the video mode.
     */
    public final int blueBits;
    
    /**
     * The refresh rate, in Hz, of the video mode.
     */
    public final int refreshRate;
    
    VideoMode(IntBuffer buffer)
    {
        width = buffer.get();
        height = buffer.get();
        redBits = buffer.get();
        greenBits = buffer.get();
        blueBits = buffer.get();
        refreshRate = buffer.get();
    }
    
    VideoMode(ByteBuffer buffer)
    {
        this(buffer.asIntBuffer());
    }
    
    @Override
    public String toString()
    {
        return "Width: " + width + '\n' +
               "Height: " + height + '\n' +
               "Red bit depth: " + redBits + '\n' +
               "Green bit depth: " + greenBits + '\n' +
               "Blue bit depth: " + blueBits + '\n' +
               "Refresh rate: " + refreshRate;
    }
}
