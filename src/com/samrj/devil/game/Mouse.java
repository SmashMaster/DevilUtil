package com.samrj.devil.game;

import com.samrj.devil.math.Vec2;
import com.samrj.devil.math.Vec2i;
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
    private static Vec2 screenToFramebuffer(long window, double xpos, double ypos)
    {
        Vec2i winSize = GLFWUtil.getWindowSize(window);
        xpos = xpos/winSize.x;
        ypos = (winSize.y - ypos)/winSize.y;
        
        Vec2i fbSize = GLFWUtil.getFramebufferSize(window);
        xpos = xpos*fbSize.x;
        ypos = ypos*fbSize.y;
        
        return new Vec2((float)xpos, (float)ypos);
    }
    
    private static Vec2 framebufferToScreen(long window, double xpos, double ypos)
    {
        Vec2i fbSize = GLFWUtil.getFramebufferSize(window);
        xpos = xpos/fbSize.x;
        ypos = ypos/fbSize.y;
        
        Vec2i winSize = GLFWUtil.getWindowSize(window);
        xpos = xpos*winSize.x;
        ypos = winSize.y - ypos*winSize.y;
        
        return new Vec2((float)xpos, (float)ypos);
    }
    
    private final long window;
    private final CursorCallback cursorCallback;
    private final ButtonCallback buttonCallback;
    private final ScrollCallback scrollCallback;
    
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
    
    private void cursorPos(long window, double screenX, double screenY)
    {
        Vec2 fbPos = screenToFramebuffer(window, screenX, screenY);
        cursorCallback.accept(fbPos.x, fbPos.y);
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
            double screenX = xBuf.get(0);
            double screenY = yBuf.get(0);
            return screenToFramebuffer(window, screenX, screenY);
        }
    }
    
    public final void setPos(float x, float y)
    {
        Vec2 screenPos = framebufferToScreen(window, x, y);
        glfwSetCursorPos(window, screenPos.x, screenPos.y);
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
        public void accept(float x, float y);
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
