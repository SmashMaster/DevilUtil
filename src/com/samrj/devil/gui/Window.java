package com.samrj.devil.gui;

import com.samrj.devil.math.Util;
import com.samrj.devil.math.Vec2;

import java.util.function.Consumer;

/**
 * The base class in which interfaces are created with DevilUI. The root form
 * of a window may be anything, but is usually a layout or a scroll box.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2019 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class Window implements Hoverable
{
    private enum Drag
    {
        NONE, MOVE, RESIZE_LEFT, RESIZE_TOP, RESIZE_RIGHT, RESIZE_BOTTOM;
    }

    private static final float TITLE_BAR_HEIGHT = 30.0f;
    private static final float TITLE_PADDING = 5.0f;
    private static final float CLOSE_BUTTON_PADDING = 5.0f;
    private static final float CLOSE_BUTTON_WIDTH = TITLE_BAR_HEIGHT - CLOSE_BUTTON_PADDING*2.0f;
    private static final float CLOSE_BUTTON_CROSS_PADDING = 3.0f;
    private static final float EDGE_CLAMP_MARGIN = TITLE_BAR_HEIGHT;
    private static final float RESIZE_RANGE = 5.0f;
    
    Window above, below; //Doubly-linked list
    boolean isVisible;
    
    Consumer<Window> onClose;
    
    private Form content;
    private float x0, x1 = 256.0f, y0, y1 = 256.0f;
    private String title;
    private final Vec2 alignment = Align.NW.vector();
    private float padding = 10.0f;
    
    private float dragStartX0, dragStartX1, dragStartY0, dragStartY1;
    private boolean titleBarVisible = true, titleBarHovered;
    private Drag drag = Drag.NONE;
    private boolean draggable = true, resizable = false;
    private boolean leftResizeHovered, topResizeHovered, rightResizeHovered, bottomResizeHovered;
    private boolean closeButtonVisible = true, closeButtonHovered, closeButtonPressed;
    
    public Window()
    {
    }
    
    /**
     * Returns true if the DevilUI window stack contains this window.
     */
    public boolean isVisible()
    {
        return isVisible;
    }
    
    public Window setCloseCallback(Consumer<Window> onClose)
    {
        this.onClose = onClose;
        return this;
    }
    
    /**
     * Sets the content of this window to the given form.
     */
    public Window setContent(Form form)
    {
        content = form;
        return this;
    }
    
    /**
     * Sets the position and size of this window manually.
     */
    public Window setBounds(float x0, float x1, float y0, float y1)
    {
        this.x0 = x0; this.x1 = x1; this.y0 = y0; this.y1 = y1;
        return this;
    }
    
    /**
     * Aligns this window to the viewport. Useful for centering, or putting in a corner or against an edge.
     */
    public Window setPosAlignToViewport(Vec2 alignment)
    {
        Vec2 size = new Vec2(x1 - x0, y1 - y0);
        Vec2 viewport = DUI.viewport();
        Vec2 aligned = Align.insideBounds(size, 0.0f, viewport.x, 0.0f, viewport.y, alignment);
        x0 = aligned.x;
        x1 = x0 + size.x;
        y0 = aligned.y;
        y1 = y0 + size.y;
        return this;
    }

    /**
     * Aligns this window to the viewport. Useful for centering, or putting in a corner or against an edge.
     */
    public Window setPosAlignToViewport(Align alignment)
    {
        return setPosAlignToViewport(alignment.vector());
    }
    
    /**
     * Puts this window smack dab in the middle of the screen. Make sure to set
     * its size first, partner.
     */
    public Window setPosCenterViewport()
    {
        return setPosAlignToViewport(Align.C.vector());
    }
    
    /**
     * Sets whether this window may be repositioned by dragging the title bar.
     */
    public Window setDraggable(boolean draggable)
    {
        this.draggable = draggable;
        return this;
    }

    public Window setResizable(boolean resizable)
    {
        throw new UnsupportedOperationException(); //Make this work later.
//        this.resizable = resizable;
//        return this;
    }
    
    private float titleBarHeight()
    {
        return titleBarVisible ? TITLE_BAR_HEIGHT : 0.0f;
    }
    
    public Window setWidth(float width)
    {
        x1 = x0 + width;
        return this;
    }
    
    public Window setHeight(float height)
    {
        y1 = y0 + height;
        return this;
    }
    
    public Window setSize(float width, float height)
    {
        x1 = x0 + width;
        y1 = y0 + height;
        return this;
    }
    
    /**
     * Sets this window to the smallest size that will fit its content.
     */
    public Window setSizeFromContent()
    {
        float width = 0;
        float height = 0;
        
        if (content != null)
        {
            content.updateSize();
            width = Util.max(width, content.width + padding*2.0f);
            height = Util.max(height, content.height + padding*2.0f + titleBarHeight());
        }
        
        if (title != null) width = Util.max(width, DUI.font().getWidth(title) + padding*2.0f);
        
        height = Util.max(height, titleBarHeight());
        
        x1 = x0 + width;
        y1 = y0 + height;
        
        return this;
    }
    
    /**
     * Sets whether this window has a visible title bar.
     */
    public Window setTitleBarVisible(boolean titleBarVisible)
    {
        this.titleBarVisible = titleBarVisible;
        return this;
    }
    
    /**
     * Sets whether this window has a visible close button. Must have a title
     * bar to work.
     */
    public Window setCloseButtonVisible(boolean closeButtonVisible)
    {
        this.closeButtonVisible = closeButtonVisible;
        return this;
    }
    
    /**
     * Sets the title of this window to the given string, which may be null.
     */
    public Window setTitle(String string)
    {
        title = string;
        return this;
    }
    
    /**
     * Sets the content alignment for this window to the given vector. Defaults
     * to northwest, or (0, 1).
     */
    public Window setAlignment(Vec2 alignment)
    {
        this.alignment.set(alignment);
        return this;
    }

    public Window setAlignment(Align alignment)
    {
        return setAlignment(alignment.vector());
    }
    
    public Window setPadding(float padding)
    {
        if (padding < 0.0f) throw new IllegalArgumentException();
        this.padding = padding;
        return this;
    }

    //Used mostly to determine what is hovered.
    Hoverable hover(float x, float y)
    {
        switch (drag)
        {
            case MOVE ->
            {
                float w = x1 - x0, h = y1 - y0;
                x0 = x + dragStartX0;
                x1 = x0 + w;
                y0 = y + dragStartY0;
                y1 = y0 + h;
            }
            case RESIZE_LEFT -> x0 = x + dragStartX0;
            case RESIZE_TOP -> y1 = y + dragStartY1;
            case RESIZE_RIGHT -> x1 = x + dragStartX1;
            case RESIZE_BOTTOM -> y0 = y + dragStartY0;
            default ->
            {
                dragStartX0 = x0 - x;
                dragStartX1 = x1 - x;
                dragStartY0 = y0 - y;
                dragStartY1 = y1 - y;
            }
        }

        titleBarHovered = false;
        closeButtonHovered = false;
        leftResizeHovered = false;
        topResizeHovered = false;
        rightResizeHovered = false;
        bottomResizeHovered = false;

        if (resizable)
        {
            float[] dist = {Math.abs(x - x0), Math.abs(y - y1), Math.abs(x - x1), Math.abs(y - y0)};
            int minDistIndex = Util.mindex(dist);
            if (dist[minDistIndex] < RESIZE_RANGE)
            {
                switch (minDistIndex)
                {
                    case 0 -> leftResizeHovered = true;
                    case 1 -> topResizeHovered = true;
                    case 2 -> rightResizeHovered = true;
                    case 3 -> bottomResizeHovered = true;
                }
                return this;
            }
        }

        if (x < x0 || x > x1 || y < y0 || y > y1) return null;
        
        if (titleBarVisible && closeButtonVisible)
        {
            float bx1 = x1 - CLOSE_BUTTON_PADDING;
            float bx0 = bx1 - CLOSE_BUTTON_WIDTH;
            float by1 = y1 - CLOSE_BUTTON_PADDING;
            float by0 = by1 - CLOSE_BUTTON_WIDTH;
            
            if (x >= bx0 && x <= bx1 && y >= by0 && y <= by1)
            {
                closeButtonHovered = true;
                return this;
            }
        }
        
        if (titleBarVisible && y >= y1 - titleBarHeight())
        {
            titleBarHovered = true;
            return this;
        }
        
        if (content != null)
        {
            Form form = content.hover(x, y);
            if (form != null) return form;
        }
        
        return this;
    }

    @Override
    public Cursor getHoverCursor()
    {
        if (leftResizeHovered || rightResizeHovered) return Cursor.H_RESIZE;
        if (topResizeHovered || bottomResizeHovered) return Cursor.V_RESIZE;
        return Cursor.DEFAULT;
    }

    Form findScrollBox(float x, float y)
    {
        if (x < x0 || x > x1 || y < y0 || y > y1) return null;
        if (content == null) return null;
        return content.findScrollBox(x, y);
    }
    
    boolean activate()
    {
        if (titleBarHovered && draggable)
        {
            drag = Drag.MOVE;
            return true;
        }
        if (resizable)
        {
            if (leftResizeHovered)
            {
                drag = Drag.RESIZE_LEFT;
                return true;
            }
            if (topResizeHovered)
            {
                drag = Drag.RESIZE_TOP;
                return true;
            }
            if (rightResizeHovered)
            {
                drag = Drag.RESIZE_RIGHT;
                return true;
            }
            if (bottomResizeHovered)
            {
                drag = Drag.RESIZE_BOTTOM;
                return true;
            }
        }
        if (closeButtonHovered)
        {
            closeButtonPressed = true;
            return true;
        }
        return false;
    }
    
    void deactivate()
    {
        drag = Drag.NONE;
        if (closeButtonPressed)
        {
            closeButtonPressed = false;
            if (closeButtonHovered) DUI.hide(this); //Will call this again. Risk of infinite recursion.
        }
    }
    
    void layout()
    {
        Vec2 viewport = DUI.viewport();
        
        //Clamp to edges of viewport, and make sure title bar is always visible.
        if (x1 < EDGE_CLAMP_MARGIN)
        {
            float w = x1 - x0;
            x1 = EDGE_CLAMP_MARGIN;
            x0 = x1 - w;
        }
        else if (x0 > viewport.x - EDGE_CLAMP_MARGIN)
        {
            float w = x1 - x0;
            x0 = viewport.x - EDGE_CLAMP_MARGIN;
            x1 = x0 + w;
        }
        
        if (y1 >= viewport.y)
        {
            float h = y1 - y0;
            y1 = viewport.y;
            y0 = y1 - h;
        }
        else if (y1 < EDGE_CLAMP_MARGIN)
        {
            float h = y1 - y0;
            y1 = EDGE_CLAMP_MARGIN;
            y0 = y1 - h;
        }
        
        if (content != null)
        {
            content.updateSize();
            Vec2 aligned = Align.insideBounds(new Vec2(content.width, content.height),
                    x0 + padding, x1 - padding, y0 + padding, y1 - titleBarHeight() - padding, alignment);
            content.layout(this, aligned.x, aligned.y);
        }
    }
    
    void render(DUIDrawer drawer)
    {
        drawer.color(0.25f, 0.25f, 0.25f, 1.0f);
        drawer.rectFill(x0, x1, y0, y1);
        drawer.color(0.75f, 0.75f, 0.75f, 1.0f);
        drawer.rect(x0, x1, y0, y1);
        
        if (content != null) content.render(drawer);
        
        if (titleBarVisible)
        {
            float titleBarColor = (titleBarHovered && draggable) ? 1.0f : 0.75f;
            
            drawer.color(0.375f, 0.375f, 0.375f, 1.0f);
            drawer.rectFill(x0, x1, y1 - titleBarHeight(), y1);
            drawer.color(titleBarColor, titleBarColor, titleBarColor, 1.0f);
            drawer.rect(x0, x1, y1 - titleBarHeight(), y1);
            
            if (title != null)
            {
                Font font = DUI.font();
                Vec2 titleSize = font.getSize(title);
                Vec2 aligned = Align.insideBounds(titleSize,
                        x0 + TITLE_PADDING, x1 - TITLE_PADDING,
                        y1 + TITLE_PADDING - titleBarHeight(), y1 - TITLE_PADDING, Align.W.vector());
                drawer.text(title, font, aligned.x, aligned.y);
            }
            
            if (closeButtonVisible)
            {
                float bx1 = x1 - CLOSE_BUTTON_PADDING;
                float bx0 = bx1 - CLOSE_BUTTON_WIDTH;
                float by1 = y1 - CLOSE_BUTTON_PADDING;
                float by0 = by1 - CLOSE_BUTTON_WIDTH;
                
                float closeButtonColor = closeButtonHovered ? 1.0f : 0.75f;
                drawer.color(closeButtonColor, closeButtonColor, closeButtonColor, 1.0f);
                drawer.rect(bx0, bx1, by0, by1);
                drawer.line(bx0 + CLOSE_BUTTON_CROSS_PADDING, bx1 - CLOSE_BUTTON_CROSS_PADDING, by0 + CLOSE_BUTTON_CROSS_PADDING, by1 - CLOSE_BUTTON_CROSS_PADDING);
                drawer.line(bx0 + CLOSE_BUTTON_CROSS_PADDING, bx1 - CLOSE_BUTTON_CROSS_PADDING, by1 - CLOSE_BUTTON_CROSS_PADDING, by0 + CLOSE_BUTTON_CROSS_PADDING);
            }
        }
    }

    @Override
    public String toString()
    {
        return "Window{" + "x0=" + x0 + ", x1=" + x1 + ", y0=" + y0 + ", y1=" + y1 + ", title='" + title + '\'' + '}';
    }
}
