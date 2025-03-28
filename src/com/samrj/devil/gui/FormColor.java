package com.samrj.devil.gui;

import com.samrj.devil.math.Vec4;

/**
 * The subclass for forms that render an outline and solid background by default.
 *
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2025 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public abstract class FormColor extends FormColorable
{
    public FormColor()
    {
        super();
        backgroundColor.set(DUI.defaultBackgroundColor);
        insetColor.set(DUI.defaultInsetColor);
        foregroundColor.set(DUI.defaultForegroundColor);
        selectionColor.set(DUI.defaultSelectionColor);
        lineColor.set(DUI.defaultLineColor);
        activeColor.set(DUI.defaultActiveColor);
    }
}
