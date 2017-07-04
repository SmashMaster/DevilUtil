package com.samrj.devil.game;

import java.util.BitSet;
import org.lwjgl.glfw.GLFW;

/**
 * Keyboard input class.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2015 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class Keyboard
{
    private final BitSet states;
    private final KeyCallback keyCallback;
    
    Keyboard(long window, KeyCallback keyCallback)
    {
        if (keyCallback == null) throw new NullPointerException();
        states = new BitSet(GLFW.GLFW_KEY_LAST + 1);
        GLFW.glfwSetKeyCallback(window, this::key);
        this.keyCallback = keyCallback;
    }
    
    private void key(long window, int key, int scancode, int action, int mods)
    {
        if (key >= 0) switch (action)
        {
            case GLFW.GLFW_PRESS: states.set(key, true); break;
            case GLFW.GLFW_RELEASE: states.set(key, false); break;
        }
        
        keyCallback.accept(key, action, mods);
    }
    
    @Deprecated
    public final void reset()
    {
        states.clear();
    }
    
    public final boolean isKeyDown(int key)
    {
        if (key < 0 || key > GLFW.GLFW_KEY_LAST) throw new IllegalArgumentException();
        return states.get(key);
    }
    
    @FunctionalInterface
    public interface KeyCallback
    {
        public void accept(int key, int action, int mods);
    }
}
