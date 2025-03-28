package com.samrj.devil.gui;

import com.samrj.devil.math.Vec4;

/**
 * The subclass for forms that can render an outline and solid background, but don't by default.
 *
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2025 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public abstract class FormColorable extends Form
{
    public FormColorable()
    {
        super();
    }

    /**
     * The color of this form behind all other elements.
     */
    public final Vec4 backgroundColor = new Vec4();

    /**
     * The color of inset elements, such as text fields.
     */
    public final Vec4 insetColor = new Vec4();

    /**
     * The color of raised elements such as scrollbars.
     */
    public final Vec4 foregroundColor = new Vec4();

    /**
     * The color of selected elements like text, and enabled toggle buttons.
     */
    public final Vec4 selectionColor = new Vec4();

    /**
     * The color of outlines and text.
     */
    public final Vec4 lineColor = new Vec4();

    /**
     * The color of outlines and text, when interactive forms such as buttons are hovered.
     */
    public final Vec4 activeColor = new Vec4();

    @Override
    protected void render(DUIDrawer drawer)
    {
        float x1 = x0 + width, y1 = y0 + height;

        if (backgroundColor.w > 0.0f)
        {
            drawer.color(backgroundColor);
            drawer.rectFill(x0, x1, y0, y1);
        }

        if (lineColor.w > 0.0f)
        {
            drawer.color(lineColor);
            drawer.rect(x0, x1, y0, y1);
        }
    }
}
