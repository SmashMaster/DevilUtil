package com.samrj.devil.gui;

import com.samrj.devil.math.Vec2;

/**
 * A DropDown can cover up other parts of a window or even extend out of the
 * bounds of its parent window. Because of this, special handling is required to
 * create one. A DropDown is not a Form--it is a special container similar to a
 * Window.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2019 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public final class DropDown
{
    private final Form parent;
    private Form content;
    private float x0 = 128.0f, y0 = 128.0f, width, height;
    private final Vec2 alignment = Align.NW.vector();
    private float padding = 0.0f;
    
    public DropDown(Form parent)
    {
        this.parent = parent;
    }
    
    public Form getParent()
    {
        return parent;
    }
    
    public Window getParentWindow()
    {
        return parent.getWindow();
    }
    
    public Vec2 getPos()
    {
        return new Vec2(x0, y0);
    }
    
    public Vec2 getSize()
    {
        return new Vec2(width, height);
    }
    
    public DropDown setContent(Form form)
    {
        content = form;
        return this;
    }
    
    public DropDown setSize(float width, float height)
    {
        this.width = width;
        this.height = height;
        return this;
    }
    
    public DropDown setSizeFromContent()
    {
        if (content == null) return this;
        content.updateSize();
        width = content.width + padding*2.0f;
        height = content.height + padding*2.0f;
        return this;
    }
    
    public DropDown setAlignment(Vec2 alignment)
    {
        this.alignment.set(alignment);
        return this;
    }
    
    public DropDown setPadding(float padding)
    {
        if (padding < 0.0f) throw new IllegalArgumentException();
        this.padding = padding;
        return this;
    }
    
    Object hover(float x, float y)
    {
        if (x < x0 || x > x0 + width || y < y0 || y > y0 + height) return null;
        
        if (content != null)
        {
            Form form = content.hover(x, y);
            if (form != null) return form;
        }
        
        return this;
    }
    
    Form findScrollBox(float x, float y)
    {
        if (x < x0 || x > x0 + width || y < y0 || y > y0 + height) return null;
        if (content == null) return null;
        return content.findScrollBox(x, y);
    }
    
    void layout(float x, float y)
    {
        x0 = x; y0 = y;
        float x1 = x0 + width, y1 = y0 + height;
        
        if (content != null)
        {
            content.updateSize();
            Vec2 aligned = Align.insideBounds(new Vec2(content.width, content.height),
                    x0 + padding, x1 - padding, y0 + padding, y1 - padding, alignment);
            content.layout(getParentWindow(), aligned.x, aligned.y);
        }
    }
    
    void render(DUIDrawer drawer)
    {
        float x1 = x0 + width, y1 = y0 + height;
        
        drawer.color(0.25f, 0.25f, 0.25f, 1.0f);
        drawer.rectFill(x0, x1, y0, y1);
        drawer.color(0.75f, 0.75f, 0.75f, 1.0f);
        drawer.rect(x0, x1, y0, y1);
        
        if (content != null) content.render(drawer);
    }
}
