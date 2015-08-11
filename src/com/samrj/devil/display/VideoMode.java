package com.samrj.devil.display;

import java.nio.ByteBuffer;

/**
 * Video mode class.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class VideoMode
{
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
    
    VideoMode(ByteBuffer buffer)
    {
        width = buffer.getInt();
        height = buffer.getInt();
        redBits = buffer.getInt();
        greenBits = buffer.getInt();
        blueBits = buffer.getInt();
        refreshRate = buffer.getInt();
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
