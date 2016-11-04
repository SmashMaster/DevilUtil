package com.samrj.devil.game;

import com.samrj.devil.display.GLFWUtil;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.lwjgl.glfw.GLFW;

/**
 * GLFW event buffer class.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class EventBuffer
{
    private final Queue<Runnable> eventQueue;
    
    private final Mouse mouse;
    private final Keyboard keyboard;
    private final int windowHeight;
    
    public EventBuffer(long window, Mouse mouse, Keyboard keyboard)
    {
        eventQueue = new ConcurrentLinkedQueue<>();
        
        GLFW.glfwSetCursorPosCallback(window, this::onCursorPos);
        GLFW.glfwSetMouseButtonCallback(window, this::onMouseButton);
        GLFW.glfwSetScrollCallback(window, this::onMouseScroll);
        GLFW.glfwSetKeyCallback(window, this::onKey);
        
        this.mouse = mouse;
        this.keyboard = keyboard;
        windowHeight = GLFWUtil.getWindowSize(window).y;
    }
    
    public void flushEvents()
    {
        while (!eventQueue.isEmpty()) eventQueue.poll().run();
    }
    
    public void discardInput()
    {
        eventQueue.clear();
        mouse.reset();
        keyboard.reset();
    }
    
    private void onCursorPos(long window, double xpos, double ypos)
    {
        eventQueue.add(() -> mouse.cursorPos((float)xpos, (float)(windowHeight - ypos)));
    }
    
    private void onMouseButton(long window, int button, int action, int mods)
    {
        eventQueue.add(() -> mouse.button(button, action, mods));
    }
    
    private void onMouseScroll(long window, double xoffset, double yoffset)
    {
        eventQueue.add(() -> mouse.scroll((float)xoffset, (float)yoffset));
    }
    
    private void onKey(long window, int key, int scancode, int action, int mods)
    {
        eventQueue.add(() -> keyboard.key(key, action, mods));
    }
}
