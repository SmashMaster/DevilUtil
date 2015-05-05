package com.samrj.devil.input;

import com.samrj.devil.display.Window;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Queue;
import org.lwjgl.glfw.GLFW;

/**
 * Mouse class.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 */
public class Mouse
{
    private final Queue<MouseEvent> eventQueue = new ConcurrentLinkedQueue<>();
    private final Window window;
    
    //Allow anyone to override this class and implement their own callback functions.
    //Make sure to have window keep track of which mouse it owns and throw an exception
    //if the user tries to create two mice for the same window.
    //As for dealing with mouse events, we'll probably need four subclasses of MouseEvent
    //and an enum called MouseEventType.
    
    public Mouse(Window window)
    {
        this.window = window;
        
        GLFW.glfwSetMouseButtonCallback(window.getID(), GLFW.GLFWMouseButtonCallback(this::onButton));
        GLFW.glfwSetCursorPosCallback(window.getID(), GLFW.GLFWCursorPosCallback(this::onMove));
        GLFW.glfwSetCursorEnterCallback(window.getID(), GLFW.GLFWCursorEnterCallback(this::onEnter));
        GLFW.glfwSetScrollCallback(window.getID(), GLFW.GLFWScrollCallback(this::onScroll));
        //Will not support file drag/drop for now. (Don't know how to dereference the file path string array.)
    }
    
    //Queue events rather than
    
    private void onButton(long window, int button, int action, int mods)
    {
    }
    
    private void onMove(long window, double xpos, double ypos)
    {
    }
    
    private void onEnter(long window, int entered)
    {
    }
    
    private void onScroll(long window, double xoffset, double yoffset)
    {
    }
}