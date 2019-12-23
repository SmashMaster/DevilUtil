package com.samrj.devil.gui;

import com.samrj.devil.math.Vec2;
import java.util.ArrayList;
import java.util.Collections;

/**
 * A fixed row arranges forms by a series of predetermined positions. It is
 * possible to cause forms to overlap, so care must be taken when using this
 * layout.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2019 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class FixedRow extends Form
{
    private final ArrayList<Float> positions = new ArrayList<>();
    private final ArrayList<Form> forms = new ArrayList<>();
    private final Vec2 alignment = Align.NW.vector();
    
    public FixedRow()
    {
    }
    
    public FixedRow clear()
    {
        positions.clear();
        forms.clear();
        return this;
    }
    
    public FixedRow add(float position, Form form)
    {
        int insertIndex = Collections.binarySearch(positions, position);
        if (insertIndex < 0) insertIndex = -insertIndex - 1;
        positions.add(insertIndex, position);
        forms.add(insertIndex, form);
        return this;
    }
    
    public FixedRow setWidth(float width)
    {
        this.width = width;
        return this;
    }
    
    public FixedRow setHeight(float height)
    {
        this.height = height;
        return this;
    }
    
    public FixedRow setSize(float width, float height)
    {
        this.width = width;
        this.height = height;
        return this;
    }
    
    public FixedRow setSizeFromContent()
    {
        height = 0.0f;
        for (int i=0; i<forms.size(); i++)
        {
            Form form = forms.get(i);
            form.updateSize();
            if (form.height > height) height = form.height;
        }
        
        int last = forms.size() - 1;
        width = positions.get(last) + forms.get(last).width;
        
        return this;
    }
    
    public FixedRow setAlignment(Vec2 alignment)
    {
        this.alignment.set(alignment);
        return this;
    }
    
    @Override
    protected void updateSize()
    {
        for (Form form : forms) form.updateSize();
    }
    
    @Override
    protected void layout(Window window, float x, float y)
    {
        super.layout(window, x, y);
        float y1 = y0 + height;
        
        for (int i=0; i<forms.size(); i++)
        {
            float fx0 = x0 + positions.get(i);
            float fx1 = x0 + (i < forms.size() - 1 ? positions.get(i + 1) : width);
            Form form = forms.get(i);
            Vec2 aligned = Align.insideBounds(form.getSize(), fx0, fx1, y0, y1, alignment);
            form.layout(window, aligned.x, aligned.y);
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
