package com.samrj.devil.gui;


import com.samrj.devil.math.Util;
import com.samrj.devil.math.Vec2;

import static org.lwjgl.opengl.GL11C.*;

/**
 * May contain content larger than itself by allowing for scrolling.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2019 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class ScrollBox extends Form
{
    private static final float SCROLLBAR_WIDTH = 20.0f;
    private static final float SCROLL_RATE = 53.0f;
    
    private Form content;
    private float x0, y0;
    private final Vec2 alignment = Align.NW.vector();
    private float padding = 10.0f;
    private float scrollY;
    
    private float dragStartY;
    private boolean scrollBarHovered, scrollBarDragged;
    
    /**
     * Sets the content of this window to the given form.
     */
    public ScrollBox setContent(Form form)
    {
        if (form.window != null) throw new IllegalArgumentException("Supplied form already belongs to a window.");
        content = form;
        form.window = window;
        return this;
    }
    
    public ScrollBox setSize(float width, float height)
    {
        this.width = width;
        this.height = height;
        return this;
    }
    
    public ScrollBox setSizeFromContent(float height)
    {
        if (content == null) return this;
        content.updateSize();
        this.width = content.width + padding*2.0f + SCROLLBAR_WIDTH;
        this.height = height;
        return this;
    }
    
    public ScrollBox setAlignment(Vec2 alignment)
    {
        this.alignment.set(alignment);
        return this;
    }
    
    public ScrollBox setPadding(float padding)
    {
        if (padding < 0.0f) throw new IllegalArgumentException();
        this.padding = padding;
        return this;
    }
    
    @Override
    void updateSize()
    {
        if (content != null) content.updateSize();
    }

    @Override
    void setAbsPos(float x, float y)
    {
        x0 = x; y0 = y;
        
        if (content != null)
        {
            float y1 = y0 + height - padding;
            
            Vec2 size = new Vec2(content.width, content.height);
            Vec2 aligned = Align.insideBounds(size, x0 + padding, x0 + width - SCROLLBAR_WIDTH,
                    y1 - content.height, y1, alignment);
            content.setAbsPos(aligned.x, aligned.y + scrollY);
        }
    }
    
    @Override
    Form hover(float x, float y)
    {
        float cHeight = content != null ? content.height + padding*2.0f : 0.0f;
        float sbRatio = height/cHeight;
        
        if (scrollBarDragged)
        {
            scrollY = dragStartY + (y0 - y)/sbRatio;
            scrollY = Util.clamp(scrollY, 0.0f, cHeight - height);
        }
        else dragStartY = scrollY - (y0 - y)/sbRatio;
        
        scrollBarHovered = false;
        
        if (x < this.x0 || x > this.x0 + width || y < this.y0 || y > this.y0 + height) return null;
        
        float sbX0 = x0 + width - SCROLLBAR_WIDTH;
        float sbY1 = y0 + height - scrollY*sbRatio;
        float sbY0 = sbY1 - height*sbRatio;
        if (x >= sbX0 && y <= sbY1 && y >= sbY0)
        {
            scrollBarHovered = true;
            return this;
        }
        
        if (x >= sbX0) return this;
        
        Form form = content.hover(x, y);
        if (form != null) return form;
        
        return this;
    }
    
    void mouseScroll(float dx, float dy)
    {
        float cHeight = content != null ? content.height + padding*2.0f : 0.0f;
        scrollY = Util.clamp(scrollY - dy*SCROLL_RATE, 0.0f, cHeight - height);
    }
    
    @Override
    boolean activate()
    {
        if (scrollBarHovered)
        {
            scrollBarDragged = true;
            return true;
        }
        
        return false;
    }
    
    @Override
    void deactivate()
    {
        scrollBarDragged = false;
    }
    
    @Override
    ScrollBox findSrollbox(float x, float y)
    {
        if (x < this.x0 || x > this.x0 + width || y < this.y0 || y > this.y0 + height) return null;
        return this;
    }

    @Override
    void render(DUIDrawer drawer)
    {
        float x1 = x0 + width, y1 = y0 + height;
        float scrollBarX = x1 - SCROLLBAR_WIDTH;
        
        drawer.color(0.75f, 0.75f, 0.75f, 1.0f);
        drawer.rect(x0, x1, y0, y1);
        drawer.color(0.75f, 0.75f, 0.75f, 1.0f);
        drawer.line(scrollBarX, scrollBarX, y0, y1);
        
        float sbColor = (scrollBarDragged || (DUI.getHoveredForm() == this && scrollBarHovered)) ? 1.0f : 0.75f;
        
        float cHeight = content != null ? content.height + padding*2.0f : 0.0f;
        float sbRatio = height/cHeight;
        float sbY1 = y1 - scrollY*sbRatio;
        float sbY0 = sbY1 - height*sbRatio;
        drawer.color(0.375f, 0.375f, 0.375f, 1.0f);
        drawer.rectFill(scrollBarX, x1, sbY0, sbY1);
        drawer.color(sbColor, sbColor, sbColor, 1.0f);
        drawer.rect(scrollBarX, x1, sbY0, sbY1);
        
        //Nested scrollboxes not supported yet. Could use a scissor stack.
        glEnable(GL_SCISSOR_TEST);
        glScissor((int)x0, (int)y0, (int)(width - SCROLLBAR_WIDTH - 1), (int)height - 1);
        
        if (content != null) content.render(drawer);
        
        glDisable(GL_SCISSOR_TEST);
    }
}
