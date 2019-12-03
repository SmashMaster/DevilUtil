package com.samrj.devil.gui;

import com.samrj.devil.math.Vec2;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * A simple button.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2019 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Button extends Form
{
    private String text;
    private float x0, y0;
    private final Vec2 alignment = Align.C.vector();
    private float padding = 5.0f;
    private Consumer<Button> onActivate;
    
    public Button(String text)
    {
        this.text = Objects.requireNonNull(text);
    }
    
    public Button setText(String text)
    {
        this.text = Objects.requireNonNull(text);
        return this;
    }
    
    public Button setSize(float width, float height)
    {
        this.width = width;
        this.height = height;
        return this;
    }
    
    public Button shrinkToText()
    {
        Font font = DUI.font();
        width = font.getWidth(text) + padding*2.0f;
        height = font.getHeight() + padding*2.0f;
        return this;
    }
    
    public Button setAlignment(Vec2 alignment)
    {
        this.alignment.set(alignment);
        return this;
    }
    
    public Button setPadding(float padding)
    {
        if (padding < 0.0f) throw new IllegalArgumentException();
        this.padding = padding;
        return this;
    }
    
    public Button setCallback(Consumer<Button> onActivate)
    {
        this.onActivate = onActivate;
        return this;
    }
    
    @Override
    void updateSize()
    {
    }
    
    @Override
    void setAbsPos(float x, float y)
    {
        x0 = x; y0 = y;
    }

    @Override
    Form hover(float x, float y)
    {
        if (x < this.x0 || x > this.x0 + width || y < this.y0 || y > this.y0 + height) return null;
        return this;
    }

    @Override
    boolean activate()
    {
        if (onActivate != null) onActivate.accept(this);
        return false;
    }
    
    @Override
    void render(DUIDrawer drawer)
    {
        float x1 = x0 + width, y1 = y0 + height;
        
        float outline = DUI.getHoveredForm() == this ? 1.0f : 0.75f;
        
        drawer.color(0.25f, 0.25f, 0.25f, 1.0f);
        drawer.rectFill(x0, x1, y0, y1);
        drawer.color(outline, outline, outline, 1.0f);
        drawer.rect(x0, x1, y0, y1);
        
        Font font = DUI.font();
        Vec2 aligned = Align.insideBounds(font.getSize(text), x0 + padding, x1 - padding, y0 + padding, y1 - padding, alignment);
        drawer.color(1.0f, 1.0f, 1.0f, 1.0f);
        drawer.text(text, font, aligned.x, aligned.y);
    }
}
