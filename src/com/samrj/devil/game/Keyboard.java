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
public class Keyboard
{
    private final BitSet states;
    
    public Keyboard()
    {
        states = new BitSet(GLFW.GLFW_KEY_LAST + 1);
    }
    
    public final void key(int key, int action, int mods)
    {
        if (key >= 0) switch (action)
        {
            case GLFW.GLFW_PRESS: states.set(key, true); break;
            case GLFW.GLFW_RELEASE: states.set(key, false); break;
        }
        
        onKey(key, action, mods);
    }
    
    public void onKey(int key, int action, int mods)
    {
    }
    
    public final boolean isKeyDown(int key)
    {
        if (key < 0 || key > GLFW.GLFW_KEY_LAST) throw new IllegalArgumentException();
        return states.get(key);
    }
}
