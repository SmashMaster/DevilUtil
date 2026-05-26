package com.samrj.devil.gui;

import com.samrj.devil.math.Util;
import com.samrj.devil.math.Vec4;

import java.util.function.Consumer;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;

/**
 * A slider can be used to choose a numerical value.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2019 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Slider extends FormColor
{
    private float value;
    private Consumer<Slider> onChange;
    
    private float dragStartX;
    private boolean barHovered, boxHovered, dragging;
    
    public float getValue()
    {
        return value;
    }
    
    public Slider setValue(float value)
    {
        this.value = Util.saturate(value);
        return this;
    }
    
    
    public Slider setSize(float width, float height)
    {
        this.width = width;
        this.height = height;
        return this;
    }
    
    public Slider setChangeCallback(Consumer<Slider> onChange)
    {
        this.onChange = onChange;
        return this;
    }
    
    @Override
    protected Form hover(float x, float y)
    {
        float boxW = height/2.0f;
        float boxX = x0 + Util.lerp(boxW, width - boxW, value);
        
        if (dragging)
        {
            float oldValue = value;
            setValue((x - x0 - boxW + dragStartX)/(width - height));
            if (onChange != null && value != oldValue) onChange.accept(this);
        }
        else dragStartX = boxX - x;
        
        if (!contains(x, y)) return null;
        
        float bx0 = boxX - boxW;
        float bx1 = boxX + boxW;
        
        boxHovered = x >= bx0 && x <= bx1;
        barHovered = !boxHovered;
        
        return this;
    }
    
    @Override
    protected boolean activate(int button)
    {
        if (button != GLFW_MOUSE_BUTTON_LEFT) return false;
        if (barHovered) dragStartX = 0.0f;
        dragging = true;
        return true;
    }
    
    @Override
    protected void deactivate()
    {
        dragging = false;
    }
    
    @Override
    protected void render(DUIDrawer drawer)
    {
        float boxW = height/2.0f;
        float boxX = x0 + Util.lerp(boxW, width - boxW, value);
        float bx0 = boxX - boxW;
        float bx1 = boxX + boxW;
        
        float lineY = y0 + boxW;
        
        Vec4 outlineColor = (barHovered && DUI.getHoveredForm() == this) ? activeColor : lineColor;
        Vec4 boxOutlineColor = (dragging || (boxHovered && DUI.getHoveredForm() == this)) ? activeColor : lineColor;
        
        drawer.color(outlineColor);
        drawer.line(x0, x0 + width, lineY, lineY);
        
        drawer.color(foregroundColor);
        drawer.rectFill(bx0, bx1, y0, y0 + height);
        drawer.color(boxOutlineColor);
        drawer.rect(bx0, bx1, y0, y0 + height);
    }
}
