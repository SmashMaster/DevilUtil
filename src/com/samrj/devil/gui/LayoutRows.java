package com.samrj.devil.gui;

import com.samrj.devil.math.Util;
import com.samrj.devil.math.Vec2;

import java.util.ArrayList;
import java.util.Objects;

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
    private final ArrayList<Vec2> alignments = new ArrayList<>();
    private final Vec2 defaultAlignment;
    private float spacing = 10.0f;
    
    public LayoutRows clear()
    {
        forms.clear();
        return this;
    }
    
    public LayoutRows(Vec2 defaultAlignment)
    {
        this.defaultAlignment = Objects.requireNonNull(defaultAlignment);
    }
    
    public LayoutRows()
    {
        this(Align.NW.vector());
    }
    
    public LayoutRows add(Form form, Vec2 alignment)
    {
        forms.add(Objects.requireNonNull(form));
        alignments.add(Objects.requireNonNull(alignment));
        return this;
    }
    
    public LayoutRows add(Form form)
    {
        return add(form, defaultAlignment);
    }
    
    public LayoutRows setAllAlignments(Vec2 alignment)
    {
        for (Vec2 a : alignments) a.set(alignment);
        return this;
    }
    
    public LayoutRows setSpacing(float spacing)
    {
        if (spacing < 0.0f) throw new IllegalArgumentException();
        this.spacing = spacing;
        return this;
    }
    
    public float getSpacing()
    {
        return spacing;
    }
    
    @Override
    protected void updateSize()
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
    protected void layout(Window window, float x0, float y0)
    {
        super.layout(window, x0, y0);
        float x1 = x0 + width;
        float y = y0 + height;
        
        for (int i=0; i<forms.size(); i++)
        {
            Form form = forms.get(i);
            Vec2 alignment = alignments.get(i);
            
            Vec2 size = new Vec2(form.width, form.height);
            float nextY = y - form.height;
            Vec2 aligned = Align.insideBounds(size, x0, x1, nextY, y, alignment);
            form.layout(window, aligned.x, aligned.y);
            y = nextY - spacing;
        }
    }

    @Override
    protected Form hover(float x, float y)
    {
        for (Form form : forms)
        {
            Form result = form.hover(x, y);
            if (result != null) return result;
        }
        return null;
    }
    
    @Override
    protected Form findScrollBox(float x, float y)
    {
        for (Form form : forms)
        {
            Form result = form.findScrollBox(x, y);
            if (result != null) return result;
        }
        return null;
    }

    @Override
    protected void render(DUIDrawer drawer)
    {
        for (Form form : forms) form.render(drawer);
    }
}
