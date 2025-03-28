package com.samrj.devil.gui;

import com.samrj.devil.math.Vec2;
import com.samrj.devil.math.Vec4;

import java.util.Objects;
import java.util.function.Consumer;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;

/**
 * A toggle button or checkbox.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2025 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Toggle extends FormColor
{
    private String text;
    private final Vec2 alignment = Align.C.vector();
    private float padding = 3.0f;
    private Consumer<Toggle> onActivate;
    private boolean value = false;

    public Toggle(String text)
    {
        this.text = Objects.requireNonNull(text);
    }

    public Toggle()
    {
        this("");
    }

    public boolean getValue()
    {
        return value;
    }

    public void setValue(boolean value)
    {
        this.value = value;
    }

    public Toggle setText(String text)
    {
        this.text = Objects.requireNonNull(text);
        return this;
    }

    public Toggle setSize(float width, float height)
    {
        this.width = width;
        this.height = height;
        return this;
    }

    public Toggle setSize(float width)
    {
        this.width = width;
        this.height = DUI.font().getHeight() + padding*2.0f;
        return this;
    }

    public Toggle setSizeFromText()
    {
        Font font = DUI.font();
        width = font.getWidth(text) + padding*2.0f;
        height = font.getHeight() + padding*2.0f;
        return this;
    }

    public Toggle setAlignment(Vec2 alignment)
    {
        this.alignment.set(alignment);
        return this;
    }

    public Toggle setPadding(float padding)
    {
        if (padding < 0.0f) throw new IllegalArgumentException();
        this.padding = padding;
        return this;
    }

    public Toggle setCallback(Consumer<Toggle> onActivate)
    {
        this.onActivate = onActivate;
        return this;
    }

    @Override
    protected Form hover(float x, float y)
    {
        if (x < this.x0 || x > this.x0 + width || y < this.y0 || y > this.y0 + height) return null;
        return this;
    }

    @Override
    protected boolean activate(int button)
    {
        if (button != GLFW_MOUSE_BUTTON_LEFT) return false;
        value = !value;
        if (onActivate != null) onActivate.accept(this);
        return false;
    }
    
    @Override
    protected void render(DUIDrawer drawer)
    {
        float x1 = x0 + width, y1 = y0 + height;
        
        Vec4 outlineColor = DUI.getHoveredForm() == this ? activeColor : lineColor;
        
        drawer.color(backgroundColor);
        drawer.rectFill(x0, x1, y0, y1);
        if (value)
        {
            drawer.color(selectionColor);
            drawer.rectFill(x0 + padding, x1 - padding, y0 + padding, y1 - padding);
        }
        drawer.color(outlineColor);
        drawer.rect(x0, x1, y0, y1);
        
        Font font = DUI.font();
        Vec2 aligned = Align.insideBounds(font.getSize(text), x0 + padding, x1 - padding, y0 + padding, y1 - padding, alignment);
        drawer.text(text, font, aligned.x, aligned.y);
    }
}
