package com.samrj.devil.gui;

import com.samrj.devil.math.Vec2;

import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A layout of vertical columns, with forms being added from left to right.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2025 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class LayoutManual extends Form
{
    private final ArrayList<Form> forms = new ArrayList<>();
    private final ArrayList<Supplier<Vec2>> positions = new ArrayList<>();

    public LayoutManual()
    {
    }
    
    public LayoutManual clear()
    {
        forms.clear();
        positions.clear();
        return this;
    }

    public LayoutManual add(Form form, Supplier<Vec2> pos)
    {
        forms.add(Objects.requireNonNull(form));
        positions.add(Objects.requireNonNull(pos));
        return this;
    }

    public <T extends Form> LayoutManual add(T form, Function<T, Vec2> pos)
    {
        if (pos == null) throw new NullPointerException();
        forms.add(Objects.requireNonNull(form));
        positions.add(() -> pos.apply(form));
        return this;
    }

    @Override
    protected void updateSize()
    {
        if (forms.size() == 0)
        {
            width = 0.0f;
            height = 0.0f;
            return;
        }

        float localX0 = Float.POSITIVE_INFINITY; float localX1 = Float.NEGATIVE_INFINITY;
        float localY0 = Float.POSITIVE_INFINITY; float localY1 = Float.NEGATIVE_INFINITY;

        for (int i=0; i<forms.size(); i++)
        {
            Form form = forms.get(i);
            Vec2 position = positions.get(i).get();
            form.updateSize();

            localX0 = Math.min(localX0, position.x);
            localX1 = Math.max(localX1, position.x + form.width);

            localY0 = Math.min(localY0, position.y);
            localY1 = Math.max(localY1, position.y + form.height);
        }

        width = localX1 - localX0;
        height = localY1 - localY0;
    }

    @Override
    protected void layout(Window window, float x0, float y0)
    {
        super.layout(window, x0, y0);

        for (int i=0; i<forms.size(); i++)
        {
            Form form = forms.get(i);
            Vec2 position = positions.get(i).get();
            form.layout(window, x0 + position.x, y0 + position.y);
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
