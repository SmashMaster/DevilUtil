package com.samrj.devil.game;

import com.samrj.devil.display.GLFWUtil;
import java.util.Arrays;
import org.lwjgl.glfw.GLFW;

/**
 * Mouse input class.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class Mouse
{
    private final long window;
    private final boolean[] states;
    private final CursorCallback cursorCallback;
    private final ButtonCallback buttonCallback;
    private final ScrollCallback scrollCallback;
    
    private boolean posDirty = true;
    private float x, y;
    
    public Mouse(long window, CursorCallback cursorCallback,
            ButtonCallback buttonCallback, ScrollCallback scrollCallback)
    {
        this.window = window;
        states = new boolean[GLFW.GLFW_MOUSE_BUTTON_LAST + 1];
        this.cursorCallback = cursorCallback;
        this.buttonCallback = buttonCallback;
        this.scrollCallback = scrollCallback;
        
        GLFW.glfwSetCursorPosCallback(window, this::cursorPos);
        GLFW.glfwSetMouseButtonCallback(window, this::button);
        GLFW.glfwSetScrollCallback(window, this::scroll);
    }
    
    @Deprecated
    public final void reset()
    {
        posDirty = true;
        Arrays.fill(states, false);
    }
    
    private void cursorPos(long window, double xpos, double ypos)
    {
        ypos = GLFWUtil.getWindowSize(window).y - ypos;
        
        float dx, dy;
        
        if (posDirty)
        {
            dx = 0.0f;
            dy = 0.0f;
            posDirty = false;
        }
        else
        {
            dx = (float)xpos - x;
            dy = (float)ypos - y;
        }
        
        x = (float)xpos;
        y = (float)ypos;
        
        cursorCallback.accept(x, y, dx, dy);
    }
    
    private void button(long window, int button, int action, int mods)
    {
        switch (action)
        {
            case GLFW.GLFW_PRESS: states[button] = true; break;
            case GLFW.GLFW_RELEASE: states[button] = false; break;
        }
        
        buttonCallback.accept(button, action, mods);
    }
    
    private void scroll(long window, double xoffset, double yoffset)
    {
        scrollCallback.accept((float)xoffset, (float)yoffset);
    }
    
    public final float getX()
    {
        return x;
    }
    
    public final float getY()
    {
        return y;
    }
    
    public final boolean isButtonDown(int button)
    {
        if (button < 0 || button > GLFW.GLFW_MOUSE_BUTTON_LAST) throw new IllegalArgumentException();
        return states[button];
    }
    
    public void setCursorMode(int glfwCursorModeEnum)
    {
        GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, glfwCursorModeEnum);
    }
    
    public void setGrabbed(boolean grabbed)
    {
        //GLFW generates weird cursor position events after disabling/enabling
        //the cursor. Prevent this by resetting the mouse position.
        
//        long a = MemStack.push(8);
//        long b = MemStack.push(8);
//        GLFW.nglfwGetCursorPos(window, a, b);
//        double mx = MemoryUtil.memGetDouble(a);
//        double my = MemoryUtil.memGetDouble(b);
//        MemStack.pop(2);
        
        setCursorMode(grabbed ? GLFW.GLFW_CURSOR_DISABLED : GLFW.GLFW_CURSOR_NORMAL);
        
//        GLFW.glfwSetCursorPos(window, mx, my);
    }
    
    public boolean isGrabbed()
    {
        return GLFW.glfwGetInputMode(window, GLFW.GLFW_CURSOR) == GLFW.GLFW_CURSOR_DISABLED;
    }
    
    @FunctionalInterface
    public interface CursorCallback
    {
        public void accept(float x, float y, float dx, float dy);
    }
    
    @FunctionalInterface
    public interface ButtonCallback
    {
        public void accept(int button, int action, int mods);
    }
    
    @FunctionalInterface
    public interface ScrollCallback
    {
        public void accept(float dx, float dy);
    }
}
