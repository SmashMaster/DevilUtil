package com.samrj.devil.display;

/**
 * Class representing the size, in screen coordinates, of each edge of the frame
 * of a window. This size includes the title bar, if the window has one. The
 * size of the frame may vary depending on the window-related hints used to
 * create it.
 * 
 * <p>Because this class stores the size of each window frame edge and not the
 * offset along a particular coordinate axis, each value will always be zero
 * or positive.</p>
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 */
public final class FrameSize
{
    /**
     * The size, in screen coordinates, of the left edge of the window frame.
     */
    public final int left;
    
    /**
     * The size, in screen coordinates, of the top edge of the window frame.
     */
    public final int top;
    
    /**
     * The size, in screen coordinates, of the right edge of the window frame.
     */
    public final int right;
    
    /**
     * The size, in screen coordinates, of the bottom edge of the window frame.
     */
    public final int bottom;
    
    
    FrameSize(int left, int top, int right, int bottom)
    {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }
}
