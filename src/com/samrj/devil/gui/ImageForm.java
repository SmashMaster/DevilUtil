package com.samrj.devil.gui;

import com.samrj.devil.gl.Image;
import com.samrj.devil.gl.Texture2D;
import com.samrj.devil.math.Vec2;

import java.util.Objects;
import java.util.function.Consumer;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;

/**
 * Form for displaying images.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2025 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class ImageForm extends FormColor
{
    private final Vec2 alignment = Align.C.vector();
    private Consumer<ImageForm> onActivate;
    private boolean clickable;
    private Texture2D texture;

    public ImageForm()
    {
        foregroundColor.set(1.0f, 1.0f, 1.0f, 1.0f);
    }

    public ImageForm(Texture2D texture)
    {
        this();
        this.texture = texture;
    }

    public ImageForm setTexture(Texture2D texture)
    {
        this.texture = texture;
        return this;
    }

    public ImageForm setSize(float width, float height)
    {
        this.width = width;
        this.height = height;
        return this;
    }

    public ImageForm setClickable(boolean clickable)
    {
        this.clickable = clickable;
        return this;
    }

    public ImageForm setAlignment(Vec2 alignment)
    {
        this.alignment.set(alignment);
        return this;
    }

    public ImageForm setCallback(Consumer<ImageForm> onActivate)
    {
        this.onActivate = onActivate;
        return this;
    }
    
    @Override
    protected Form hover(float x, float y)
    {
        if (!clickable) return null;
        if (!contains(x, y)) return null;
        return this;
    }

    @Override
    protected boolean activate(int button)
    {
        if (button != GLFW_MOUSE_BUTTON_LEFT) return false;
        if (onActivate != null) onActivate.accept(this);
        return false;
    }
    
    @Override
    protected void render(DUIDrawer drawer)
    {
        float x1 = x0 + width, y1 = y0 + height;

        drawer.color(foregroundColor);
        drawer.rectImage(x0, x1, y0, y1, texture);
        drawer.color(DUI.getHoveredForm() == this ? activeColor : lineColor);
        drawer.rect(x0, x1, y0, y1);
    }
}
