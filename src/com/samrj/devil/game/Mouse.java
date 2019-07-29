package com.samrj.devil.game;

import com.samrj.devil.display.GLFWUtil;
import java.util.Arrays;

import static org.lwjgl.glfw.GLFW.*;

/**
 * Mouse input class.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2019 Samuel Johnson
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
        states = new boolean[GLFW_MOUSE_BUTTON_LAST + 1];
        this.cursorCallback = cursorCallback;
        this.buttonCallback = buttonCallback;
        this.scrollCallback = scrollCallback;
        
        glfwSetCursorPosCallback(window, this::cursorPos);
        glfwSetMouseButtonCallback(window, this::button);
        glfwSetScrollCallback(window, this::scroll);
    }
    
    public final void setPosDirty()
    {
        posDirty = true;
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
            case GLFW_PRESS: states[button] = true; break;
            case GLFW_RELEASE: states[button] = false; break;
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
    
    public final void setPos(float x, float y)
    {
        y = GLFWUtil.getWindowSize(window).y - y;
        glfwSetCursorPos(window, x, y);
        cursorPos(window, x, y);
    }
    
    public final boolean isButtonDown(int button)
    {
        if (button < 0 || button > GLFW_MOUSE_BUTTON_LAST) throw new IllegalArgumentException();
        return states[button];
    }
    
    public void setGrabbed(boolean grabbed)
    {
        int mode = grabbed ? GLFW_CURSOR_DISABLED : GLFW_CURSOR_NORMAL;
        glfwSetInputMode(window, GLFW_CURSOR, mode);
    }
    
    public boolean isGrabbed()
    {
        return glfwGetInputMode(window, GLFW_CURSOR) == GLFW_CURSOR_DISABLED;
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
