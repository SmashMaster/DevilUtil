package com.samrj.devil.gui;

import com.samrj.devil.math.Util;
import com.samrj.devil.math.Vec2;
import java.util.ArrayList;

/**
 * A layout of horizontal rows, with forms being added from top to bottom.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2019 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class LayoutRows extends Form
{
    private final ArrayList<Form> forms = new ArrayList<>();
    private final Vec2 alignment = Align.NW.vector();
    private float spacing = 10.0f;
    
    public LayoutRows add(Form form)
    {
        if (form.window != null) throw new IllegalArgumentException("Supplied form already belongs to a window.");
        forms.add(form);
        form.window = window;
        return this;
    }
    
    public LayoutRows setAlignment(Vec2 alignment)
    {
        this.alignment.set(alignment);
        return this;
    }
    
    public LayoutRows setSpacing(float spacing)
    {
        if (spacing < 0.0f) throw new IllegalArgumentException();
        this.spacing = spacing;
        return this;
    }
    
    @Override
    void updateSize()
    {
        width = 0.0f;
        height = spacing*Util.max(0.0f, forms.size() - 1.0f);
        
        for (Form form : forms)
        {
            form.updateSize();
            if (form.width > width) width = form.width;
            height += form.height;
        }
    }

    @Override
    void setAbsPos(float x0, float y0)
    {
        float x1 = x0 + width;
        float y = y0 + height;
        
        for (Form form : forms)
        {
            Vec2 size = new Vec2(form.width, form.height);
            float nextY = y - form.height;
            Vec2 aligned = Align.insideBounds(size, x0, x1, nextY, y, alignment);
            form.setAbsPos(aligned.x, aligned.y);
            y = nextY - spacing;
        }
    }

    @Override
    Form hover(float x, float y)
    {
        for (Form form : forms)
        {
            Form result = form.hover(x, y);
            if (result != null) return result;
        }
        return null;
    }
    
    @Override
    ScrollBox findSrollbox(float x, float y)
    {
        for (Form form : forms)
        {
            ScrollBox result = form.findSrollbox(x, y);
            if (result != null) return result;
        }
        return null;
    }

    @Override
    void render(DUIDrawer drawer)
    {
        for (Form form : forms) form.render(drawer);
    }
}
