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

import com.samrj.devil.game.GameWindow;
import com.samrj.devil.math.Vec2;

import java.util.Objects;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11C.GL_VIEWPORT;
import static org.lwjgl.opengl.GL11C.glGetIntegerv;

/**
 * DevilUI. A GUI library for DevilUtil.
 * 
 * @author Samuel Johnson (SmashMaster)
 */
public final class DUI
{
    private static DUIDrawer drawer;
    private static long glfwWindow;
    private static long vResizeCursor, hResizeCursor, ibeamCursor;
    private static Font font;
    private static Window topWindow, bottomWindow;
    private static boolean init;
    
    private static float mouseX, mouseY;
    
    private static Window hoveredWindow, activeWindow;
    private static Form hoveredForm, activeForm;
    private static int activeFormButton;
    private static Form focusedForm;
    private static Form hoveredScrollBox;
    private static DropDown dropDown;
    private static boolean dropDownHovered;
    private static Form background;
    
    /**
     * Initializes DevilUI.
     */
    public static void init()
    {
        if (init) throw new IllegalStateException("DUI already initialized.");
        drawer = new DUIDrawer();
        glfwWindow = GameWindow.getWindow();
        vResizeCursor = glfwCreateStandardCursor(GLFW_VRESIZE_CURSOR);
        hResizeCursor = glfwCreateStandardCursor(GLFW_HRESIZE_CURSOR);
        ibeamCursor = glfwCreateStandardCursor(GLFW_IBEAM_CURSOR);
        init = true;
    }

    static void setCursor(Hoverable hovered)
    {
        Cursor cursor = hovered != null ? hovered.getHoverCursor() : Cursor.DEFAULT;
        glfwSetCursor(glfwWindow, switch (cursor)
        {
            case DEFAULT -> 0;
            case V_RESIZE -> vResizeCursor;
            case H_RESIZE -> hResizeCursor;
            case IBEAM -> ibeamCursor;
        });
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
    
    //Adds window to top of linked list
    private static void addLinked(Window window)
    {
        if (topWindow != null)
        {
            topWindow.above = window;
            window.below = topWindow;
        }
        else bottomWindow = window;
        topWindow = window;
    }
    
    /**
     * Displays the given window, putting it at the top of the window stack.
     * Does nothing if the window is already visible.
     */
    public static void show(Window window)
    {
        if (!window.isVisible)
        {
            addLinked(window);
            window.isVisible = true;
        }
    }
    
    //Removes window from linked list
    private static void removeLinked(Window window)
    {
        if (window == topWindow) topWindow = window.below;
        if (window == bottomWindow) bottomWindow = window.above;
        if (window.above != null) window.above.below = window.below;
        if (window.below != null) window.below.above = window.above;
        window.above = null;
        window.below = null;
    }
    
    /**
     * Hides the given window. Does nothing if the window is already hidden.
     */
    public static void hide(Window window)
    {
        if (window.isVisible)
        {
            //Update any related state
            if (hoveredWindow == window) hoveredWindow = null;
            if (activeWindow == window)
            {
                activeWindow.deactivate();
                activeWindow = null;
            }
            if (hoveredForm != null && hoveredForm.getWindow() == window) hoveredForm = null;
            if (activeForm != null && activeForm.getWindow() == window)
            {
                activeForm.deactivate();
                activeForm = null;
            }
            if (focusedForm != null && focusedForm.getWindow() == window)
            {
                focusedForm.defocus();
                focusedForm = null;
            }
            if (hoveredScrollBox != null && hoveredScrollBox.getWindow() == window) hoveredScrollBox = null;
            if (dropDown != null && dropDown.getParentWindow() == window)
            {
                dropDown = null;
                dropDownHovered = false;
            }
            
            removeLinked(window);
            
            //Finish up
            window.isVisible = false;
            if (window.onClose != null) window.onClose.accept(window);
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
        dropDown = null;
        dropDownHovered = false;
    }
    
    /**
     * Sets the DUI background form. The background is always behind any open
     * windows, and does not count as an open window.
     */
    public static void setBackground(Form background)
    {
        if (background != DUI.background && DUI.background != null)
        {
            //The background is the only form which belongs to a null window.
            if (hoveredForm != null && hoveredForm.getWindow() == null) hoveredForm = null;
            if (activeForm != null && activeForm.getWindow() == null)
            {
                activeForm.deactivate();
                activeForm = null;
            }
            if (focusedForm != null && focusedForm.getWindow() == null)
            {
                focusedForm.defocus();
                focusedForm = null;
            }
            if (hoveredScrollBox != null && hoveredScrollBox.getWindow() == null) hoveredScrollBox = null;
            if (dropDown != null && dropDown.getParentWindow() == null)
            {
                dropDown = null;
                dropDownHovered = false;
            }
        }
        
        DUI.background = background;
    }
    
    /**
     * Returns the current DUI background form.
     */
    public static Form getBackground()
    {
        return background;
    }
    
    /**
     * Sends a mouse move event to DevilUI. These events are needed to determine
     * which window or form is currently hovered.
     */
    public static void mouseMoved(float x, float y)
    {
        mouseX = x; mouseY = y;
        
        if (activeForm != null)
        {
            setCursor(activeForm.hover(x, y));
            return;
        }
        
        if (activeWindow != null)
        {
            setCursor(activeWindow.hover(x, y));
            return;
        }
        
        hoveredForm = null;
        hoveredWindow = null;
        hoveredScrollBox = null;
        
        if (dropDown != null)
        {
            Hoverable hovered = dropDown.hover(x, y);
            Form scrollBox = dropDown.findScrollBox(x, y);
            
            if (hovered instanceof Form) hoveredForm = (Form)hovered;
            
            dropDownHovered = hovered != null;
            if (dropDownHovered)
            {
                hoveredScrollBox = scrollBox;
                setCursor(hovered);
                return;
            }
        }
        
        Window window = topWindow;
        while (window != null)
        {
            Hoverable hovered = window.hover(x, y);
            Form scrollBox = window.findScrollBox(x, y);
            if (hovered instanceof Form)
            {
                hoveredForm = (Form)hovered;
                hoveredWindow = window;
                hoveredScrollBox = scrollBox;
                setCursor(hoveredForm);
                return;
            }
            else if (hovered instanceof Window)
            {
                hoveredWindow = window;
                hoveredScrollBox = scrollBox;
                setCursor(hoveredWindow);
                return;
            }
            
            window = window.below;
        }
        
        if (background != null)
        {
            hoveredForm = background.hover(x, y);
            hoveredScrollBox = background.findScrollBox(x, y);
        }

        setCursor(hoveredForm);
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
        switch (action)
        {
            case GLFW_PRESS:
                if (activeForm == null && activeWindow == null)
                {
                    focus(null);
                    mouseMoved(mouseX, mouseY); //Make sure hovered items are up to date.
                    if (!dropDownHovered) closeDropDown();
                    
                    //Bring hovered window to top if clicked
                    if (hoveredWindow != null && hoveredWindow != topWindow)
                    {
                        removeLinked(hoveredWindow);
                        addLinked(hoveredWindow);
                    }
                    
                    if (hoveredForm != null)
                    {
                        if (hoveredForm.activate(button))
                        {
                            activeForm = hoveredForm;
                            activeForm.hover(mouseX, mouseY);
                            activeFormButton = button;
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
                if (activeForm != null && button == activeFormButton)
                {
                    activeForm.deactivate();
                    activeForm = null;
                    mouseMoved(mouseX, mouseY);
                }
                else if (activeWindow != null)
                {
                    activeWindow.deactivate();
                    activeWindow = null;
                    mouseMoved(mouseX, mouseY);
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
        resetCaretBlinkTimer();
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
        
        if (background != null)
        {
            background.updateSize();
            background.layout(null, background.x0, background.y0);
            background.render(drawer);
        }
        
        Window window = bottomWindow;
        while (window != null)
        {
            window.layout();
            window.render(drawer);
            window = window.above;
        }
        
        if (dropDown != null) dropDown.render(drawer);
        
        drawer.end();
    }
    
    /**
     * Destroys DevilUI and releases all associated native resources.
     */
    public static void destroy()
    {
        if (!init) throw new IllegalStateException("Not initialized.");
        drawer.destroy();
        glfwDestroyCursor(vResizeCursor);
        glfwDestroyCursor(hResizeCursor);
        init = false;
    }
    
    private DUI()
    {
    }
}
