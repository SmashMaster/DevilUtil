package com.samrj.devil.gui;

import com.samrj.devil.math.Vec4;
import java.util.Objects;

/**
 *
 * @author angle
 */
public class BorderForm extends Form
{
    private Form content;
    private float padding;
    private Vec4 borderColor, fillColor;

    public BorderForm(Form content, float padding, Vec4 borderColor, Vec4 fillColor) {
        this.content = content;
        this.padding = padding;
        this.borderColor = borderColor;
        this.fillColor = fillColor;
    }

    public BorderForm setContent(Form content)
    {
        this.content = content;
        return this;
    }

    public BorderForm setPadding(float padding)
    {
        this.padding = padding;
        return this;
    }

    public BorderForm setBorderColor(Vec4 borderColor)
    {
        this.borderColor = borderColor;
        return this;
    }

    public BorderForm setFillColor(Vec4 fillColor)
    {
        this.fillColor = fillColor;
        return this;
    }

    @Override
    protected void render(DUIDrawer drawer)
    {
        if (Objects.nonNull(fillColor))
        {
            drawer.color(fillColor);
            drawer.rectFill(content.x0 - padding, content.x0 + content.width + padding,
                        content.y0 - padding, content.y0 + content.height + padding);
        }
        
        if (Objects.nonNull(borderColor))
        {
            drawer.color(borderColor);
            drawer.rect(content.x0 - padding, content.x0 + content.width + padding,
                        content.y0 - padding, content.y0 + content.height + padding);
        }
        
        content.render(drawer);
    }

    @Override
    protected void updateSize()
    {
        content.updateSize();
        width = content.width + padding * 2;
        height = content.height + padding * 2;
    }

    @Override
    protected void layout(Window window, float x, float y)
    {
        super.layout(window, x, y);
        content.layout(window, x + padding, y + padding);
    }

    @Override
    protected Form hover(float x, float y)
    {
        if (x < this.x0 || x > this.x0 + width || y < this.y0 || y > this.y0 + height) return null;
        else return content.hover(x, y);
    }
}