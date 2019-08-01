package com.samrj.devil.game;

import com.samrj.devil.math.Vec2;
import java.nio.DoubleBuffer;
import java.util.Objects;
import org.lwjgl.system.MemoryStack;

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
    private final CursorCallback cursorCallback;
    private final ButtonCallback buttonCallback;
    private final ScrollCallback scrollCallback;
    
    private boolean posDirty = true;
    private float x, y;
    
    Mouse(long window, CursorCallback cursorCallback, ButtonCallback buttonCallback, ScrollCallback scrollCallback)
    {
        this.window = window;
        this.cursorCallback = Objects.requireNonNull(cursorCallback);
        this.buttonCallback = Objects.requireNonNull(buttonCallback);
        this.scrollCallback = Objects.requireNonNull(scrollCallback);
        
        glfwSetCursorPosCallback(window, this::cursorPos);
        glfwSetMouseButtonCallback(window, this::button);
        glfwSetScrollCallback(window, this::scroll);
    }
    
    public final void setPosDirty()
    {
        posDirty = true;
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
        buttonCallback.accept(button, action, mods);
    }
    
    private void scroll(long window, double xoffset, double yoffset)
    {
        scrollCallback.accept((float)xoffset, (float)yoffset);
    }
    
    public final Vec2 getPos()
    {
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            DoubleBuffer xBuf = stack.mallocDouble(1);
            DoubleBuffer yBuf = stack.mallocDouble(2);
            glfwGetCursorPos(window, xBuf, yBuf);
            return new Vec2((float)xBuf.get(0), GLFWUtil.getWindowSize(window).y - (float)yBuf.get(0));
        }
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
        return glfwGetMouseButton(window, button) == GLFW_PRESS;
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
    
    void destroy()
    {
        glfwSetCursorPosCallback(window, null).free();
        glfwSetMouseButtonCallback(window, null).free();
        glfwSetScrollCallback(window, null).free();
    }
    
    @FunctionalInterface
    public static interface CursorCallback
    {
        public void accept(float x, float y, float dx, float dy);
    }
    
    @FunctionalInterface
    public static interface ButtonCallback
    {
        public void accept(int button, int action, int mods);
    }
    
    @FunctionalInterface
    public static interface ScrollCallback
    {
        public void accept(float dx, float dy);
    }
}
