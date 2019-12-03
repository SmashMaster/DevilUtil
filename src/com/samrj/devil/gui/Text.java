package com.samrj.devil.gui;

import java.util.Objects;

/**
 * Simple text.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2019 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public class Text extends Form
{
    private String text;
    private float x0, y0;
    
    public Text(String text)
    {
        this.text = Objects.requireNonNull(text);
    }
    
    public Text setText(String text)
    {
        this.text = Objects.requireNonNull(text);
        return this;
    }
    
    @Override
    protected void updateSize()
    {
        Font font = DUI.font();
        width = font.getWidth(text);
        height = font.getHeight();
    }
    
    @Override
    protected void layout(float x, float y)
    {
        x0 = x; y0 = y;
    }
    
    @Override
    protected void render(DUIDrawer drawer)
    {
        drawer.color(0.75f, 0.75f, 0.75f, 1.0f);
        drawer.text(text, DUI.font(), x0, y0);
    }
}
