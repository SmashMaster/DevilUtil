package com.samrj.devil.gui;

import com.samrj.devil.math.Util;
import com.samrj.devil.math.Vec2;
import java.util.ArrayList;

/**
 * A layout of vertical columns, with forms being added from left to right.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2019 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class LayoutColumns extends Form
{
    private final ArrayList<Form> forms = new ArrayList<>();
    private final Vec2 alignment = Align.NW.vector();
    private float spacing = 10.0f;
    
    public LayoutColumns add(Form form)
    {
        if (form.window != null) throw new IllegalArgumentException("Supplied form already belongs to a window.");
        forms.add(form);
        form.window = window;
        return this;
    }
    
    public LayoutColumns setAlignment(Vec2 alignment)
    {
        this.alignment.set(alignment);
        return this;
    }
    
    public LayoutColumns setSpacing(float spacing)
    {
        if (spacing < 0.0f) throw new IllegalArgumentException();
        this.spacing = spacing;
        return this;
    }
    
    @Override
    void updateSize()
    {
        width = spacing*Util.max(0.0f, forms.size() - 1.0f);
        height = 0.0f;
        
        for (Form form : forms)
        {
            form.updateSize();
            width += form.width;
            if (form.height > height) height = form.height;
        }
    }

    @Override
    void setAbsPos(float x0, float y0)
    {
        float x = x0;
        float y1 = y0 + height;
        
        for (Form form : forms)
        {
            Vec2 size = new Vec2(form.width, form.height);
            float nextX = x + form.width;
            Vec2 aligned = Align.insideBounds(size, x, nextX, y0, y1, alignment);
            form.setAbsPos(aligned.x, aligned.y);
            x = nextX + spacing;
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
