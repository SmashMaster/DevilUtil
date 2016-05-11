package com.samrj.devil.game;

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
    private float dx, dy;
    
    public Mouse(long window, CursorCallback cursorCallback,
            ButtonCallback buttonCallback, ScrollCallback scrollCallback)
    {
        this.window = window;
        states = new boolean[GLFW.GLFW_MOUSE_BUTTON_LAST + 1];
        this.cursorCallback = cursorCallback;
        this.buttonCallback = buttonCallback;
        this.scrollCallback = scrollCallback;
    }
    
    public final void cursorPos(float x, float y)
    {
        if (posDirty)
        {
            dx = 0.0f;
            dy = 0.0f;
            posDirty = false;
        }
        else
        {
            dx = x - this.x;
            dy = y - this.y;
        }
        
        this.x = x;
        this.y = y;
        
        cursorCallback.accept(x, y, dx, dy);
    }
    
    public final void button(int button, int action, int mods)
    {
        switch (action)
        {
            case GLFW.GLFW_PRESS: states[button] = true; break;
            case GLFW.GLFW_RELEASE: states[button] = false; break;
        }
        
        buttonCallback.accept(button, action, mods);
    }
    
    public final void scroll(float dx, float dy)
    {
        scrollCallback.accept(dx, dy);
    }
    
    public final float getX()
    {
        return x;
    }
    
    public final float getY()
    {
        return y;
    }
    
    public final float getDX()
    {
        return dx;
    }
    
    public final float getDY()
    {
        return dy;
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
        setCursorMode(grabbed ? GLFW.GLFW_CURSOR_DISABLED : GLFW.GLFW_CURSOR_NORMAL);
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
