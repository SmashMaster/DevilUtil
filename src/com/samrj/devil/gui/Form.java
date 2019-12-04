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
    /**
     * The position of the southwest (bottom-left) corner of this Form in
     * viewport space.
     */
    protected float x0, y0;
    
    /**
     * The size of a form is determined from the bottom of the form tree, up to
     * the top. This means there are two kinds of forms: ones of fixed size, and
     * ones that calculate their size from their children.
     */
    protected float width = 128.0f, height = 32.0f;
    
    /**
     * Returns the current southwest (bottom-left) corner of this form.
     */
    public final Vec2 getPos()
    {
        return new Vec2(x0, y0);
    }
    
    /**
     * Gets the current size of this form.
     */
    public final Vec2 getSize()
    {
        return new Vec2(width, height);
    }
    
    /**
     * Calculates the size of this form based on its children.
     */
    protected void updateSize()
    {
    }
    
    /**
     * Sets the southwest (or bottom-left) position of this form to the given
     * coordinates, and updates all of its children recursively.
     */
    protected void layout(float x, float y)
    {
        x0 = x; y0 = y;
    }
    
    /**
     * Any form that may be clicked (activated) may be hovered. This method
     * recursively finds the currently hovered form, or null if no form was
     * found. Non-interactive forms may return null when the mouse is over them.
     */
    protected Form hover(float x, float y)
    {
        return null;
    }
    
    /**
     * Recursively finds whichever scrollbox the mouse is over. This is used to
     * send mouse scroll wheel. ScrollBox returns itself, and leaf forms (those
     * with no children) return null.
     */
    protected ScrollBox findScrollBox(float x, float y)
    {
        return null;
    }
    
    /**
     * When a hovered form is clicked, this method is called on it. Some forms
     * may have behavior that occurs when they are clicked, such as buttons and
     * sliders. If a form has a click-and-drag behavior, this method should
     * return true. It is often useful to grab focus from within this method,
     * using DUI.focus().
     */
    protected boolean activate()
    {
        return false;
    }
    
    /**
     * This method is called when a clicked-and-dragged form is released. It is
     * not called if activate() returns false, as the form never becomes active.
     */
    protected void deactivate()
    {
    }
    
    /**
     * Whichever form has focus will receive all character and key events.
     */
    protected void character(char character, int codepoint)
    {
    }
    
    /**
     * Whichever form has focus will receive all character and key events.
     */
    protected void key(int key, int action, int mods)
    {
    }
    
    /**
     * This method is called when a form loses focus.
     */
    protected void defocus()
    {
    }

    /**
     * This method is called to render a form to the screen.
     */
    protected abstract void render(DUIDrawer drawer);
}
