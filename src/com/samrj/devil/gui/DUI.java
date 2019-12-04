/*
 * Copyright (c) 2019 Sam Johnson
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.samrj.devil.gui;

import com.samrj.devil.math.Vec2;
import java.util.Objects;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11C.*;

/**
 * DevilUI. A GUI library for DevilUtil.
 * 
 * @author Samuel Johnson (SmashMaster)
 */
public final class DUI
{
    private static DUIDrawer drawer;
    private static Font font;
    private static Window topWindow, bottomWindow;
    private static boolean init;
    
    private static float mouseX, mouseY;
    
    private static Window hoveredWindow, activeWindow;
    private static Form hoveredForm, activeForm;
    private static Form focusedForm;
    private static ScrollBox hoveredScrollBox;
    private static DropDown dropDown;
    private static boolean dropDownHovered;
    
    /**
     * Initializes DevilUI.
     */
    public static void init()
    {
        if (init) throw new IllegalStateException("DUI already initialized.");
        drawer = new DUIDrawer();
        init = true;
    }
    
    /**
     * Returns the current size of the screen, as defined by glViewport. This
     * is used by DevilUI to determine its screen coordinates.
     */
    public static Vec2 viewport()
    {
        int[] viewport = new int[4]; //x, y, width, height
        glGetIntegerv(GL_VIEWPORT, viewport);
        return new Vec2(viewport[2], viewport[3]);
    }

    /**
     * Returns the DevilUI draw object, which handles all UI rendering. It is
     * exposed to the application for convenience, but is not necessary to use
     * DevilUI.
     */
    public static DUIDrawer drawer()
    {
        if (!init) throw new IllegalStateException("Not initialized.");
        return drawer;
    }
    
    public static void setFont(Font font)
    {
        DUI.font = font;
    }
    
    public static Font font()
    {
        if (font == null) throw new IllegalStateException("No font selected.");
        return font;
    }
    
    /**
     * Displays the given window, and puts it at the top of the window stack.
     * Does nothing if the window is already visible.
     */
    public static void show(Window window)
    {
        if (!window.isVisible)
        {
            if (topWindow != null)
            {
                topWindow.above = window;
                window.below = topWindow;
            }
            else bottomWindow = window;
            topWindow = window;
            window.isVisible = true;
        }
    }
    
    /**
     * Hides the given window. Does nothing if the window is already hidden.
     */
    public static void hide(Window window)
    {
        if (window.isVisible)
        {
            if (window == topWindow) topWindow = window.below;
            if (window == bottomWindow) bottomWindow = window.above;
            if (window.above != null) window.above.below = window.below;
            if (window.below != null) window.below.above = window.above;
            window.above = null;
            window.below = null;
            window.isVisible = false;
        }
    }
    
    /**
     * Brings the given window to the top of the window stack. Does nothing if
     * the window is hidden or already at the top of the stack.
     */
    public static void bringToTop(Window window)
    {
        if (window.isVisible && window != topWindow)
        {
            hide(window);
            show(window);
        }
    }
    
    /**
     * Returns the topmost window in the window stack, or null if the stack is
     * empty.
     */
    public static Window getTopWindow()
    {
        return topWindow;
    }
    
    /**
     * Returns true if any windows or forms are currently hovered or active.
     */
    public static boolean anyActive()
    {
        return hoveredWindow != null || activeWindow != null ||
               hoveredForm != null || activeForm != null ||
               (dropDown != null && dropDownHovered);
    }
    
    /**
     * Returns the topmost window the mouse is currently over, or null if no
     * such window exists.
     */
    public static Window getHoveredWindow()
    {
        return hoveredWindow;
    }
    
    /**
     * Returns the topmost form the mouse is currently over, or null if no such
     * form exists.
     */
    public static Form getHoveredForm()
    {
        return hoveredForm;
    }
    
    /**
     * Shows the given DropDown. It will be visible above all windows until a
     * click event happens anywhere outside its bounds.
     */
    public static void dropDown(DropDown dropDown, float parentX, float parentY, Vec2 parentSize)
    {
        DUI.dropDown = Objects.requireNonNull(dropDown);
        if (dropDown != null)
        {
            Vec2 ddSize = dropDown.getSize();
            dropDown.layout(parentX, parentY - ddSize.y);
        }
    }
    
    public static DropDown getDropDown()
    {
        return dropDown;
    }
    
    public static void closeDropDown()
    {
        DUI.dropDown = null;
    }
    
    /**
     * Sends a mouse move event to DevilUI. These events are needed to determine
     * which window or form is currently hovered.
     */
    public static void mouseMoved(float x, float y)
    {
        mouseX = x; mouseY = y;
        
        hoveredForm = null;
        hoveredWindow = null;
        hoveredScrollBox = null;
        
        if (activeForm != null)
        {
            activeForm.hover(x, y);
            return;
        }
        
        if (activeWindow != null)
        {
            activeWindow.hover(x, y);
            return;
        }
        
        if (dropDown != null)
        {
            Object hovered = dropDown.hover(x, y);
            ScrollBox scrollBox = dropDown.findScrollBox(x, y);
            
            if (hovered instanceof Form) hoveredForm = (Form)hovered;
            
            dropDownHovered = hovered != null;
            if (dropDownHovered)
            {
                hoveredScrollBox = scrollBox;
                return;
            }
        }
        
        Window window = topWindow;
        while (window != null)
        {
            Object hovered = window.hover(x, y);
            ScrollBox scrollBox = window.findScrollBox(x, y);
            if (hovered instanceof Form)
            {
                hoveredForm = (Form)hovered;
                hoveredWindow = window;
                hoveredScrollBox = scrollBox;
                break;
            }
            else if (hovered instanceof Window)
            {
                hoveredForm = null;
                hoveredWindow = window;
                hoveredScrollBox = scrollBox;
                break;
            }
            
            window = window.below;
        }
    }
    
    /**
     * Sends a mouse move event to DevilUI. DX and DY are not used, but are
     * provided so that this method can be used as a functional interface for
     * the Game class. Should be called between the beforeInput and afterInput
     * methods.
     */
    public static void mouseMoved(float x, float y, float dx, float dy)
    {
        mouseMoved(x, y);
    }
    
    /**
     * Sends a mouse button event to DevilUI.
     */
    public static void mouseButton(int button, int action, int mods)
    {
        if (button != GLFW_MOUSE_BUTTON_LEFT) return;
        
        mouseMoved(mouseX, mouseY); //Make sure hovered items are up to date.
        
        switch (action)
        {
            case GLFW_PRESS:
                
                focus(null);
                
                if (!dropDownHovered) closeDropDown();
                
                if (activeForm == null && activeWindow == null)
                {
                    if (hoveredWindow != null) bringToTop(hoveredWindow);
                    
                    if (hoveredForm != null)
                    {
                        if (hoveredForm.activate())
                        {
                            activeForm = hoveredForm;
                            activeForm.hover(mouseX, mouseY);
                        }
                    }
                    else if (hoveredWindow != null)
                    {
                        if (hoveredWindow.activate())
                        {
                            activeWindow = hoveredWindow;
                            activeWindow.hover(mouseX, mouseY);
                        }
                    }
                }
                break;
            case GLFW_RELEASE:
                if (activeForm != null)
                {
                    activeForm.deactivate();
                    activeForm =  null;
                }
                else if (activeWindow != null)
                {
                    activeWindow.deactivate();
                    activeWindow = null;
                }
                break;
        }
    }
    
    /**
     * Sends a mouse scroll event to DevilUI.
     */
    public static void mouseScroll(float dx, float dy)
    {
        if (dropDown != null && !dropDownHovered && hoveredForm != dropDown.getParent()) dropDown = null;
        
        if (hoveredScrollBox != null)
        {
            hoveredScrollBox.mouseScroll(dx, dy);
            mouseMoved(mouseX, mouseY);
        }
    }
    
    /**
     * Gives focus to the supplied form. Useful for automatically selecting
     * text fields upon opening a window. Not all forms can be meaningfully
     * focused, but all forms are accepted by this method. Null may be passed to
     * clear focus.
     */
    public static void focus(Form form)
    {
        if (form == focusedForm) return;
        if (focusedForm != null) focusedForm.defocus();
        focusedForm = form;
    }
    
    /**
     * Returns whatever form is currently focused, or null.
     */
    public static Form getFocusedForm()
    {
        return focusedForm;
    }
    
    private static long lastCaretBlink;
    private static long caretBlinkRate = 530L*2000000L;
    
    /**
     * Sets the caret blink rate. Defaults to 530 ms. Set to zero to disable
     * caret blinking.
     */
    public static void setCaretBlinkRate(int ms)
    {
        if (ms < 0) throw new IllegalArgumentException();
        caretBlinkRate = ms*2000000L;
    }
    
    /**
     * Returns if the caret is currently visible in its blink cycle.
     */
    public static boolean getCaretBlink()
    {
        long time = System.nanoTime();
        long timeSinceLastBlink = time - lastCaretBlink;
        
        long cycle = timeSinceLastBlink%caretBlinkRate;
        if (cycle < 0L) cycle += caretBlinkRate; //Ensure positive
        
        return cycle < (caretBlinkRate >> 1);
    }
    
    /**
     * Sets the caret blink timer to zero, meaning that the caret should be
     * visible right now.
     */
    public static void resetCaretBlinkTimer()
    {
        lastCaretBlink = System.nanoTime();
    }
    
    /**
     * Sends a unicode character event to DevilUI.
     */
    public static void character(char character, int codepoint)
    {
        if (focusedForm != null) focusedForm.character(character, codepoint);
    }
    
    /**
     * Sends a keyboard event to DevilUI.
     */
    public static void key(int key, int action, int mods)
    {
        if (focusedForm != null) focusedForm.key(key, action, mods);
    }
    
    /**
     * Updates the state of the UI, and then draws it.
     * 
     * It is up to the application to manage the OpenGL state. DevilUI will draw
     * to whatever GL_DRAW_FRAMEBUFFER is bound, and glViewport should be set to
     * the correct resolution. All other relevant OpenGL state should be set to
     * their default values: depth and stencil tests should be disabled, face
     * culling should be disabled, etc.
     */
    public static void render()
    {
        if (!init) throw new IllegalStateException("Not initialized.");
        
        drawer.begin();
        
        Window window = bottomWindow;
        while (window != null)
        {
            window.layout();
            window.render(drawer);
            window = window.above;
        }
        
        if (dropDown != null) dropDown.render(drawer);
    }
    
    /**
     * Destroys DevilUI and releases any associated native resources.
     */
    public static void destroy()
    {
        if (!init) throw new IllegalStateException("Not initialized.");
        drawer.destroy();
        init = false;
    }
    
    private DUI()
    {
    }
}
