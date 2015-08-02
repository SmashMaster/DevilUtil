package com.samrj.devil.game;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWScrollCallback;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;

/**
 * GLFW event buffer class.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class EventBuffer
{
    private final long window;
    private final Queue<Runnable> eventQueue;
    
    private final GLFWCursorPosCallback cursorPosCallback;
    private final GLFWMouseButtonCallback mouseButtonCallback;
    private final GLFWScrollCallback scrollCallback;
    private final GLFWKeyCallback keyCallback;
    
    private final Mouse mouse;
    private final Keyboard keyboard;
    
    public EventBuffer(long window, Mouse mouse, Keyboard keyboard)
    {
        this.window = window;
        eventQueue = new ConcurrentLinkedQueue<>();
            
        cursorPosCallback = GLFW.GLFWCursorPosCallback(this::cursorPosCallback);
        mouseButtonCallback = GLFW.GLFWMouseButtonCallback(this::mouseButtonCallback);
        scrollCallback = GLFW.GLFWScrollCallback(this::mouseScrollCallback);
        keyCallback = GLFW.GLFWKeyCallback(this::keyCallback);

        GLFW.glfwSetCursorPosCallback(window, cursorPosCallback);
        GLFW.glfwSetMouseButtonCallback(window, mouseButtonCallback);
        GLFW.glfwSetScrollCallback(window, scrollCallback);
        GLFW.glfwSetKeyCallback(window, keyCallback);
        
        this.mouse = mouse;
        this.keyboard = keyboard;
    }
    
    public void flushEvents()
    {
        while (!eventQueue.isEmpty()) eventQueue.poll().run();
    }
    
    public void discardEvents()
    {
        eventQueue.clear();
    }
    
    private void cursorPosCallback(long window, double xpos, double ypos)
    {
        assert(this.window == window);
        eventQueue.offer(() -> {mouse.cursorPos((float)xpos, (float)ypos);});
    }
    
    private void mouseButtonCallback(long window, int button, int action, int mods)
    {
        assert(this.window == window);
        eventQueue.offer(() -> {mouse.button(button, action, mods);});
    }
    
    private void mouseScrollCallback(long window, double xoffset, double yoffset)
    {
        assert(this.window == window);
        eventQueue.offer(() -> {mouse.scroll((float)xoffset, (float)yoffset);});
    }
    
    private void keyCallback(long window, int key, int scancode, int action, int mods)
    {
        assert(this.window == window);
        eventQueue.offer(() -> {keyboard.key(key, action, mods);});
    }
}
