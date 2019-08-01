package com.samrj.devil.game;

import java.util.Objects;

import static org.lwjgl.glfw.GLFW.*;

/**
 * Keyboard input class.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2019 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class Keyboard
{
    private final long window;
    private final KeyCallback keyCallback;
    private final CharacterCallback charCallback;
    
    Keyboard(long window, KeyCallback keyCallback, CharacterCallback charCallback)
    {
        this.window = window;
        this.keyCallback = Objects.requireNonNull(keyCallback);
        this.charCallback = Objects.requireNonNull(charCallback);
        
        glfwSetKeyCallback(window, this::key);
        glfwSetCharCallback(window, this::character);
    }
    
    private void key(long window, int key, int scancode, int action, int mods)
    {
        keyCallback.accept(key, action, mods);
    }
    
    private void character(long window, int codepoint)
    {
        for (char c : Character.toChars(codepoint)) charCallback.accept(c, codepoint);
    }
    
    public final boolean isKeyDown(int key)
    {
        return glfwGetKey(window, key) == GLFW_PRESS;
    }
    
    void destroy()
    {
        glfwSetKeyCallback(window, null).free();
        glfwSetCharCallback(window, null).free();
    }
    
    @FunctionalInterface
    public interface KeyCallback
    {
        public void accept(int key, int action, int mods);
    }
    
    @FunctionalInterface
    public interface CharacterCallback
    {
        public void accept(char character, int codepoint);
    }
}
