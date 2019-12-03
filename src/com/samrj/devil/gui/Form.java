package com.samrj.devil.gui;

import com.samrj.devil.math.Vec2;

/**
 * The base class for everything that can be put in a Window. Interfaces built
 * using DevilUI are a hierarchy of nested forms.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2019 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public abstract class Form
{
    Window window;
    float width, height;
    
    public final Vec2 getSize()
    {
        return new Vec2(width, height);
    }
    
    abstract void updateSize();
    abstract void setAbsPos(float x, float y);
    
    Form hover(float x, float y)
    {
        return null;
    }
    
    ScrollBox findSrollbox(float x, float y)
    {
        return null;
    }
    
    boolean activate()
    {
        return false;
    }
    
    void deactivate()
    {
    }
    
    void character(char character, int codepoint)
    {
    }
    
    void key(int key, int action, int mods)
    {
    }
    
    void defocus()
    {
    }

    abstract void render(DUIDrawer drawer);
}
