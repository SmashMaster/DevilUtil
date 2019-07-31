package com.samrj.devil.game;

import com.samrj.devil.util.IntList;

import static org.lwjgl.glfw.GLFW.*;

/**
 * Display hint class.
 * 
 * See GLFW documentation for a list of valid hints:
 * 
 * http://www.glfw.org/docs/latest/window.html#window_hints
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2019 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class HintSet
{
    private final IntList targets, hints;
    private int size;
    
    HintSet()
    {
        targets = new IntList();
        hints = new IntList();
    }
    
    public HintSet hint(int target, int hint)
    {
        targets.add(target);
        hints.add(hint);
        size++;
        return this;
    }
    
    public void clear()
    {
        targets.clear();
        hints.clear();
        size = 0;
    }
    
    void glfw()
    {
        for (int i=0; i<size; i++) glfwWindowHint(targets.get(i), hints.get(i));
    }
}
