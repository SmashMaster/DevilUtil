package com.samrj.devil.gui;

import com.samrj.devil.math.Util;
import com.samrj.devil.math.Vec4;

import java.util.function.Consumer;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;

/**
 * A slider can be used to display a fraction value.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2025 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class ProgressBar extends FormColor
{
    private float value;
    private Consumer<ProgressBar> onChange;

    public float getValue()
    {
        return value;
    }
    
    public ProgressBar setValue(float value)
    {
        this.value = Util.saturate(value);
        return this;
    }

    public ProgressBar setSize(float width, float height)
    {
        this.width = width;
        this.height = height;
        return this;
    }

    @Override
    protected void render(DUIDrawer drawer)
    {
        float x1 = x0 + width;
        float y1 = y0 + height;
        float valueX = Util.lerp(x0, x1, value);

        drawer.color(insetColor);
        drawer.rectFill(x0, x1, y0, y1);
        drawer.color(foregroundColor);
        drawer.rectFill(x0, valueX, y0, y1);
        drawer.color(lineColor);
        drawer.rect(x0, x1, y0, y1);
    }
}
