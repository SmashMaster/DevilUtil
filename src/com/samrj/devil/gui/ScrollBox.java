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
    public static final float SCROLLBAR_WIDTH = 20.0f;
    
    private static final float SCROLL_RATE = 53.0f;
    
    private Form content;
    private final Vec2 alignment = Align.NW.vector();
    private float padding = 10.0f;
    private float scrollY;
    
    private float dragStartY;
    private boolean scrollBarHovered, scrollBarDragged;
    
    public ScrollBox setContent(Form form)
    {
        content = form;
        return this;
    }
    
    public ScrollBox setSize(float width, float height)
    {
        this.width = width;
        this.height = height;
        return this;
    }
    
    public ScrollBox setWidth(float width)
    {
        this.width = width;
        return this;
    }
    
    public ScrollBox setHeight(float height)
    {
        this.height = height;
        return this;
    }
    
    public ScrollBox setSizeFromContent(float maxHeight)
    {
        if (content == null) return this;
        content.updateSize();
        this.width = content.width + padding*2.0f + SCROLLBAR_WIDTH;
        this.height = Util.min(content.height + padding*2.0f, maxHeight);
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
    protected void updateSize()
    {
        if (content != null) content.updateSize();
    }

    @Override
    protected void layout(float x, float y)
    {
        x0 = x; y0 = y;
        
        if (content != null)
        {
            float y1 = y0 + height - padding;
            
            Vec2 size = new Vec2(content.width, content.height);
            Vec2 aligned = Align.insideBounds(size, x0 + padding, x0 + width - SCROLLBAR_WIDTH,
                    y1 - content.height, y1, alignment);
            content.layout(aligned.x, aligned.y + scrollY);
        }
    }
    
    private void clampScroll()
    {
        //Could try to implement alignment here, as this works only for NW.
        float cHeight = content != null ? content.height + padding*2.0f : 0.0f;
        float max = cHeight - height;
        
        if (max < 0.0f)
        {
            scrollY = 0.0f;
            return;
        }
        
        scrollY = Util.clamp(scrollY, 0.0f, max);
    }
    
    @Override
    protected Form hover(float x, float y)
    {
        
        float cHeight = content != null ? content.height + padding*2.0f : 0.0f;
        float sbRatio = height/cHeight;
        
        if (scrollBarDragged)
        {
            scrollY = dragStartY + (y0 - y)/sbRatio;
            clampScroll();
            layout(x0, y0);
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
        scrollY -= dy*SCROLL_RATE;
        clampScroll();
        layout(x0, y0);
    }
    
    @Override
    protected boolean activate()
    {
        if (scrollBarHovered)
        {
            scrollBarDragged = true;
            return true;
        }
        
        return false;
    }
    
    @Override
    protected void deactivate()
    {
        scrollBarDragged = false;
    }
    
    @Override
    protected ScrollBox findScrollBox(float x, float y)
    {
        if (x < this.x0 || x > this.x0 + width || y < this.y0 || y > this.y0 + height) return null;
        
        if (content != null)
        {
            ScrollBox inner = content.findScrollBox(x, y);
            if (inner != null) return inner;
        }
        
        return this;
    }

    @Override
    protected void render(DUIDrawer drawer)
    {
        float x1 = x0 + width, y1 = y0 + height;
        float scrollBarX = x1 - SCROLLBAR_WIDTH;
        
        drawer.color(0.75f, 0.75f, 0.75f, 1.0f);
        drawer.rect(x0, x1, y0, y1);
        drawer.color(0.75f, 0.75f, 0.75f, 1.0f);
        drawer.line(scrollBarX, scrollBarX, y0, y1);
        
        float cHeight = content != null ? content.height + padding*2.0f : 0.0f;
        if (height < cHeight)
        {
            float sbColor = (scrollBarDragged || (DUI.getHoveredForm() == this && scrollBarHovered)) ? 1.0f : 0.75f;
            float sbRatio = height/cHeight;
            float sbY1 = y1 - scrollY*sbRatio;
            float sbY0 = sbY1 - height*sbRatio;
            drawer.color(0.375f, 0.375f, 0.375f, 1.0f);
            drawer.rectFill(scrollBarX, x1, sbY0, sbY1);
            drawer.color(sbColor, sbColor, sbColor, 1.0f);
            drawer.rect(scrollBarX, x1, sbY0, sbY1);
        }
        
        //Nested scrollboxes not supported yet. Could use a scissor stack.
        glEnable(GL_SCISSOR_TEST);
        glScissor((int)x0, (int)y0, (int)(width - SCROLLBAR_WIDTH), (int)height - 1);
        
        if (content != null) content.render(drawer);
        
        glDisable(GL_SCISSOR_TEST);
    }
}
