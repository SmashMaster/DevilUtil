package com.samrj.devil.game;

import com.samrj.devil.util.IntList;

import static org.lwjgl.glfw.GLFW.glfwWindowHint;

/**
 * Display hint class.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2019 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
final class HintSet
{
    private final IntList targets, hints;
    private int size;
    
    HintSet()
    {
        targets = new IntList();
        hints = new IntList();
    }
    
    HintSet hint(int target, int hint)
    {
        targets.add(target);
        hints.add(hint);
        size++;
        return this;
    }
    
    void clear()
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
