package com.samrj.devil.gui;

import com.samrj.devil.math.Util;
import com.samrj.devil.math.Vec2;

import java.util.ArrayList;
import java.util.Objects;

/**
 * A layout of vertical columns, with forms being added from left to right.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2019 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class LayoutColumns extends FormColorable implements LayoutAligned<LayoutColumns>
{
    private final ArrayList<Form> forms = new ArrayList<>();
    private final ArrayList<Vec2> alignments = new ArrayList<>();
    private final Vec2 defaultAlignment;
    private float spacing = 10.0f;
    
    public LayoutColumns(Vec2 defaultAlignment)
    {
        this.defaultAlignment = Objects.requireNonNull(defaultAlignment);
    }

    public LayoutColumns(Align defaultAlignment)
    {
        this(defaultAlignment.vector());
    }

    public LayoutColumns()
    {
        this(Align.NW);
    }
    
    public LayoutColumns clear()
    {
        forms.clear();
        alignments.clear();
        return this;
    }
    
    public LayoutColumns add(Form form, Vec2 alignment)
    {
        forms.add(Objects.requireNonNull(form));
        alignments.add(Objects.requireNonNull(alignment));
        return this;
    }

    public LayoutColumns add(Form form, Align alignment)
    {
        return add(form, alignment.vector());
    }

    public LayoutColumns add(Form form)
    {
        return add(form, defaultAlignment);
    }
    
    public LayoutColumns setAllAlignments(Vec2 alignment)
    {
        for (Vec2 a : alignments) a.set(alignment);
        return this;
    }
    
    public LayoutColumns setSpacing(float spacing)
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
    protected void layout(Window window, float x0, float y0)
    {
        super.layout(window, x0, y0);
        float x = x0;
        float y1 = y0 + height;
        
        for (int i=0; i<forms.size(); i++)
        {
            Form form = forms.get(i);
            Vec2 alignment = alignments.get(i);
            
            Vec2 size = new Vec2(form.width, form.height);
            float nextX = x + form.width;
            Vec2 aligned = Align.insideBounds(size, x, nextX, y0, y1, alignment);
            form.layout(window, aligned.x, aligned.y);
            x = nextX + spacing;
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
        super.render(drawer);
        for (Form form : forms) form.render(drawer);
    }
}
