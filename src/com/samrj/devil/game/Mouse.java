package com.samrj.devil.game;

import org.lwjgl.glfw.GLFW;

public class Mouse
{
    private final long window;
    private boolean posDirty;
    private final boolean[] states;
    private float x, y;
    private float dx, dy;
    
    public Mouse(long window)
    {
        this.window = window;
        states = new boolean[GLFW.GLFW_MOUSE_BUTTON_LAST + 1];
        posDirty = true;
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
        
        onMoved(x, y, dx, dy);
    }
    
    public void onMoved(float x, float y, float dx, float dy)
    {
    }
    
    public final void button(int button, int action, int mods)
    {
        switch (action)
        {
            case GLFW.GLFW_PRESS: states[button] = true; break;
            case GLFW.GLFW_RELEASE: states[button] = false; break;
        }
        
        onButton(button, action, mods);
    }
    
    public void onButton(int button, int action, int mods)
    {
    }
    
    public final void scroll(float dx, float dy)
    {
        onScroll(dx, dy);
    }
    
    public void onScroll(float dx, float dy)
    {
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
}
